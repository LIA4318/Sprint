package lcsfw.fw.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lcsfw.fw.file.FileUpload;


//   Prepare les arguments d'une methode de controleur depuis les parametres HTTP.
//   Une methode peut recevoir plusieurs valeurs simples ou un unique objet metier.
 
public final class ParameterBinder {

    private ParameterBinder() {
       
    }

    // Construit les arguments dans le meme ordre que la declaration de la methode. 
    public static Object[] bind(Method method, HttpServletRequest request) throws ServletException {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int index = 0; index < parameters.length; index++) {
            Class<?> parameterType = parameters[index].getType();
            if (FileUpload.class.isAssignableFrom(parameterType)) {
                // Un upload est un cas special : il vient d'une Part multipart et non de getParameter().
                if (parameters.length != 1) {
                    throw new ServletException("Une methode recevant un fichier doit avoir un seul argument: "
                            + method.getName());
                }
                arguments[index] = bindFileUpload(parameterType, parameters[index].getName(), request);
            } else if (isSimpleType(parameterType)) {
                if (!parameters[index].isNamePresent()) {
                    throw new ServletException("Nom de parametre indisponible pour " + method.getName()
                            + "; compiler avec l'option -parameters");
                }
                arguments[index] = convert(request.getParameter(parameters[index].getName()), parameterType,
                        parameters[index].getName());
            } else {
                if (parameters.length != 1) {
                    throw new ServletException("Une methode avec un parametre objet doit avoir un seul argument: "
                            + method.getName());
                }
                arguments[index] = bindObject(parameterType, request);
            }
        }
        return arguments;
    }

    /**
     * Transforme la partie multipart du champ demande en objet FileUpload.
     * Le nom du parametre Java sert de nom de champ HTML (ici, "filemap").
     */
    private static Object bindFileUpload(Class<?> fileType, String fieldName, HttpServletRequest request)
            throws ServletException {
        try {
            Part part = request.getPart(fieldName);
            if (part == null || part.getSubmittedFileName() == null
                    || part.getSubmittedFileName().isEmpty()) {
                throw new ServletException("Aucun fichier n'a ete envoye dans le champ " + fieldName);
            }

            Constructor<?> constructor = fileType.getDeclaredConstructor();
            constructor.setAccessible(true);
            FileUpload upload = (FileUpload) constructor.newInstance();
            String fileName = extractFileName(part.getSubmittedFileName());

            // Le framework remplit l'objet : le controleur recoit une instance prete a stocker.
            upload.setFieldName(fieldName);
            upload.setFileName(fileName);
            upload.setExtension(extractExtension(fileName));
            upload.setContentType(part.getContentType());
            upload.setContent(readBytes(part));
            upload.addMetadata("name", fileName);
            upload.addMetadata("extension", upload.getExtension());
            upload.addMetadata("contentType", part.getContentType());
            upload.addMetadata("size", String.valueOf(part.getSize()));
            return upload;
        } catch (ReflectiveOperationException | IOException | ClassCastException exception) {
            throw new ServletException("Impossible de construire l'objet FileUpload", exception);
        }
    }

    /** Lit la Part une seule fois afin de fournir le contenu au controleur sous forme de bytes. */
    private static byte[] readBytes(Part part) throws IOException {
        try (java.io.InputStream input = part.getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    /** Evite qu'un chemin local eventuellement fourni par un navigateur devienne le nom du fichier. */
    private static String extractFileName(String submittedFileName) {
        int slash = Math.max(submittedFileName.lastIndexOf('/'), submittedFileName.lastIndexOf('\\'));
        return slash >= 0 ? submittedFileName.substring(slash + 1) : submittedFileName;
    }

    /** Retourne une extension vide pour un fichier sans extension ou commencant par un point. */
    private static String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 && dot < fileName.length() - 1 ? fileName.substring(dot + 1) : "";
    }

    // Cree l'objet, puis remplit ses champs en faisant correspondre leurs noms aux champs HTTP.
    private static Object bindObject(Class<?> objectType, HttpServletRequest request) throws ServletException {
        final Object instance;
        try {
            Constructor<?> constructor = objectType.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (ReflectiveOperationException | SecurityException exception) {
            throw new ServletException("Impossible d'instancier " + objectType.getName()
                    + ": un constructeur sans argument accessible est requis", exception);
        }

        for (Class<?> currentType = objectType; currentType != null && currentType != Object.class;
                currentType = currentType.getSuperclass()) {
            for (Field field : currentType.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || field.isSynthetic()) {
                    continue;
                }

                String value = request.getParameter(field.getName());
                try {
                    field.setAccessible(true);
                    field.set(instance, convert(value, field.getType(), field.getName()));
                } catch (IllegalAccessException | SecurityException exception) {
                    throw new ServletException("Impossible de remplir le champ " + field.getName(), exception);
                }
            }
        }
        return instance;
    }

    // Convertit une valeur de formulaire; une valeur absente recoit le defaut Java du champ.
    private static Object convert(String value, Class<?> targetType, String name) throws ServletException {
        if (value == null) {
            return defaultValue(targetType);
        }
        try {
            if (targetType == String.class) return value;
            if (targetType == int.class || targetType == Integer.class) return Integer.valueOf(value);
            if (targetType == long.class || targetType == Long.class) return Long.valueOf(value);
            if (targetType == double.class || targetType == Double.class) return Double.valueOf(value);
            if (targetType == float.class || targetType == Float.class) return Float.valueOf(value);
            if (targetType == short.class || targetType == Short.class) return Short.valueOf(value);
            if (targetType == byte.class || targetType == Byte.class) return Byte.valueOf(value);
            if (targetType == boolean.class || targetType == Boolean.class) return Boolean.valueOf(value);
            if (targetType == char.class || targetType == Character.class) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("un caractere est attendu");
                }
                return value.charAt(0);
            }
            if (targetType == BigDecimal.class) return new BigDecimal(value);
            if (targetType == BigInteger.class) return new BigInteger(value);
            if (targetType.isEnum()) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Object enumValue = Enum.valueOf((Class<? extends Enum>) targetType, value);
                return enumValue;
            }
        } catch (IllegalArgumentException exception) {
            throw new ServletException("Valeur invalide pour le champ " + name + ": " + value, exception);
        }
        throw new ServletException("Type de binding non pris en charge: " + targetType.getName());
    }

    // Reconnait les types qui peuvent etre convertis directement depuis une valeur HTTP.
    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Integer.class || type == Long.class
                || type == Double.class || type == Float.class || type == Short.class || type == Byte.class
                || type == Boolean.class || type == Character.class || type == BigDecimal.class
                || type == BigInteger.class || type.isEnum();
    }

    // Renvoie null pour les references ou la valeur zero du type primitif. 
    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        return null;
    }
}
