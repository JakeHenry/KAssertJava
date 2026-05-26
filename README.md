# KAssert
Assertion Library For Java

## Runtime implementation injection

KAssert selects its implementation at runtime based on JVM assertion status:

- **Assertions enabled (`-ea`)**: `KAssertDebugImplementation`
- **Assertions disabled**: `KAssertReleaseImplementation`

In debug mode, any assertion failure opens an always-on-top, application-modal Swing dialog showing the assertion error and the most recent 10 stack trace lines. The dialog offers:

- **Continue**: throws `AssertionError` and allows normal exception handling
- **Exit JVM**: terminates the running JVM with `System.exit(1)`

## Build

```bash
mvn clean verify
```
