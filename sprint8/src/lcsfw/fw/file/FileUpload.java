package lcsfw.fw.file;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Objet remis au controleur lorsqu'un formulaire contient un fichier.
 *
 * Le controleur ne manipule donc jamais directement un tableau de bytes :
 * cet objet regroupe le contenu et les informations utiles au stockage.
 */
public class FileUpload {

    /** Nom du champ HTML qui a transporte le fichier. */
    private String fieldName;

    /** Nom original du fichier, sans modification par le framework. */
    private String fileName;

    /** Extension du fichier, sans le point (exemple : "pdf"). */
    private String extension;

    /** Type MIME fourni par le navigateur. */
    private String contentType;

    /** Contenu binaire du fichier. */
    private byte[] content;

    /** Metadonnees supplementaires, extensibles sans changer l'API du controleur. */
    private final Map<String, String> metadata = new LinkedHashMap<>();

    /** Constructeur sans argument utilise par le framework par reflexion. */
    public FileUpload() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    /** Retourne les metadonnees en lecture seule pour proteger l'objet recu. */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Vue compatible avec le format filemap : le nom du champ pointe vers les
     * donnees du fichier et ses informations principales.
     */
    public Map<String, Object> getFilemap() {
        Map<String, Object> filemap = new LinkedHashMap<>();
        filemap.put("fieldName", fieldName);
        filemap.put("content", content);
        filemap.put("name", fileName);
        filemap.put("extension", extension);
        filemap.put("contentType", contentType);
        return Collections.unmodifiableMap(filemap);
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void addMetadata(String name, String value) {
        metadata.put(name, value);
    }
}
