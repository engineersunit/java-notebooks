public class AotObjectCachingDemo {
    public static void main(String[] args) {
        banner("JEP 516 — Ahead-of-Time Object Caching with Any GC (exploratory)");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();

        banner("Baseline mixed workload (allocation + compute)");
        record Person(String first, String last, int age) {}
        var data = new java.util.ArrayList<Person>(100_000);
        long t0 = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            data.add(new Person("First" + i, "Last" + i, 20 + (i % 50)));
        }
        long allocMs = (System.nanoTime() - t0) / 1_000_000;
        kv("allocated.records", data.size());
        kv("alloc.elapsed.ms", allocMs);

        long t2 = System.nanoTime();
        var stats = data.stream().collect(java.util.stream.Collectors.summarizingInt(Person::age));
        long computeMs = (System.nanoTime() - t2) / 1_000_000;
        kv("age.min", stats.getMin());
        kv("age.max", stats.getMax());
        kv("age.avg", String.format("%.2f", stats.getAverage()));
        kv("compute.elapsed.ms", computeMs);
        hr();

        banner("Repeat small workload to observe warm/hot behavior");
        int runs = 5;
        long best = Long.MAX_VALUE, worst = Long.MIN_VALUE, sum = 0;
        for (int r = 1; r <= runs; r++) {
            long s = System.nanoTime();
            int[] arr = new int[100_000];
            for (int i = 0; i < arr.length; i++) arr[i] = i % 17;
            long acc = 0;
            for (int v : arr) acc += v;
            var sb = new StringBuilder();
            for (int i = 0; i < 10_000; i++) sb.append('x');
            long ms = (System.nanoTime() - s) / 1_000_000;
            kv("run.ms[" + r + "]", ms);
            best = Math.min(best, ms);
            worst = Math.max(worst, ms);
            sum += ms;
        }
        hr();
        kv("repeat.best.ms", best);
        kv("repeat.worst.ms", worst);
        kv("repeat.avg.ms", sum / (double) runs);
        hr();

        IO.println("How to use this for A/B comparisons:");
        IO.println("- Run this exact program with different JVM flags/configurations relevant to JEP 516.");
        IO.println("- Collect wall-clock times, GC logs (-Xlog:gc), and compare cold vs warm behavior.");
        IO.println("Reference: https://openjdk.org/jeps/516");
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
