# Progress — java-notebooks

Last updated: 2025-12-28

Current Status Summary
- Memory Bank
  - Updated core docs: projectbrief.md, productContext.md, activeContext.md, systemPatterns.md, techContext.md, progress.md
  - .clinerules present at repo root to enforce Memory Bank usage
- Refactor Work
  - All `.java` sources refactored to use `IO.println(...)` instead of `System.out.println(...)`
  - Per-folder `IO.java` helper added where modifications occurred
  - Codemod scripts added/maintained under `scripts/`
- Notebooks (.ijnb)
  - Present and unchanged functionally; optional codemod available for code cells

Demos Snapshot (post-refactor)
- http3-demo (HTTP/3 for HttpClient, JDK 26)
  - Status: Ready to compile/run; README contains instructions
  - Build: `javac Http3Demo.java`
  - Run: `java Http3Demo https://www.cloudflare.com/`
  - Notes: Fallback to HTTP/2/1.1 if UDP/QUIC:443 blocked or HTTP/3 unavailable
  - Uses `IO.println` for println-style output
- jep500-demo (JEP 500 — Prepare to Make final mean final, JDK 26)
  - Status: Ready to compile/run; README covers flags and diagnostics
  - Build: `javac C.java`
  - Run (warn default): `java C`
  - Diagnostics: `java --illegal-final-field-mutation=debug C`
  - Deny mode: `java --illegal-final-field-mutation=deny C`
  - Enable for classpath code: `java --enable-final-field-mutation=ALL-UNNAMED C`
  - Uses `IO.println` for println-style output
- Other JEP demos (504, 516, 522, 524, 525, 526, 529, 530)
  - Updated to `IO.println` where applicable
  - Sample code shown inside strings/text blocks intentionally preserved

What Works
- Repository-wide println refactor for `.java` sources
  - Verification: `node scripts/refactor-java-println.js --check .` → 0 file(s) would be modified
  - Script skips strings, text blocks (`""" ... """`), char literals, comments, and `IO.java` files
- Spot compilation successful for representative demos:
  - `javac jep504-demo/RemoveAppletApiDemo.java`
  - `javac jep500-demo/C.java`
- Notebook codemod available for `.ijnb` code cells (optional)

Known Issues / Constraints
- `System.out.print` and `System.out.printf` are intentionally not modified
- Per-folder `IO.java` duplication is deliberate to keep demos self-contained
- HTTP/3: requires UDP/QUIC 443; otherwise protocol downgrade expected
- JEP 500: deep reflection may still require `--add-opens` in addition to JEP 500 flags

Refactor and Maintenance Scripts
- Java sources codemod: `scripts/refactor-java-println.js`
  - Dry-run: `node scripts/refactor-java-println.js --check .`
  - Apply:   `node scripts/refactor-java-println.js .`
- Notebook code cells codemod: `scripts/refactor-println.js`
  - Dry-run: `node scripts/refactor-println.js --check notebooks`
  - Apply:   `node scripts/refactor-println.js notebooks`

Next Steps (Short Term)
- Optional: Add CI sanity checks (JDK 21, JDK 26) to compile demos and run dry-run codemod
- Optional: Add pre-commit hook to warn if `System.out.println` is reintroduced
- Optional: Consider consolidating `IO.java` into a common package if cross-demo dependency is acceptable

Backlog / Future Enhancements
- Add more JEP demos (e.g., extended Loom features, structured concurrency with preview APIs)
- Provide minimal Gradle/Maven sample for one demo (if pedagogically valuable)
- Add quick-run scripts for demos to simplify execution

Decision Log (recent)
- Standardize all demos to use `IO.println` for println-style output
- Keep `IO.java` per demo folder to avoid cross-folder packages/imports
- Patch codemod to skip editing `IO.java` and skip literal/comment contexts
- Preserve `System.out.print/printf` for formatting/partial-line semantics

References
- Root README.md — prerequisites, notebook setup, and demo quick starts
- http3-demo/README.md — HTTP/3 opt-in, discovery/forcing, troubleshooting
- jep500-demo/README.md — mutation controls, diagnostics, and migration tips
- Scripts:
  - `scripts/refactor-java-println.js` (Java sources)
  - `scripts/refactor-println.js` (notebook code cells)
