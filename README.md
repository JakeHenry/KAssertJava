# KAssert
Assertion Library For Java

## Conditional compilation

KAssert uses a compile-time `static final boolean` flag (`KAssert.ENABLED`) to support
the conditional compilation idiom. The flag is set by the Maven build profile and enables
the Java compiler to eliminate assertion call-sites entirely from bytecode via dead code
elimination.

### Consumer guard pattern

```java
if (KAssert.ENABLED) {
    KAssert.kRequire(expensiveCheck(), "condition").throwIfFailed();
}
```

When `KAssert.ENABLED` is `false` (release profile), the compiler removes the entire
guarded block — no method call, no argument evaluation, zero overhead.

### Build profiles

A Maven profile **must** be specified. The build fails with a clear error if neither is
active:

```bash
# Debug build — assertions are active, failures show a Swing dialog
mvn clean verify -Pdebug

# Release build — assertions are compiled out (no-ops)
mvn clean verify -Prelease
```

## Using KAssert in a client app

Add KAssert as a normal Maven dependency:

```xml
<dependency>
    <groupId>com.kassert</groupId>
    <artifactId>kassert</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

If you are building KAssert from source first, install/deploy it with the profile you want
to ship:

```bash
# Build/install debug artifact (ENABLED=true)
mvn clean install -Pdebug

# Build/install release artifact (ENABLED=false)
mvn clean install -Prelease
```

The selected profile is baked into the produced JAR via `KAssertConfig.ENABLED`.

### How a client switches debug vs release mode

Mode is fixed at **KAssert build time**, not at client runtime. A client switches mode by
using a different KAssert artifact/version:

- debug client build -> depend on a KAssert JAR built with `-Pdebug`
- release client build -> depend on a KAssert JAR built with `-Prelease`

Typical setup is to publish two versions (for example, `1.0.0-debug` and
`1.0.0-release`) and let the client select one via Maven profiles:

```xml
<properties>
    <kassert.version>1.0.0-release</kassert.version>
</properties>

<profiles>
    <profile>
        <id>debug</id>
        <properties>
            <kassert.version>1.0.0-debug</kassert.version>
        </properties>
    </profile>
</profiles>
```

## Generated source and client builds

KAssert generates `KAssertConfig.java` during **KAssert's own build** (from
`src/main/java-templates`). That generated class is compiled into the KAssert JAR.

Client applications that depend on KAssert do **not** need to:

- enable generated sources
- add templating plugins
- configure special compiler/source directories

For clients, KAssert is a standard binary dependency.

### Invalid state detection

At class load time, KAssert validates that the compiled profile matches the JVM state.
If the library was compiled with `-Prelease` (`ENABLED = false`) but the JVM has
assertions enabled (`-ea`), a warning dialog is shown indicating a configuration
mismatch.

## Build

```bash
mvn clean verify -Pdebug
```
