# Tech Context — java-notebooks

Last updated: 2025-12-28

Stack and Languages
- Language: Java (plain Java sources and interactive notebooks)
- JDK:
  - Recommended baseline: JDK 21+
  - Some demos target JDK 26 features (HTTP/3 in HttpClient, JEP 500 behavior)
- Build tooling: none required by default (use `javac`/`java`); optional Maven/Gradle may be added per-demo if helpful

Editor/IDE
- VS Code with Oracle Java extension for Interactive Java Notebooks (.ijnb)
  - Extension: Oracle "Java" (Oracle.oracle-java)
  - Notebook usage: open `.ijnb` files, run cells interactively
- Alternative editors: copy notebook cells to `.java` files and run via CLI

Environment Setup
- Verify Java installation:
  - `java -version`
  - `javac -version`
- Optional: configure EA JDK for VS Code notebooks
  - VS Code Settings
    - Jdk › Advanced › Disable: Nbjavac
    - Jdk: Jdkhome = path to JDK (e.g., `/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home`)
  - Example settings:
    ```
    "jdk.jdkhome": "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home",
    "jdk.advanced.disable.nbjavac": true
    ```

Key Libraries and APIs
- Standard library only; no external dependencies by default
- HTTP/3 demo uses `java.net.http.HttpClient`
  - Negotiated protocol introspection (HTTP_3 / HTTP_2 / HTTP_1_1)
  - Optional `Alt-Svc` handling per server advertising

Runtime Constraints and Flags
- HTTP/3 (JDK 26):
  - Network must allow UDP/QUIC on port 443 or the client will downgrade to HTTP/2/1.1
  - Request/Client can prefer HTTP/3; demo code may probe `HttpClient.Version.valueOf("HTTP_3")`
  - Advanced discovery/forcing options exist in JDK 26 (see JDK docs for `H3_DISCOVERY` / discovery modes)
- JEP 500 (final-field mutation semantics in JDK 26):
  - Behavior control flags:
    - `--enable-final-field-mutation=ALL-UNNAMED` (or named modules)
    - `--illegal-final-field-mutation=allow|warn|debug|deny` (JDK 26 default: warn; future likely: deny)
  - `--add-opens` may also be required for deep reflection across modules, but it is not sufficient alone to allow final field mutation
  - Diagnostics:
    - `--illegal-final-field-mutation=debug` to print stack traces
    - JFR event: `jdk.FinalFieldMutation` (record with `-XX:StartFlightRecording`)

Conventions and Practices
- Runnable-first: prefer single-file or minimal multi-file demos, runnable via CLI
- Explicit requirements: each demo README states minimum JDK version and necessary flags
- Portability: commands are POSIX-friendly; note platform-specific deviations where applicable
- Isolation: each demo folder is self-contained; notebooks are exploratory and optional
- Output abstraction: demos should use `IO.println(...)` instead of `System.out.println(...)`
  - Do not alter `System.out.print` or `System.out.printf` usages
  - Preserve occurrences in strings, text blocks (`""" ... """`), char literals, and comments (tutorial snippets)

Maintenance Scripts
- Java sources codemod: `scripts/refactor-java-println.js`
  - Purpose: replace `System.out.println(...)` with `IO.println(...)` across `.java` files
  - Behavior:
    - Skips replacements inside string literals, text blocks, char literals, and comments
    - Skips editing `IO.java` to avoid recursive rewrites
    - Creates a minimal per-folder `IO.java` if a directory had modifications
  - Usage:
    - Dry run: `node scripts/refactor-java-println.js --check .`
    - Apply:   `node scripts/refactor-java-println.js .`
- Notebook codemod: `scripts/refactor-println.js`
  - Purpose: replace `System.out.println(...)` with `IO.println(...)` in `.ijnb` code cells only
  - Usage examples:
    - Dry run: `node scripts/refactor-println.js --check notebooks`
    - Apply:   `node scripts/refactor-println.js notebooks`

Requirements for Scripts
- Node.js available on PATH to run the maintenance scripts
- Run dry-run (`--check`) first to review changes, then apply

IO Helper Class
- Per-demo duplication for independence (no cross-folder dependencies)
- Minimal implementation:
  - `public static void println()`
  - `public static void println(Object o)`
- Lives alongside demo sources in each affected folder

References
- Root README.md for prerequisites, notebook usage, and quick starts
- Per-demo README.md files for focused instructions and flags
- VS Code Oracle Java extension documentation and Inside Java resources
- Scripts:
  - `scripts/refactor-java-println.js` (Java sources)
  - `scripts/refactor-println.js` (notebook code cells)
