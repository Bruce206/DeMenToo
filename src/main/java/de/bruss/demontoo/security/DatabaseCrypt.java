package de.bruss.demontoo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class DatabaseCrypt {

    private static Logger logger = LoggerFactory.getLogger(DatabaseCrypt.class);

    public DatabaseCrypt() {
    }

    public static String hashPassword (String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword (String candidate, String hashedPassword) {
        try {
            return BCrypt.checkpw(candidate, hashedPassword);
        } catch (IllegalArgumentException e) {
            // invalid salt version, when passwords are still saved as MD5
            logger.error(e.getMessage(), e);
            return false;
        }
    }

}
