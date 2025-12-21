# java-notebooks

A collection of Java notebooks and small demo programs. This repo is intended as a playground for exploring Java features, quick experiments, and reference snippets. It includes:
- IDE notebooks (.ijnb) for interactive exploration
- Standalone demo programs under subfolders
- Images and notes to accompany examples

## Repository structure

- CircularDependency.ijnb — Notebook exploring circular dependency concepts
- Http3Notebook.ijnb — Notebook exploring HTTP/3 concepts
- VirtualThreadsNotebook.ijnb — Notebook exploring virtual threads (Project Loom)
- http3-demo/ — Minimal HTTP/3 demo
  - Http3Demo.java
  - README.md
- jep500-demo/ — Demo related to JEP 500 (or other feature as noted inside)
  - C.java
  - args.txt
  - README.md
- images/ — Supporting diagrams and images

Refer to per-demo README files (e.g., `http3-demo/README.md`, `jep500-demo/README.md`) for details specific to each example.

## Prerequisites

- Java Development Kit (JDK) installed and on PATH
  - JDK 21+ recommended for modern features (e.g., virtual threads)
- An IDE or editor of your choice (IntelliJ IDEA, VS Code, etc.)

Optional tooling depending on the example:
- Maven/Gradle if you convert demos to builds
- Browser/cURL for networking demos

## Using the notebooks (.ijnb)

The `.ijnb` files are IDE notebook-style documents. You can:
- Open them directly in IDEs/editors that support Java notebooks or code cells.
- Or copy/paste code snippets into a standard `.java` file to compile/run as needed.

Tip: If your editor does not natively support `.ijnb`, you can still use them as structured notes and source snippets.

## Running the demos (quick start)

These demos are kept plain Java for simplicity. From the repo root:

- HTTP/3 demo
  - cd http3-demo
  - javac Http3Demo.java
  - java Http3Demo
  - See http3-demo/README.md for any extra flags or runtime notes.

- JEP 500 demo
  - cd jep500-demo
  - javac C.java
  - java C
  - See jep500-demo/README.md for details and any input/args guidance (e.g., args.txt).

If a demo has additional setup or requires specific JDK flags, it will be documented in that demo’s README.

## Git housekeeping

This repository includes a comprehensive, catch‑all `.gitignore` that:
- Covers common OS artifacts (macOS, Windows, Linux)
- Excludes files from popular IDEs/editors
- Ignores common build outputs and dependency caches across many languages and tools

You can prune the `.gitignore` over time to better fit the exact technologies used in this repo.

## Contributing / extending

- Add new notebooks or demos in their own folders.
- Keep demos minimal and self-contained when possible.
- Include a short README in each demo folder with:
  - Purpose / what the demo illustrates
  - Prerequisites or JDK version requirements
  - How to build/run
  - Any special notes or flags

## License

Add your preferred license (e.g., MIT, Apache 2.0) to a `LICENSE` file in the repo root if you plan to share or distribute.
