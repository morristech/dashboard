package io.resourcepool.dashboard.security.model;

import io.resourcepool.dashboard.model.User;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Loïc Ortola on 08/06/2016.
 */
public class TokenMetaData {
    public final String login;
    public final String hash;

    public TokenMetaData(String login, String hash) {
        this.login = login;
        this.hash = hash;
    }

    public boolean isValidAgainst(User u) {
        return equals(fromUser(u));
    }

    /**
     * Creates hashed token metadata from user.
     *
     * @param u the user
     * @return the TokenMetaData
     */
    public static TokenMetaData fromUser(User u) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        byte[] salt = u.getSalt();
        byte[] password = u.getPassword();
        byte[] message = new byte[salt.length + password.length];
        System.arraycopy(salt, 0, message, 0, salt.length);
        System.arraycopy(password, 0, message, salt.length, password.length);
        byte[] hash = digest.digest(message);

        return new TokenMetaData(u.getLogin(), Base64.encodeBase64URLSafeString(hash));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TokenMetaData that = (TokenMetaData) o;

        if (login != null ? !login.equals(that.login) : that.login != null) {
            return false;
        }
        return hash != null ? hash.equals(that.hash) : that.hash == null;

    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
