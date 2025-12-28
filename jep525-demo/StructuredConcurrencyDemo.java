public class StructuredConcurrencyDemo {
    public static void main(String[] args) {
        banner("JEP 525 — Structured Concurrency (Sixth Preview)");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();

        banner("Preview API detection (StructuredTaskScope)");
        boolean hasStructuredScope;
        try {
            Class.forName("java.util.concurrent.StructuredTaskScope");
            hasStructuredScope = true;
        } catch (Throwable t) {
            hasStructuredScope = false;
        }
        kv("StructuredTaskScope.present", hasStructuredScope);
        if (!hasStructuredScope) {
            IO.println("Note: Structured Concurrency is a preview API in JDK 26.");
            IO.println("To run real samples, compile/run with --enable-preview on a supported JDK.");
            IO.println("Example:");
            IO.println("  javac --enable-preview --release 26 Demo.java");
            IO.println("  java  --enable-preview Demo");
        }
        hr();

        banner("Runnable simulation using CompletableFuture + virtual threads");
        java.util.concurrent.Executor exec = r -> java.lang.Thread.ofVirtual().start(r);
        long t0 = System.nanoTime();
        var f1 = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return "alpha";
        }, exec);
        var f2 = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return "beta";
        }, exec);
        var f3 = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            sleep(400);
            return "gamma";
        }, exec);

        // Fail-fast race: complete with the first successful result
        var any = f1.applyToEither(f2, s -> s).applyToEither(f3, s -> s);
        String winner = any.join();
        long t1 = System.nanoTime();
        kv("winner", winner);
        kv("elapsed.ms", (t1 - t0) / 1_000_000);

        // Cooperative cancellation demo: cancel others after winner
        f1.cancel(true);
        f2.cancel(true);
        f3.cancel(true);
        hr();
        IO.println("This simulates key ideas: scope-like join of subtasks, fast result, and cancellation of losers.");

        hr();
        IO.println("To try the preview API, create a Demo.java like:");
        IO.println("--------------------------------------------------");
        IO.println("""
            // javac --enable-preview --release 26 Demo.java
            // java  --enable-preview Demo
            import java.util.concurrent.StructuredTaskScope;

            public class Demo {
                public static void main(String[] args) throws Exception {
                    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
                        var f1 = scope.fork(() -> { Thread.sleep(300); return "alpha"; });
                        var f2 = scope.fork(() -> { Thread.sleep(200); return "beta";  });
                        var f3 = scope.fork(() -> { Thread.sleep(400); return "gamma"; });
                        String result = scope.join().result();
                        System.out.println("winner = " + result);
                    }
                }
            }
        """);
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
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
