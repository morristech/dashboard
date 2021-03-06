package io.resourcepool.dashboard.security.util;

/**
 * @author Loïc Ortola on 07/06/2016.
 */

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to encrypt and decrypt token strings
 */
public class StringEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringEncryptor.class);

    private static final int ITERATION_COUNT = 100;
    private static final String VERSION_PREFIX = "|!|";

    // Those variables are arbitrary. It is the passphrase and salt that will
    // allow the application id generation
    public static final SessionIdentifierGenerator SIG = new SessionIdentifierGenerator();
    private static final String PASSWORD = SIG.nextPassword();
    private static final byte[] SALT = SIG.nextSalt();

    private SecretKey key;

    public static final class SessionIdentifierGenerator {
        private SecureRandom random = new SecureRandom();

        public String nextPassword() {
            return new BigInteger(130, random).toString(32);
        }

        public byte[] nextSalt() {
            return new BigInteger(64, random).toByteArray();
        }
    }

    /**
     * Default constructor.
     *
     * @throws SecurityException if an error occurs
     */
    public StringEncryptor() throws SecurityException {
        initKey(PASSWORD.toCharArray(), SALT);
    }

    /**
     * @param pass the password
     * @param salt the salt
     */
    public void initKey(char[] pass, byte[] salt) {
        try {
            SecretKeyFactory kf = null;
            // JAVA
            kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            SecretKey tmpKey = kf.generateSecret(new PBEKeySpec(pass, salt, ITERATION_COUNT, 128));
            key = new SecretKeySpec(tmpKey.getEncoded(), "AES");

        } catch (Exception e) {
            throw new SecurityException("Could not generate appId: " + e.getMessage());
        }
    }

    /**
     * The version prefix is |!| or |!!|.
     *
     * @param str where the version is
     * @return version number
     */
    public String getVersionPrefix(String str) {
        // The version prefix is |!| or |!!|... It will be useful for
        // algorithm retrocompatibility in case of an evol
        Pattern p = Pattern.compile("\\|\\!+\\|");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * method to encrypt a string.
     *
     * @param str Description of the Parameter
     * @return String the encrypted string.
     * @throws SecurityException if an error occurs
     */
    public String encrypt(String str) throws SecurityException {
        try {
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = aes.doFinal(str.getBytes());
            // JAVA
            return VERSION_PREFIX + Base64.encodeBase64URLSafeString(cipherText);
        } catch (Exception e) {
            throw new SecurityException("Could not encrypt: " + e.getMessage());
        }
    }

    /**
     * method to decrypt a string.
     *
     * @param str Description of the Parameter
     * @return String the encrypted string.
     * @throws SecurityException if an error occurs
     */
    public String decrypt(String str) throws SecurityException {
        try {
            String cipherVersionPrefix = getVersionPrefix(str);
            if (cipherVersionPrefix == null) {
                return null;
            }

            String cipherWithoutPrefix = str.substring(cipherVersionPrefix.length());

            // JAVA
            byte[] cipherText = Base64.decodeBase64(cipherWithoutPrefix);

            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, key);
            return new String(aes.doFinal(cipherText));
        } catch (Exception e) {
            throw new SecurityException("Could not decrypt: " + e.getMessage());
        }
    }
}