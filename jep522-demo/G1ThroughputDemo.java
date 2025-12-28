import java.util.ArrayList;
import java.util.List;

public class G1ThroughputDemo {
    public static void main(String[] args) {
        banner("JEP 522 — G1 GC: Improve Throughput by Reducing Synchronization");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();

        banner("Allocation stressor to trigger GC activity");
        int blocks = argOrDefault(args, 0, 1_000);   // number of blocks to allocate
        int blockSize = argOrDefault(args, 1, 512_000); // bytes per block (~0.5 MB default)
        kv("blocks", blocks);
        kv("blockSize(bytes)", blockSize);
        hr();

        long startNs = System.nanoTime();
        List<byte[]> sink = new ArrayList<>(blocks);
        long bytes = 0;
        for (int i = 1; i <= blocks; i++) {
            sink.add(new byte[blockSize]);
            bytes += blockSize;
            if (i % 100 == 0) {
                kv("allocated.blocks", i);
                kv("allocated.MB", bytes / (1024 * 1024));
            }
        }
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        hr();
        kv("elapsed.ms", elapsedMs);
        kv("kept.blocks", sink.size());
        kv("kept.MB", bytes / (1024 * 1024));
        hr();

        IO.println("Tips:");
        IO.println("- Run with GC logs to observe behavior: java -Xlog:gc G1ThroughputDemo");
        IO.println("- Adjust heap size to amplify effects: java -Xms512m -Xmx512m -Xlog:gc G1ThroughputDemo 1500 1048576");
        IO.println("- Compare across JDK builds and configurations for throughput differences.");
    }

    static int argOrDefault(String[] args, int idx, int def) {
        try {
            return (idx < args.length) ? Integer.parseInt(args[idx]) : def;
        } catch (Exception e) {
            return def;
        }
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
