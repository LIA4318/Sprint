package models;

// Objet metier de demonstration, rempli depuis les champs du formulaire par leur nom. 
public class Personne {

    // Chaque nom de champ doit correspondre au name d'un controle HTML.
    private String prenom;
    private String message;
    private int age;

    /// Le framework requiert ce constructeur sans argument pour instancier l'objet.
    public Personne() {
    }

    // Les getters rendent les champs lisibles depuis la JSP de resultat.
    public String getPrenom() {
        return prenom;
    }

    public String getMessage() {
        return message;
    }

    public int getAge() {
        return age;
    }
}