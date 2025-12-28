import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;

public class LazyConstantsDemo {
    public static void main(String[] args) throws Exception {
        banner("JEP 526 — Lazy Constants (Second Preview) — Simulation Demo");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();
        IO.println("Note: This demo simulates lazy constants behavior in plain Java.");
        IO.println("Real preview APIs require: --enable-preview on a supported JDK (see JEP 526).");
        hr();

        banner("Single-threaded: first access vs subsequent accesses");
        Lazy<String> EXPENSIVE = new Lazy<>(() -> {
            sleep(250);
            return "CONST-" + java.time.Instant.now();
        });
        long t1 = System.nanoTime();
        String a = EXPENSIVE.get();
        long t2 = System.nanoTime();
        String b = EXPENSIVE.get();
        long t3 = System.nanoTime();
        kv("first.value", a);
        kv("second.value", b);
        kv("first.call.ms", (t2 - t1) / 1_000_000);
        kv("second.call.ms", (t3 - t2) / 1_000_000);
        hr();

        banner("Multi-threaded: ensure single initialization under contention");
        Lazy<Integer> INIT_ONCE = new Lazy<>(() -> {
            IO.println("[init] Building heavy object...");
            sleep(200);
            return 42;
        });
        int threads = 6;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    start.await();
                    int v = INIT_ONCE.get();
                    System.out.printf("Thread-%d saw value: %d%n", id, v);
                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            }).start();
        }
        long s = System.nanoTime();
        start.countDown();
        done.await();
        long e = System.nanoTime();
        kv("threads", threads);
        kv("elapsed.ms(concurrent)", (e - s) / 1_000_000);
        hr();

        IO.println("Takeaways:");
        IO.println("- First access performs initialization; subsequent calls are fast.");
        IO.println("- Under contention, initialization occurs once, and all threads observe the same value.");
        IO.println("- Preview APIs in JEP 526 aim to standardize lazy constant semantics and performance benefits.");
        IO.println("  Compile/run preview samples with: --enable-preview");
        IO.println("  Link: https://openjdk.org/jeps/526");
    }

    // Simple thread-safe lazy holder with verbose tracing
    static final class Lazy<T> {
        private final java.util.function.Supplier<T> supplier;
        private final AtomicReference<T> ref = new AtomicReference<>();

        Lazy(java.util.function.Supplier<T> supplier) { this.supplier = supplier; }

        T get() {
            T val = ref.get();
            if (val != null) return val;
            synchronized (this) {
                val = ref.get();
                if (val == null) {
                    long t0 = System.nanoTime();
                    IO.println("[init] Computing constant...");
                    val = supplier.get();
                    long ms = (System.nanoTime() - t0) / 1_000_000;
                    IO.println("[init] Done in " + ms + " ms");
                    ref.set(val);
                }
                return val;
            }
        }
    }

    // Helpers
    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
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
