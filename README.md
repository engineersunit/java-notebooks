# java-notebooks

Lightweight playground for exploring Java features with:
- Interactive Java notebooks (.ijnb) for quick iteration and inline outputs
- Tiny, self-contained demo programs showing specific JDK features/JEPs
- Concise docs focused on how to build/run and any required flags

Goals: rapid experiments, reproducible reference snippets, and clear, runnable demos.

## Repository structure

- Notebooks (root)
  - CircularDependency.ijnb — circular dependency explorations
  - Http3Notebook.ijnb — HTTP/3 exploration
  - VirtualThreadsNotebook.ijnb — Project Loom virtual threads
- Notebooks (organized)
  - notebooks/jdk26/… — JEP-focused notebooks for JDK 26
    - jep-500-final-mean-final/JEP500Notebook.ijnb
    - jep-504-remove-applet-api/JEP504Notebook.ijnb
    - jep-516-aot-object-cache/JEP516Notebook.ijnb
    - jep-517-http3/Http3Notebook.ijnb
    - jep-522-g1-throughput/JEP522Notebook.ijnb
    - jep-524-pem-encodings/JEP524Notebook.ijnb
    - jep-525-structured-concurrency/JEP525Notebook.ijnb
    - jep-526-lazy-constants/JEP526Notebook.ijnb
    - jep-529-vector-api/JEP529Notebook.ijnb
    - jep-530-primitive-patterns/JEP530Notebook.ijnb
- Demos (plain Java; each folder is self-contained)
  - http3-demo/ — JEP 517 (HTTP/3 for HttpClient, JDK 26)
    - Http3Demo.java
    - README.md
  - jep500-demo/ — JEP 500 (final-field mutation behavior, JDK 26)
    - C.java, args.txt
    - README.md
  - Additional minimal demos (compile/run via javac/java)
    - jep504-demo/RemoveAppletApiDemo.java
    - jep516-demo/AotObjectCachingDemo.java
    - jep522-demo/G1ThroughputDemo.java
    - jep524-demo/PemEncodingsDemo.java
    - jep525-demo/StructuredConcurrencyDemo.java
    - jep526-demo/LazyConstantsDemo.java
    - jep529-demo/VectorApiDemo.java
    - jep530-demo/PrimitivePatternsDemo.java
- images/
  - connections.png — supporting diagram
- scripts/
  - refactor-java-println.js — codemod for .java sources
  - refactor-println.js — codemod for .ijnb code cells

Refer to per-demo README files where present (e.g., `http3-demo/README.md`, `jep500-demo/README.md`) for details specific to those examples.

## Prerequisites

- Java Development Kit (JDK) on PATH
  - Recommended baseline: JDK 21+
  - Some demos require JDK 26 (e.g., HTTP/3, JEP 500 behavior changes)
- Verify installation:
  - `java -version`
  - `javac -version`
- Editor: any editor is fine. For notebooks, VS Code with the Oracle Java extension is recommended.

Optional tooling:
- Node.js (to run maintenance scripts in `scripts/`)
- Maven/Gradle only if you choose to convert demos to builds (not required here)
- Browser/cURL for networking demos

## Using the notebooks (.ijnb)

Interactive Java Notebooks are supported in VS Code via the Oracle Java extension:

- Install the extension:
  - VS Code Marketplace: Oracle "Java" (Oracle.oracle-java)
    https://marketplace.visualstudio.com/items?itemName=Oracle.oracle-java
- Usage:
  - Open a `.ijnb` file; run cells with the gutter Run buttons
  - Restart the Java notebook kernel as needed; outputs show inline
  - Command Palette: “Java: Create Java Notebook” to create a new notebook
- If your editor does not support `.ijnb`, copy code cells into a `.java` file and run with `javac`/`java`.

References:
- Inside Java: VS Code Java Notebooks — https://inside.java/2025/12/09/new-vscode-extension/
- VS Code extension — https://marketplace.visualstudio.com/items?itemName=Oracle.oracle-java
- Notebook details — https://github.com/oracle/javavscode/wiki/Interactive-Java-Notebooks

