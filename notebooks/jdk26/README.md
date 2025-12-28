# JDK 26 Notebooks (JEPs)

Interactive Java notebooks (.ijnb) for JDK 26 features, organized per JEP. Open these in VS Code with the Oracle Java extension and run cells interactively.

How to use
- Ensure JDK 26 (or EA) is selected in VS Code for notebooks.
- Open any .ijnb below and run cells.
- Some features are Preview/Incubator and require flags when compiling/running as .java:
  - Preview: --enable-preview
  - Incubator module (Vector API): --add-modules jdk.incubator.vector

Notebooks

- JEP 517 — HTTP/3 for the HTTP Client API
  - notebooks/jdk26/jep-517-http3/Http3Notebook.ijnb
  - Shows preferring HTTP/3 using java.net.http.HttpClient, with environment checks and graceful fallback to HTTP/2/HTTP/1.1.

- JEP 500 — Prepare to Make Final Mean Final
  - notebooks/jdk26/jep-500-final-mean-final/JEP500Notebook.ijnb
  - Demonstrates reflective mutation attempts on final fields and points to runtime flags and diagnostics.

- JEP 504 — Remove the Applet API
  - notebooks/jdk26/jep-504-remove-applet-api/JEP504Notebook.ijnb
  - Probes for removal of java.applet.Applet and includes compile-time guidance.

- JEP 516 — Ahead-of-Time Object Caching with Any GC
  - notebooks/jdk26/jep-516-aot-object-cache/JEP516Notebook.ijnb
  - Provides a stable workload and guidance to compare cold/warm runs under different JVM configurations.

- JEP 522 — G1 GC: Improve Throughput by Reducing Synchronization
  - notebooks/jdk26/jep-522-g1-throughput/JEP522Notebook.ijnb
  - Allocation stressor to observe GC throughput; pair with -Xlog:gc from CLI.

- JEP 524 — PEM Encodings of Cryptographic Objects (Second Preview)
  - notebooks/jdk26/jep-524-pem-encodings/JEP524Notebook.ijnb
  - Parses a PEM public key to a PublicKey, with notes about preview API scope.

- JEP 525 — Structured Concurrency (Sixth Preview)
  - notebooks/jdk26/jep-525-structured-concurrency/JEP525Notebook.ijnb
  - Detects StructuredTaskScope presence and simulates behavior using CompletableFuture + virtual threads.

- JEP 526 — Lazy Constants (Second Preview)
  - notebooks/jdk26/jep-526-lazy-constants/JEP526Notebook.ijnb
  - Simulates lazy constants with verbose tracing; real APIs require --enable-preview.

- JEP 529 — Vector API (Eleventh Incubator)
  - notebooks/jdk26/jep-529-vector-api/JEP529Notebook.ijnb
  - Detects incubator module, includes scalar baseline and vectorized code snippet (requires --add-modules jdk.incubator.vector).

- JEP 530 — Primitive Types in Patterns, instanceof, and switch (Fourth Preview)
  - notebooks/jdk26/jep-530-primitive-patterns/JEP530Notebook.ijnb
  - Explains preview usage with snippets to compile/run with --enable-preview.

Notes
- For HTTP/3 details, also see http3-demo/ and the root Http3Notebook.ijnb (legacy location). This folder is the canonical home for JDK 26 notebooks going forward.
- Each notebook embeds a NotebookUtils helper (banners, sections, hr, kv) to produce engaging output with clear headers and key-value summaries.
