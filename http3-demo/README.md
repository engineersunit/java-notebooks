# JEP 517 Demo — HTTP/3 for the HTTP Client API (JDK 26)

This demo shows how to opt in to HTTP/3 using the standard `java.net.http.HttpClient` and how to observe the negotiated protocol.

What this JEP provides
- Client-side HTTP/3 support in the standard `HttpClient` API.
- Opt-in: you must request HTTP/3 (it is not the default).
- Transparent downgrade to HTTP/2/1.1 when HTTP/3 is not available (unless you force HTTP/3-only via advanced options).

Prerequisites
- JDK 26+ recommended (adds `HttpClient.Version.HTTP_3`).
- The provided sample is resilient and compiles/runs on earlier JDKs by dynamically detecting `HTTP_3` and falling back to `HTTP_2` or `HTTP_1_1`.
- Your network must allow UDP/QUIC on port 443 for HTTP/3 to succeed; otherwise the client will downgrade.

Files
- `Http3Demo.java` — minimal example that prefers HTTP/3 and prints the protocol used.

Quick start
1) Navigate and compile:
   cd http3-demo
   javac Http3Demo.java

2) Run against an HTTP/3-capable site (e.g., Cloudflare):
   java Http3Demo https://www.cloudflare.com/

3) Observe output:
   - Status code
   - Protocol used: `HTTP_3`, `HTTP_2`, or `HTTP_1_1`
   - Optional `Alt-Svc` header if the server advertises HTTP/3 via Alternative Services

Notes on opting in
- Client-level preference:
  The demo sets the client’s preferred version using a helper that tries `HTTP_3` first and falls back if the enum constant isn’t present.
- Request-level preference:
  The demo also sets the request’s preferred version similarly. Either place is sufficient; both are shown for clarity.

Advanced (discovery/forcing)
- JDK 26 introduces request options to control discovery/forcing modes (e.g., discover via `Alt-Svc`, or HTTP/3-only).
- Refer to the JDK 26 `java.net.http` API docs for `H3_DISCOVERY`/`Http3DiscoveryMode` usage if you want to:
  - Start with HTTP/2/1.1 and switch to HTTP/3 when discovered (Alt-Svc mode).
  - Force HTTP/3-only (fail fast if not supported).

Troubleshooting
- If you always see `HTTP_2` or `HTTP_1_1`:
  - Ensure you are on JDK 26+: `java --version`
  - Try a known HTTP/3-enabled endpoint (e.g., `https://www.cloudflare.com/`)
  - Check that your network/firewall/proxy allows UDP/QUIC (port 443)
- If compilation fails on older JDKs:
  - Ensure you are using the provided `Http3Demo.java` which dynamically detects `HTTP_3` via `HttpClient.Version.valueOf("HTTP_3")`.

License
- For educational purposes.
