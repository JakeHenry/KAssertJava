package com.kassert;

/**
 * Defines the assertion operations used by {@link KAssert}.
 */
public interface KAssertImplementation
{
    /**
     * Requires the supplied condition to be {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the requirement succeeds
     * @throws IllegalStateException when the requirement fails
     */
    boolean kRequire(boolean condition, String message) throws IllegalStateException;

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the condition is false
     * @throws IllegalStateException when the requirement fails
     */
    boolean kRefuse(boolean condition, String message) throws IllegalStateException;

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T> the type of expected value
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireEquals(T expected, Object actual, String message) throws IllegalStateException;

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T> the type of the disallowed value
     * @param notExpected the value that must not match
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireNotEquals(T notExpected, Object actual, String message) throws IllegalStateException;

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T> the expected reference type
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireSame(T expected, Object actual, String message) throws IllegalStateException;

    /**
     * Requires {@code notExpected} and {@code actual} to reference different objects.
     *
     * @param <T> the disallowed reference type
     * @param notExpected the reference that must not match
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireNotSame(T notExpected, Object actual, String message) throws IllegalStateException;

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireNull(T object, String message) throws IllegalStateException;

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireNotNull(T object, String message) throws IllegalStateException;

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T> the object type
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireInstanceOf(Class<?> expectedType, T object, String message) throws IllegalStateException;

    /**
     * Requires the supplied object not to be an instance of {@code illegalType}.
     *
     * @param <T> the object type
     * @param illegalType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    <T> T kRequireNotInstanceOf(Class<?> illegalType, T object, String message) throws IllegalStateException;

    /**
     * Asserts that the supplied condition is {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertTrue(boolean condition, String message);

    /**
     * Asserts that the supplied condition is {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertFalse(boolean condition, String message);

    /**
     * Asserts that {@code expected} and {@code actual} are equal.
     *
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertEquals(Object expected, Object actual, String message);

    /**
     * Asserts that {@code expected} and {@code actual} are not equal.
     *
     * @param expected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertNotEquals(Object expected, Object actual, String message);

    /**
     * Asserts that {@code expected} and {@code actual} reference the same object.
     *
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertSame(Object expected, Object actual, String message);

    /**
     * Asserts that {@code expected} and {@code actual} reference different objects.
     *
     * @param expected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertNotSame(Object expected, Object actual, String message);

    /**
     * Asserts that the supplied object is {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertNull(Object object, String message);

    /**
     * Asserts that the supplied object is not {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertNotNull(Object object, String message);

    /**
     * Asserts that the supplied object is an instance of {@code expectedType}.
     *
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertInstanceOf(Class<?> expectedType, Object object, String message);

    /**
     * Asserts that the supplied object is not an instance of {@code expectedType}.
     *
     * @param expectedType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    boolean kAssertNotInstanceOf(Class<?> expectedType, Object object, String message);
}
