# KAssert

Assertion library for Java.

## Overview

KAssert provides runtime assertion helpers that return `com.kassert.ex.KResult<T>`.
For zero-overhead elimination in client builds, guard calls with `KAssert.ENABLED`:

```java
if (KAssert.ENABLED) {
    KAssert.kRequire(expensiveCheck(), () -> "condition must hold").get();
}
```

When `KAssert.ENABLED` is `false`, the guarded block is removed by the compiler and
the assertion code is not evaluated.

## Compile-time enablement

KAssert includes an annotation processor that generates `com.kassert.KAssertConfig`
for the client build. Pass `-Akassert.enabled=true|false` to control the generated
flag.

### Maven example

```xml
<properties>
    <kassert.enabled>true</kassert.enabled>
</properties>

<profiles>
    <profile>
        <id>release</id>
        <properties>
            <kassert.enabled>false</kassert.enabled>
        </properties>
    </profile>
    <profile>
        <id>debug</id>
        <properties>
            <kassert.enabled>true</kassert.enabled>
        </properties>
    </profile>
</profiles>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-Akassert.enabled=${kassert.enabled}</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Build the client with:

```bash
mvn clean verify -Pdebug
mvn clean verify -Prelease
```

## Public API

### `com.kassert.KAssert`

Assertion helpers:

- `kRequire(boolean, Supplier<String>)`
- `kRefuse(boolean, Supplier<String>)`
- `kRequireEquals(expected, actual, Supplier<String>)`
- `kRefuseEquals(refusedValue, actual, Supplier<String>)`
- `kRequireSame(expected, actual, Supplier<String>)`
- `kRefuseSame(refusedReference, actual, Supplier<String>)`
- `kRequireNull(object, Supplier<String>)`
- `kRefuseNull(value, Supplier<String>)`
- `kRequireInstanceOf(expectedType, object, Supplier<String>)`
- `kRefuseInstanceOf(refusedType, object, Supplier<String>)`

Debug-only failure hooks:

- `registerDebugFailureHandler(KAssertionFailureHandler handler)`

### `com.kassert.ex.KResult<T>`

Result type returned by the assertion helpers.

Core operations:

- `ok()`, `err()`
- `get()`, `getErr()`
- `expect(String)`, `expectErr(String)`
- `getOr(T)`, `getOrElse(Function<RuntimeException, T>)`
- `map(...)`, `mapErr(...)`
- `mapOr(...)`, `mapOrElse(...)`
- `and(...)`, `andThen(...)`
- `or(...)`, `orElse(...)`
- `inspect(...)`, `inspectErr(...)`

## Debug failure handling

When `KAssert.ENABLED == true`, failed assertions dispatch a `KAssertionFailureContext`
to registered `KAssertionFailureHandler` instances and then show the built-in popup
dialog handler.

```java
import com.kassert.KAssert;
import com.kassert.KAssertionFailureContext;
import com.kassert.KAssertionFailureHandler;

if (KAssert.ENABLED) {
    KAssert.registerDebugFailureHandler(new KAssertionFailureHandler() {
        @Override
        public void onFailure(final KAssertionFailureContext context) {
            // handle the failure
        }
    });
}
```

Behavior:

- supplementary handlers run asynchronously
- handler failures are logged and do not stop the popup
- popup handling stays enabled in debug mode
- registration is a no-op when `KAssert.ENABLED` is `false`

### Runtime flags

`KFailureHandlerDispatcher` reads these system properties:

- `kassert.logFailures` — logs assertion failures when `true` (default: `true`)
- `kassert.crashOnFailure` — waits for supplementary handlers and exits the JVM when `true`
- `kassert.disablePopupHandler` — skips the popup dialog when `true`

## Dependency

```xml
<dependency>
    <groupId>com.kassert</groupId>
    <artifactId>kassert</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Build

```bash
mvn clean verify
```
