# JEP 500 Demo — Prepare to Make final mean final (JDK 26)

This demo shows how JEP 500 behaves when code uses deep reflection to mutate final fields, and how to control that behavior with the new runtime options.

What this JEP changes
- In JDK 26, mutating a final field via deep reflection triggers a warning by default.
- A future JDK release will deny such mutations by default (throwing IllegalAccessException).
- You can explicitly enable final-field mutation for selected code to silence warnings now and keep working when denial becomes the default.

Prerequisites
- JDK 26 or newer on your PATH (java and javac).
- macOS/Linux examples shown; adapt for Windows as needed.

Contents
- C.java — minimal reflective mutation example.

Quick start
1) Navigate to this demo folder:
   cd jep500-demo

2) Compile:
   javac C.java

3) Run (default mode in JDK 26 is warn; mutation succeeds with a warning):
   java C

4) See stack traces for each illegal mutation:
   java --illegal-final-field-mutation=debug C

5) Simulate the future default (deny — throws IllegalAccessException):
   java --illegal-final-field-mutation=deny C

6) Allow classpath code (unnamed module) without warnings:
   java --enable-final-field-mutation=ALL-UNNAMED C

Details

Runtime options you’ll use
- Enable mutation for selected modules/code:
  - Classpath/unnamed module:
    java --enable-final-field-mutation=ALL-UNNAMED ...
  - Specific named modules on the module path:
    java --enable-final-field-mutation=M1,M2 ...
- Control behavior for illegal mutations:
  - java --illegal-final-field-mutation=allow | warn | debug | deny
  - JDK 26 default: warn. Future default: deny.

Important: --add-opens may still be required for deep reflection to access private members across modules. But note:
- --add-opens alone is not enough to permit mutation of final fields under JEP 500.
- You must also enable final-field mutation for the mutating module (or ALL-UNNAMED for classpath code).

Detect where final-field mutation happens
- Quick diagnostic with stack traces:
  java --illegal-final-field-mutation=debug ...
- JFR event recording:
  java -XX:StartFlightRecording:filename=recording.jfr C
  jfr print --events jdk.FinalFieldMutation recording.jfr

Using environment variable or argument files
- Environment variable (applies to all invocations in this shell):
  export JDK_JAVA_OPTIONS="--enable-final-field-mutation=ALL-UNNAMED --illegal-final-field-mutation=warn"
- Argument file:
  Create args.txt with one option per line, then run:
  java @args.txt C

Serialization libraries
- Prefer sun.reflect.ReflectionFactory for Serializable classes to avoid asking users to enable final-field mutation.
- Non-Serializable classes should not rely on mutating final fields; redesign APIs to avoid it.

Migration tips
- Avoid reflection-based mutation of final fields; prefer constructor injection, builders, or setters for test seams.
- For clone, instantiate via constructors instead of mutating finals reflectively.
- Keep CI periodically running with: --illegal-final-field-mutation=deny to catch offenders early.

Notes and constraints
- --enable-final-field-mutation applies only to the boot module layer; user-defined layers can’t be enabled this way.
- addOpens at runtime (Module::addOpens, ModuleLayer.Controller::addOpens, Instrumentation::redefineModule) won’t “smuggle in” mutation if it wasn’t enabled at startup.
- System.in/out/err remain writable only via System.setIn/Out/Err (unchanged).

Troubleshooting
- If you see no warning in JDK 26, ensure you are on the expected Java version: java -version.
- If you get InaccessibleObjectException, you may need --add-opens in addition to enabling mutation (for cross-module deep reflection).

License
- This demo is for educational purposes.
