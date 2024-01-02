package git.aatufutaa.login.mongo;

public class TokenGenerator {

    //private static final MessageDigest digest;
    //private static final Random random = new Random();
    private static final Hashids hashid;

    static {
        /*try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }*/
        String salt = "NqObr7n6BXPhrI4yspiG437DZynLsEDx";
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuwxyz0123456789!@#$%&/()=?";
        hashid = new Hashids(salt, 64, alphabet);
    }

    public static String generateToken(int id) {
        String encode = hashid.encode(id);
        System.out.println("encode " + id + " -> " + encode);
        return encode;

        /*byte[] msg = new byte[24 + 8];

        // TODO: use id

        //byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        //System.arraycopy(idBytes, 0, msg, 0, idBytes.length);

        long randomValue = random.nextLong();

        for (int i = 24; i < 24 + 8; i++) {
            msg[i] = (byte) (randomValue & 0xFF);
            randomValue >>= 8;
        }

        byte[] encoded = digest.digest(msg);

        return bytesToHex(encoded);

         */
    }

    /*public static String bytesToHex(byte[] hash) {
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }*/
}
