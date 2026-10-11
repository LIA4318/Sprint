package lcsfw.fw.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;


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
            if (isSimpleType(parameterType)) {
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