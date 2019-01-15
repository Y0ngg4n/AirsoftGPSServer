package netty.packets.Authentification;

public class User {

    private String username;
    private String ipadress;
    private String hashedpassword;

    public User(final String username, final String hashedpassword) {
        this.username = username;
        this.hashedpassword = hashedpassword;
    }

    public void setHashedpassword(String hashedpassword) {
        this.hashedpassword = hashedpassword;
    }

    public void setIpadress(String ipadress) {
        this.ipadress = ipadress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedpassword() {
        return hashedpassword;
    }

    public String getIpadress() {
        return ipadress;
    }

    public String getUsername() {
        return username;
    }
}
