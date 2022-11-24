Test HTTP echo server
---

It just returns input payload back. 
Build and run `java -jar jetty-http-echo-1.0-SNAPSHOT-shaded.jar` from `target` subdirectory.
If you need to run in background, use `nohup`.

Arguments
--

- `--port <int>` or `-p <int>` - port to use
- `--threads <int>` or `-t <int>` - max server threads
- `--ssl` - if you need TLS and HTTP/2. This option requires next two options. Without them no TLS/ALPN will be enabled.
- `--keyfile` - path to JKS keyfile. It can be self-signed. SNI is disabled.
- `--keypass` - password for it

