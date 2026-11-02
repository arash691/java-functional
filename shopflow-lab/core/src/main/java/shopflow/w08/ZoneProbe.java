package shopflow.w08;

import java.nio.charset.Charset;
import java.time.ZoneId;

/** Prints the JVM's default zone and charset. Run it with different {@code -Duser.timezone=...} values. */
public class ZoneProbe {
    public static void main(String[] args) {
        System.out.println("default zone   : " + ZoneId.systemDefault());
        System.out.println("default charset: " + Charset.defaultCharset());
    }
}
