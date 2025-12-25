# Progress — java-notebooks

Last updated: 2025-12-25

Current Status Summary
- Memory Bank
  - Initialized core docs: projectbrief.md, productContext.md, activeContext.md, systemPatterns.md, techContext.md
  - .clinerules pending
- Notebooks (.ijnb)
  - Present in repo: CircularDependency.ijnb, Http3Notebook.ijnb, VirtualThreadsNotebook.ijnb
  - Usage via Oracle Java VS Code extension; no CI/automated checks
- Demos
  - http3-demo (HTTP/3 for HttpClient, JDK 26)
    - Status: Ready to compile/run; README contains instructions and troubleshooting
    - Build: javac Http3Demo.java
    - Run: java Http3Demo https://www.cloudflare.com/
    - Notes: Will fall back to HTTP/2/1.1 if UDP/QUIC:443 blocked or HTTP/3 unavailable
  - jep500-demo (JEP 500 — Prepare to Make final mean final, JDK 26)
    - Status: Ready to compile/run; README covers flags and diagnostics
    - Build: javac C.java
    - Run (warn default): java C
    - Diagnostics: java --illegal-final-field-mutation=debug C
    - Deny mode: java --illegal-final-field-mutation=deny C
    - Enable for classpath code: java --enable-final-field-mutation=ALL-UNNAMED C

What Works
- Plain Java compilation and execution (no build tool required) for both demos
- Documentation patterns established per demo folder
- Notebook workflow documented in root README (with extension references)

Known Issues / Constraints
- HTTP/3 demo:
  - Requires network allowing UDP/QUIC on port 443; otherwise protocol will downgrade
  - JDK 26+ recommended to expose HttpClient.Version.HTTP_3; dynamic probing used for resilience
- JEP 500 demo:
  - On deep reflection, may require --add-opens in addition to JEP 500 flags
  - Future JDK default likely deny; keep testing with --illegal-final-field-mutation=deny periodically
- Notebooks:
  - Require Oracle Java VS Code extension; alternative editors must copy cells into .java files

Next Steps (Short Term)
- Add .clinerules with Cline Memory Bank instructions to enforce Memory Bank reads at task start
- Validate http3-demo against multiple endpoints (Cloudflare, Google) and capture typical outputs
- Expand systemPatterns.md with code references once additional demos land
- Consider small CI job (matrix: JDK 21, 26 EA where applicable) to sanity-check compilation/runtime

Backlog / Future Enhancements
- Add more JEP demos (e.g., Loom APIs beyond basic virtual threads, structured concurrency examples)
- Optional minimal Gradle/Maven sample for one demo to illustrate build-based workflows
- Add quick-run scripts (bash/powershell) for demos to reduce user keystrokes
- Capture “gotchas” discovered during manual runs into productContext.md and techContext.md

Decision Log (recent)
- Keep repository runnable-first and build-tool-optional
- Prefer dynamic feature detection for JDK-version-sensitive APIs
- Document exact JVM flags prominently for demos relying on runtime controls

References
- Root README.md — prerequisites, notebook setup, and demo quick starts
- http3-demo/README.md — HTTP/3 opt-in, discovery/forcing, troubleshooting
- jep500-demo/README.md — mutation controls, diagnostics, and migration tips
