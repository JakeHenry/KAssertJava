# KAssert
Assertion Library For Java

## Conditional compilation

KAssert ships with an annotation processor that generates a client-side compile-time
flag class:

- `com.kassert.KAssertConfig.ENABLED`

This flag is then assigned to a public constant in `com.kassert.KAssert.ENABLED` for client code to reference.

Client code should guard KAssert calls with this generated constant so the Java compiler
can remove assertion blocks in release mode.

### Consumer guard pattern

```java
if (com.kassert.KAssert.ENABLED) {
    KAssert.kRequire(expensiveCheck(), "condition").throwIfFailed();
}
```

When `KAssert.ENABLED` is `false`, the compiler removes the entire
guarded block — no method call, no argument evaluation, zero overhead.

## Client mode selection (single KAssert artifact)

Clients switch mode at **their own compile time** by passing `-Akassert.enabled=...`
to javac (via Maven compiler args). No separate KAssert debug/release artifacts are
required.

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
# Client debug build (generated ENABLED=true)
mvn clean verify -Pdebug

# Client release build (generated ENABLED=false)
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

## Generated source and client builds

KAssert's annotation processor generates `com.kassert.KAssertConfig` in the
**client app build**. The client only needs to pass the processor option above.

Default behavior:

- if `kassert.enabled` is omitted, generated `ENABLED` defaults to `false`
- pass `-Akassert.enabled=false` for release-mode elimination

## Build

```bash
mvn clean verify
```
