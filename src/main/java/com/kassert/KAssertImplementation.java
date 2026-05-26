package com.kassert;

import com.kassert.ex.KResult;

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
     */
    KResult<Boolean> kRequire(boolean condition, String message);

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the condition is false
     */
    KResult<Boolean> kRefuse(boolean condition, String message);

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T> the type of expected value
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireEquals(T expected, Object actual, String message);

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T> the type of the disallowed value
     * @param notExpected the value that must not match
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireNotEquals(T notExpected, Object actual, String message);

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T> the expected reference type
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireSame(T expected, Object actual, String message);

    /**
     * Requires {@code notExpected} and {@code actual} to reference different objects.
     *
     * @param <T> the disallowed reference type
     * @param notExpected the reference that must not match
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireNotSame(T notExpected, Object actual, String message);

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireNull(T object, String message);

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireNotNull(T object, String message);

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T> the object type
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireInstanceOf(Class<?> expectedType, T object, String message);

    /**
     * Requires the supplied object not to be an instance of {@code illegalType}.
     *
     * @param <T> the object type
     * @param illegalType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     */
    <T> KResult<T> kRequireNotInstanceOf(Class<?> illegalType, T object, String message);
}
