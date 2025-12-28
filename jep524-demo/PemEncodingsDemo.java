import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PemEncodingsDemo {
    private static final String SAMPLE_PEM = """
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwV6/1u4m3Jc1tW3bfb3T
rN4qvN6rAm0e3lLZz2sYvA2+H1kCj9rA3YzqkQ7e0WQb9o5lY2u8Gv0V36i6T1qS
J6o3N7f1mZbN6Yh9S0nH5vXx3J2wC6Q8qj+qfYwU1p8l4w8pT1Gd+U1wWZkY0x+X
pQIDAQAB
-----END PUBLIC KEY-----
""";

    public static void main(String[] args) throws Exception {
        banner("JEP 524 — PEM Encodings of Cryptographic Objects (Second Preview)");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();

        banner("Parse PEM-encoded RSA Public Key");
        String base64 = SAMPLE_PEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        kv("pem.base64.length", base64.length());
        byte[] der = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.US_ASCII));
        kv("der.bytes.length", der.length);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(spec);
        kv("publicKey.algorithm", key.getAlgorithm());
        kv("publicKey.format", key.getFormat());
        kv("publicKey.encoded.length", key.getEncoded().length);
        hr();

        IO.println("Notes:");
        IO.println("- JEP 524 makes PEM encodings first-class across JDK crypto APIs (preview).");
        IO.println("- For private keys: use PKCS8EncodedKeySpec, for certs: CertificateFactory, or new preview APIs per the JEP.");
        IO.println("- Link: https://openjdk.org/jeps/524");
    }

    // Pretty output helpers
    static void banner(String t) {
        String line = "#".repeat(Math.max(10, t.length() + 8));
        IO.println();
        IO.println(line);
        IO.println("###  " + t + "  ###");
        IO.println(line);
    }
    static void kv(String k, Object v) { System.out.printf("%-24s : %s%n", k, String.valueOf(v)); }
    static void hr() { IO.println("-".repeat(80)); }
}
