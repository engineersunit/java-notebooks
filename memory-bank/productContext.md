# Product Context — java-notebooks

Last updated: 2025-12-25

Purpose
- Provide a lightweight playground to explore Java features using:
  - Interactive Java notebooks (.ijnb) for quick iteration and inline outputs.
  - Minimal standalone demos to illustrate specific JEPs/features.
- Serve as a curated reference of runnable snippets for future reuse.

Problems This Solves
- Reduces setup friction for experimenting with new JDK features (e.g., JDK 26 additions).
- Captures knowledge in runnable form rather than ad-hoc notes.
- Demonstrates practical, minimal examples without heavy build systems.
- Documents nuanced runtime flags and environment requirements (e.g., HTTP/3, final-field mutation).

How It Should Work
- Each demo lives in its own small folder with:
  - One or few Java files.
  - A concise README that includes: purpose, prerequisites (JDK version), how to compile/run, and any flags.
- Notebooks (.ijnb) can be opened in VS Code with the Oracle Java extension:
  - Run cells interactively, view outputs inline, and iterate quickly.
- Demos compile and run with javac/java by default (no external dependencies).
- Where demos target new JDK features, they should:
  - Clearly state the minimum JDK version.
  - Degrade gracefully or detect feature presence dynamically if possible.

Users & Experience Goals
- Repo owner and collaborators experimenting with Java features:
  - Fast on-ramp: open a notebook or cd into a demo and run it.
  - Clear, minimal instructions in each README.
- Broader Java developers seeking minimal examples:
  - Consistent folder structure and documentation.
  - Runnable code without mandatory build tools.

Value Proposition
- A single place to try modern Java/JEPs with the least ceremony.
- Encourages accurate, concise documentation alongside runnable code.
- Supports both interactive learning (notebooks) and CLI-centric demos.

Current Content Snapshot
- Notebooks:
  - CircularDependency.ijnb — circular dependency explorations.
  - Http3Notebook.ijnb — HTTP/3 exploration.
  - VirtualThreadsNotebook.ijnb — Project Loom virtual threads exploration.
- Demos:
  - http3-demo (JEP 517 — HTTP/3 in HttpClient, JDK 26): prefers HTTP/3, resilient fallback to HTTP/2/1.1; prints negotiated protocol.
  - jep500-demo (JEP 500 — Prepare to Make final mean final, JDK 26): demonstrates deep-reflection mutation of final fields and runtime controls.

Constraints & Considerations
- Some features require JDK 26+ (HTTP/3, JEP 500 behavior).
- Networking constraints (UDP/QUIC 443) can force HTTP/2/1.1 fallback.
- Reflection-based mutation demonstrations may require both --add-opens and the new JEP 500 flags.
- Editors without .ijnb support can copy notebook code into .java files.

Success Indicators
- Demos and notebooks are runnable with minimal commands.
- Each demo’s README clearly communicates requirements and flags.
- New examples can be added without reorganizing the repository.
