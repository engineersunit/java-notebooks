public class RemoveAppletApiDemo {
    public static void main(String[] args) {
        banner("JEP 504 — Remove the Applet API");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();

        banner("Runtime probe for java.applet.Applet");
        try {
            Class.forName("java.applet.Applet");
            IO.println("Found java.applet.Applet (unexpected on JDK 26)");
        } catch (ClassNotFoundException e) {
            IO.println("java.applet.Applet is removed as expected on JDK 26");
        }
        hr();

        banner("Compile-time probe suggestion");
        IO.println("Create a file AppletProbe.java containing:");
        IO.println("  import java.applet.Applet;");
        IO.println("  public class AppletProbe extends Applet {}");
        IO.println("Then run: javac AppletProbe.java");
        IO.println("On JDK 26 you should see: package java.applet does not exist");
        hr();

        IO.println("Run this demo:");
        IO.println("  javac RemoveAppletApiDemo.java");
        IO.println("  java RemoveAppletApiDemo");
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
