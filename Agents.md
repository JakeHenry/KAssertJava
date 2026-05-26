# Agents.md

Guidelines for coding agents working in this repository.

**Tradeoff:** These guidelines intentionally bias toward correctness and clarity over raw speed.

## 1. Think Before Coding

**Don’t assume. Don’t hide confusion. Surface tradeoffs.**

Before implementing:
- State assumptions explicitly.
- If multiple interpretations exist, call them out instead of choosing silently.
- If a simpler approach exists, prefer it and say why.
- If requirements are unclear, stop and ask.

## 2. Simplicity First

**Write the minimum code that solves the requested problem.**

- Don’t add features that were not requested.
- Don’t introduce abstractions for single-use code.
- Don’t add configurability “for the future” unless requested.
- Avoid speculative error handling for impossible scenarios.
- If a solution is obviously overcomplicated, simplify it.

## 3. Surgical Changes

**Touch only what is necessary for the request.**

When editing existing code:
- Don’t refactor unrelated code.
- Don’t reformat unrelated files.
- Match the repository’s existing style and conventions.
- If you see unrelated issues, mention them separately instead of fixing them silently.

When your change causes cleanup needs:
- Remove imports/variables/functions made unused by your change.
- Don’t remove pre-existing dead code unless requested.

## 4. Goal-Driven Execution

**Define success criteria and verify outcomes.**

Turn vague asks into verifiable goals:
- “Fix bug” -> reproduce, implement fix, confirm reproduction no longer fails.
- “Add validation” -> add/adjust checks and ensure expected invalid paths are handled.
- “Refactor X” -> preserve behavior and verify before/after outcomes.

For multi-step work, use a short plan:
1. Implement step.
2. Verify expected outcome.
3. Iterate until criteria are met.

## 5. Repository-Specific Notes (KAssert)

- KAssert is a Java assertion library (see `README.md`).
- Keep Java build artifacts out of version control (`.class`, `.jar`, `.war`, `.ear`, etc.), consistent with `.gitignore`.
- Keep new source and dependencies compatible with the Apache-2.0 license (`LICENSE`).
- All code should be written in Java, following standard Java conventions and the style of any existing code once it is added.
- When adding tests, follow standard Java testing practices and keep test code separate from library sources.
- For build/test execution, use the commands defined in `.vscode/tasks.json`:
  - `Maven: Test` (`D:/apache-maven-3.2.5/bin/mvn.bat test`) for tests.
  - `Maven: Clean Verify` (`D:/apache-maven-3.2.5/bin/mvn.bat clean verify`) for full verification.
  - `Maven: Package` (`D:/apache-maven-3.2.5/bin/mvn.bat package`) for packaging.
- No third party libraries are permitted except for JUnit for testing, unless explicitly requested.
- NASA/JPL **Power of 10** rule #5 applies to the implementation of this library with non-negotiable priority where applicable to Java.
- Java 6 runtime compatibility is required for the library. 
//
### Power of 10 Rule #5

> **Rule:** The assertion density of the code should average to a minimum of two assertions per function. Assertions are used to check for anomalous conditions that should never happen in real-life executions. Assertions must always be side-effect free and should be defined as Boolean tests. When an assertion fails, an explicit recovery action must be taken, e.g., by returning an error condition to the caller of the function that executes the failing assertion. Any assertion for which a static checking tool can prove that it can never fail or never hold violates this rule. (I.e., it is not possible to satisfy the rule by adding unhelpful `assert(true)` statements.)
>
> **Rationale:** Statistics for industrial coding efforts indicate that unit tests often find at least one defect per 10 to 100 lines of code written. The odds of intercepting defects increase with assertion density. Use of assertions is often also recommended as part of a strong defensive coding strategy. Assertions can be used to verify pre- and post-conditions of functions, parameter values, return values of functions, and loop-invariants. Because assertions are side-effect free, they can be selectively disabled after testing in performance-critical code.

A typical use of an assertion:

```c
if (!c_assert(p >= 0) == true) {
    return ERROR;
}
```

With the assertion defined as:

```c
#define c_assert(e) ((e) ? (true) : \
    (tst_debugging("%s,%d: assertion '%s' failed\n", \
    __FILE__, __LINE__, #e), false))
```

In this definition, `__FILE__` and `__LINE__` are predefined by the macro preprocessor to produce the filename and line number of the failing assertion. The syntax `#e` turns assertion condition `e` into a string that is printed as part of the error message. In code destined for an embedded processor there is no place to print the error message itself; in that case, the call to `tst_debugging` is turned into a no-op, and the assertion becomes a pure Boolean test that enables error recovery from anomalous behavior.
