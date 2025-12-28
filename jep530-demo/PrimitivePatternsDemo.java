public class PrimitivePatternsDemo {
    public static void main(String[] args) {
        banner("JEP 530 — Primitive Types in Patterns, instanceof, and switch (Fourth Preview)");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();
        IO.println("Note: This feature is PREVIEW in JDK 26. To compile/run preview code, use --enable-preview.");
        IO.println("Examples and commands are shown below.");
        hr();

        banner("Baseline (non-preview): pattern matching for reference types");
        Object any = Math.random() > 0.5 ? Integer.valueOf(42) : Double.valueOf(3.14159);
        if (any instanceof Integer i) {
            kv("kind", "Integer");
            kv("value", i);
        } else if (any instanceof Double d) {
            kv("kind", "Double");
            kv("value", d);
        } else {
            kv("kind", any.getClass().getName());
            kv("value", any);
        }
        hr();

        banner("Preview snippets: primitive patterns in instanceof and switch");
        IO.println("""
            // Save as Demo.java, compile/run with preview:
            //   javac --enable-preview --release 26 Demo.java
            //   java  --enable-preview Demo

            public class Demo {
                public static void main(String[] args) {
                    Object o = Math.random() > 0.5 ? 123 : 3.14;

                    // instanceof with primitive patterns
                    if (o instanceof int i) {
                        System.out.println("int pattern matched: " + i);
                    } else if (o instanceof double d) {
                        System.out.println("double pattern matched: " + d);
                    }

                    // switch with primitive patterns and guards
                    Object v = (System.currentTimeMillis() & 1) == 0 ? 0 : 256;
                    String result = switch (v) {
                        case int i when i == 0 -> "zero (int)";
                        case int i when i > 0 && i < 10 -> "small positive int";
                        case int i -> "other int: " + i;
                        case double d when d == 0.0 -> "zero (double)";
                        default -> "unhandled type: " + v;
                    };
                    System.out.println(result);
                }
            }
            """);
        hr();

        IO.println("Summary:");
        IO.println("- JEP 530 extends patterns to primitive types for instanceof and switch.");
        IO.println("- Improves clarity and reduces boilerplate when dealing with Object-typed primitives.");
        IO.println("- Requires --enable-preview on a supported JDK (26). See commands above.");
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
