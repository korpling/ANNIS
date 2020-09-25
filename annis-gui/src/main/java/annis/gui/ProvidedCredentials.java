package annis.gui;

public class ProvidedCredentials {
    private final String name;
    private final String password;

    public ProvidedCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

}
