# Active Context — java-notebooks

Last updated: 2025-12-28

Current Focus
- Refactor all Java demo sources to use IO.println instead of System.out.println.
- Maintain codemod scripts to keep demos and notebooks consistent.
- Keep Memory Bank up to date so future sessions retain full context.

Recent Changes
- Implemented a repository-wide refactor:
  - Added scripts/refactor-java-println.js to replace System.out.println(...) with IO.println(...) in .java files, skipping occurrences inside string literals, text blocks ("""), char literals, and comments.
  - Auto-created an IO.java helper in each affected demo directory with:
    - public static void println()
    - public static void println(Object)
  - Left System.out.print and System.out.printf untouched.
  - Verified by search that remaining System.out.println occurrences are only inside newly created IO.java files (by design) or preserved within string/text-block samples in demos (e.g., sample code shown to users).
- Safety improvement:
  - Patched the refactor script to skip modifying any IO.java file, preventing recursive edits.
  - Re-ran dry-run to confirm 0 files would be changed post-refactor.
- Spot compilation:
  - Compiled representative demos (e.g., jep504-demo/RemoveAppletApiDemo.java, jep500-demo/C.java) to ensure no regressions.

Next Steps
- Decide whether to:
  - Keep per-demo IO.java helpers (current: maximizes independence, zero cross-folder dependencies), or
  - Consolidate into a shared IO class in a common package (would require package structure and import updates).
- Optional:
  - Add a lightweight CI/lint step to fail builds if System.out.println is reintroduced outside IO.java.
  - Extend codemod coverage to notebooks (.ijnb) via existing scripts/refactor-println.js (code cells) in routine maintenance.
  - Add a pre-commit hook that runs the dry-run and warns on violations.

Active Decisions and Considerations
- Demos remain self-contained; per-folder IO.java avoids inter-folder imports and package coupling.
- Purposely do not alter System.out.print / printf to avoid impacting formatted output or partial-line logging patterns.
- Preserve literal examples: strings and text blocks that show tutorial code should not be rewritten.

Important Patterns and Preferences
- IO.println abstraction for output in demos, implemented via a tiny static helper class.
- Codemod-first maintenance:
  - scripts/refactor-java-println.js for .java sources
  - scripts/refactor-println.js for .ijnb notebooks (code cells only)
- Runnable-first: demos remain plain javac/java without external build tooling by default.

Learnings and Insights
- Text blocks and embedded tutorial strings are common in these demos; a tokenizer-based replacement (not raw regex) avoids corrupting sample code.
- Skipping IO.java in codemods prevents refactoring churn and retains the helper’s canonical form.

Open Questions / Future Clarifications
- Should we introduce a common package (e.g., util.IO) and a minimal classpath setup to share IO across demos, or keep per-folder duplication for maximal independence?
- Add CI targets for JDK 21 and JDK 26 to validate compilation and basic runs for representative demos?
