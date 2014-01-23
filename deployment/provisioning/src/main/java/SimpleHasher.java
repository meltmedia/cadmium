import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.util.ByteSource;

public class SimpleHasher {
  public static void main(String args[]) {
    if(args.length < 1) {
      System.err.println("Please specify a password to hash!");
      System.exit(1);
    }
    try {
      int generatedSaltSize = 128;
      String algorithm = DefaultPasswordService.DEFAULT_HASH_ALGORITHM;
      int iterations = DefaultPasswordService.DEFAULT_HASH_ITERATIONS;
      String formatString = Shiro1CryptFormat.class.getName();

      char[] passwordToHash = args[0].toCharArray();

      SecureRandomNumberGenerator generator = new SecureRandomNumberGenerator();
      int byteSize = generatedSaltSize / 8;
      ByteSource salt = generator.nextBytes(byteSize);

      SimpleHash hash = new SimpleHash(algorithm, passwordToHash, salt, iterations);

      HashFormat format = new DefaultHashFormatFactory().getInstance(formatString);

      String output = format.format(hash);

      System.out.println(output);

    } catch (Throwable t) {
      System.err.println(t.getMessage());
      System.exit(1);
    }

  }
}