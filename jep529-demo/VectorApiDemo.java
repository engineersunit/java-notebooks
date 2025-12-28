/*
To compile and run with the Vector API incubator module:
  javac --add-modules jdk.incubator.vector VectorApiDemo.java
  java  --add-modules jdk.incubator.vector VectorApiDemo
*/
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

public class VectorApiDemo {
    public static void main(String[] args) {
        banner("JEP 529 — Vector API (Eleventh Incubator)");
        kv("java.version", System.getProperty("java.version"));
        kv("java.vendor", System.getProperty("java.vendor"));
        kv("os.name", System.getProperty("os.name"));
        hr();
        IO.println("Note: This demo requires the incubator module jdk.incubator.vector.");
        IO.println("Run with --add-modules jdk.incubator.vector (see header).");
        hr();

        int n = argOrDefault(args, 0, 1_000_000);
        int[] a = new int[n], b = new int[n], cScalar = new int[n], cVector = new int[n];
        for (int i = 0; i < n; i++) { a[i] = i; b[i] = n - i; }

        // Scalar baseline
        banner("Scalar addition baseline");
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) cScalar[i] = a[i] + b[i];
        long scalarMs = (System.nanoTime() - t0) / 1_000_000;
        kv("length", n);
        kv("scalar.ms", scalarMs);
        kv("scalar.sample.c[12345]", cScalar[Math.min(12_345, n - 1)]);
        hr();

        // Vectorized addition using preferred species
        banner("Vectorized addition using IntVector.SPECIES_PREFERRED");
        VectorSpecies<Integer> SPEC = IntVector.SPECIES_PREFERRED;
        kv("species.length", SPEC.length());
        kv("species", SPEC.toString());
        long t1 = System.nanoTime();
        int i = 0;
        int upperBound = SPEC.loopBound(n); // largest multiple of species length <= n
        for (; i < upperBound; i += SPEC.length()) {
            var va = IntVector.fromArray(SPEC, a, i);
            var vb = IntVector.fromArray(SPEC, b, i);
            var vc = va.add(vb);
            vc.intoArray(cVector, i);
        }
        // Tail
        for (; i < n; i++) cVector[i] = a[i] + b[i];
        long vectorMs = (System.nanoTime() - t1) / 1_000_000;
        kv("vector.ms", vectorMs);
        kv("vector.sample.c[12345]", cVector[Math.min(12_345, n - 1)]);
        kv("speedup.scalar/vector", String.format("%.2fx", (vectorMs == 0 ? Double.POSITIVE_INFINITY : (scalarMs * 1.0 / vectorMs))));
        hr();

        // Quick validation
        banner("Validation");
        boolean ok = true;
        for (int j = 0; j < n; j += Math.max(1, n / 10)) {
            if (cScalar[j] != cVector[j]) { ok = false; break; }
        }
        kv("results.equal(sampled)", ok);
        IO.println("Tip: vary array length (first CLI arg) and compare timings.");
        IO.println("     e.g., java --add-modules jdk.incubator.vector VectorApiDemo 5000000");
    }

    static int argOrDefault(String[] args, int idx, int def) {
        try { return (idx < args.length) ? Integer.parseInt(args[idx]) : def; } catch (Exception e) { return def; }
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
