# INC-09-A: token in the logs: hint ladder

Open ONE level at a time, and only after you have tried for 10 minutes.

<details><summary>Level 1: symptom only</summary>

A log search for "Bearer" returns real customer tokens. The logging code is a single line.
</details>

<details><summary>Level 2: where to look</summary>

`RequestLogger.logIncoming`. What does `{}` print when given a Java `record`? Look at the `toString()` of `HttpRequest`.
</details>

<details><summary>Level 3: the tool</summary>

Run `RequestLoggerTest.noBearerToken` and read the full logged line in the failure message. Count how many secrets you can spot in it.
</details>

<details><summary>Level 4: the concept to search</summary>

Search: "OWASP logging cheat sheet sensitive data" and "log allowlist vs redaction".
</details>

<details><summary>Level 5: fix outline</summary>

Log an allowlist: method, path, and a fixed set of safe headers (`X-Request-Id`, `Content-Type`, `Accept`), plus the body length, never the body. An allowlist is safer than a blocklist because new secrets are excluded by default. Keep the two guard tests green.
</details>
