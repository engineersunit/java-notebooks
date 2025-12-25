# Tech Context — java-notebooks

Last updated: 2025-12-25

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

References
- Root README.md for prerequisites, notebook usage, and quick starts
- Per-demo README.md files for focused instructions and flags
- VS Code Oracle Java extension documentation and Inside Java resources
