package net.skykingdoms.backend.base.util.hash;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public enum HashType {
    SHA_256 {
        private final SecureRandom random = new SecureRandom();

        @Override
        public boolean comparePassword(String password, String hashedWithSalt) {
            try {
                String[] parts = hashedWithSalt.split("\\$");
                if (parts.length != 4 || !parts[1].equals("SHA")) {
                    return false;
                }
                String salt = parts[2];
                String expectedHash = parts[3];

                String actualHash = computeHash(password, salt);
                return MessageDigest.isEqual(
                        actualHash.getBytes(StandardCharsets.UTF_8),
                        expectedHash.getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                throw new RuntimeException("Error comparing SHA-256 passwords", e);
            }
        }

        @Override
        public String hashPassword(String password) {
            try {
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                String saltStr = Base64.getEncoder().encodeToString(salt);
                return "$SHA$" + saltStr + "$" + computeHash(password, saltStr);
            } catch (Exception e) {
                throw new RuntimeException("Error hashing with SHA-256", e);
            }
        }

        private String computeHash(String password, String salt) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = sha256(password) + salt;
            digest.update(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest.digest());
        }

        private String sha256(String input) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        }
    },

    SHA_512 {
        private final SecureRandom random = new SecureRandom();

        @Override
        public boolean comparePassword(String password, String hashedWithSalt) {
            try {
                String[] parts = hashedWithSalt.split(":");
                if (parts.length != 2) {
                    return false;
                }
                String salt = parts[0];
                String expectedHash = parts[1];

                String actualHash = hashWithSalt(password, salt);
                return MessageDigest.isEqual(
                        actualHash.getBytes(StandardCharsets.UTF_8),
                        expectedHash.getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                throw new RuntimeException("Error comparing SHA-512 passwords", e);
            }
        }

        @Override
        public String hashPassword(String password) {
            try {
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                String saltStr = Base64.getEncoder().encodeToString(salt);
                String hash = hashWithSalt(password, saltStr);
                return saltStr + ":" + hash;
            } catch (Exception e) {
                throw new RuntimeException("Error hashing with SHA-512", e);
            }
        }

        private String hashWithSalt(String password, String salt) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        }
    },

    MD5 {
        @Override
        public boolean comparePassword(String password, String hashed) {
            try {
                String computedHash = hashPassword(password);
                return MessageDigest.isEqual(
                        computedHash.getBytes(StandardCharsets.UTF_8),
                        hashed.getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                throw new RuntimeException("Error comparing MD5 passwords", e);
            }
        }

        @Override
        public String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hashedBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error hashing with MD5", e);
            }
        }
    },

    BCRYPT {
        private final BCrypt.Hasher hasher = BCrypt.withDefaults();
        private final BCrypt.Verifyer verifier = BCrypt.verifyer();

        @Override
        public boolean comparePassword(String password, String hashed) {
            try {
                BCrypt.Result result = verifier.verify(
                        password.getBytes(StandardCharsets.UTF_8),
                        hashed.getBytes(StandardCharsets.UTF_8)
                );
                return result.verified;
            } catch (Exception e) {
                throw new RuntimeException("Error comparing BCrypt passwords", e);
            }
        }

        @Override
        public String hashPassword(String password) {
            try {
                byte[] hashedBytes = hasher.hash(12,
                        password.getBytes(StandardCharsets.UTF_8)
                );
                return new String(hashedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Error hashing with BCrypt", e);
            }
        }
    }
    ;

    public abstract boolean comparePassword(String password, String hashed);
    public abstract String hashPassword(String password);
}