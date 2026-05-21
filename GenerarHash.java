import org.mindrot.jbcrypt.BCrypt;

public class GenerarHash {
    public static void main(String[] args) {
        String pw = "segura123";
        String hash = BCrypt.hashpw(pw, BCrypt.gensalt(12));
        System.out.println("HASH=" + hash);
        boolean checks = BCrypt.checkpw(pw, hash);
        System.out.println("CHECKS=" + checks);
    }
}
