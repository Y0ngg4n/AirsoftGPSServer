package netty.packets.Authentification;

import java.util.function.Consumer;

public class Authentificator {

    public static void checkLogin(final User user, final String password, final Consumer<Boolean> authentificated) {
        if (user == null) {
            authentificated.accept(false);
            return;
        }
        authentificated.accept(BCrypt.checkpw(password, user.getHashedpassword()));
    }
}