Using an Early Access (EA) JDK in VS Code (optional):
1) VS Code Settings: enable “Jdk › Advanced › Disable: Nbjavac”
2) Set “Jdk: Jdkhome” to your EA JDK (examples below)

Examples:
- macOS: `/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home`
- Windows: `C:\Program Files\Java\jdk-26`
- Linux: `/usr/lib/jvm/jdk-26`

```
"jdk.jdkhome": "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home",
"jdk.advanced.disable.nbjavac": true
```

Download EA builds: https://jdk.java.net/

## Running the demos (quick start)

Demos are plain Java by default. From repo root:

- HTTP/3 demo (JEP 517; JDK 26)
  ```
  cd http3-demo
  javac Http3Demo.java
  java Http3Demo
  ```
  Notes:
  - Prefers HTTP/3 when available; prints negotiated protocol
  - Falls back to HTTP/2/1.1 automatically (e.g., if UDP/QUIC 443 is blocked)
  - See `http3-demo/README.md` for details and flags

- JEP 500 demo (final-field mutation; JDK 26)
  ```
  cd jep500-demo
  javac C.java
  # Example run with explicit behavior controls:
  java \
    --enable-final-field-mutation=ALL-UNNAMED \
    --illegal-final-field-mutation=debug \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    C
  ```
  Notes:
  - `--illegal-final-field-mutation=warn|debug|deny` (JDK 26 default: warn; future likely: deny)
  - `--add-opens` may be required for deep reflection across modules
  - See `jep500-demo/README.md` for specifics and sample runs

Other demo folders follow the same pattern:
```
cd <demo-folder>
javac <Main>.java
java <Main>   # plus any required flags noted in that demo's README or comments
```

## Output convention: IO.println

All println-style output in demos goes through a tiny helper `IO` class:
- `IO.println()` and `IO.println(Object)`
- Each demo folder that emits output includes its own `IO.java` (keeps demos independent)
- We intentionally do NOT alter `System.out.print` or `System.out.printf` usages

A repository codemod refactors `System.out.println(...)` to `IO.println(...)`:
- Skips replacements inside string literals, text blocks (`""" ... """`), char literals, and comments
- Skips editing `IO.java` itself to avoid recursive rewrites

## Maintenance scripts

Requires Node.js on PATH.

- Dry-run (see what would change without modifying files):
  ```
  node scripts/refactor-java-println.js --check .
  node scripts/refactor-println.js --check notebooks
  ```
- Apply:
  ```
  node scripts/refactor-java-println.js .
  node scripts/refactor-println.js notebooks
  ```

## Troubleshooting

- HTTP/3 demo falls back to HTTP/2/1.1
  - Ensure the target server advertises HTTP/3 (Alt-Svc) and your network allows UDP/QUIC on port 443
  - JDK 26 provides discovery/forcing options; see JDK docs and the demo README

- JEP 500 mutations denied or warnings emitted
  - Adjust flags: `--enable-final-field-mutation=...` and `--illegal-final-field-mutation=allow|warn|debug|deny`
  - You may need `--add-opens` for deep reflection across modules
  - For diagnostics, try `--illegal-final-field-mutation=debug` or record JFR event `jdk.FinalFieldMutation`

- VS Code notebooks kernel issues
  - Verify JDK installations and the Oracle Java extension
  - If using EA JDKs, configure `jdk.jdkhome` and disable nbjavac (see above)

## Contributing / extending

- Add new notebooks or demos in their own folders
- Keep demos minimal and self-contained
- Include a short README in each demo folder with:
  - Purpose / what the demo illustrates
  - Minimum JDK version and prerequisites
  - How to build/run
  - Any special notes or flags

## License

Licensed under GNU General Public License v2.0 with the Classpath Exception (same as OpenJDK).
SPDX: GPL-2.0-only WITH Classpath-exception-2.0

See the LICENSE file for the full text.
