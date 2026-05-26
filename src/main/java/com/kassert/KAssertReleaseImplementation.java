package com.kassert;

/**
 * Release-mode KAssert implementation that throws immediately on failures.
 */
final class KAssertReleaseImplementation implements KAssertImplementation
{
    /**
     * Requires the supplied condition to be {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the requirement succeeds
     * @throws IllegalStateException when the requirement fails
     */
    public boolean kRequire(final boolean condition, final String message) throws IllegalStateException
    {
        return requireCondition(condition, message);
    }

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the condition is false
     */
    public boolean kRefuse(final boolean condition, final String message)
    {
        return !condition;
    }

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T> the expected type
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code expected}
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireEquals(final T expected, final Object actual, final String message) throws IllegalStateException
    {
        requireCondition(areEqual(expected, actual), message);
        return expected;
    }

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T> the value type
     * @param notExpected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code notExpected}
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNotEquals(final T notExpected, final Object actual, final String message)
            throws IllegalStateException
    {
        requireCondition(!areEqual(notExpected, actual), message);
        return notExpected;
    }

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T> the reference type
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code expected}
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireSame(final T expected, final Object actual, final String message) throws IllegalStateException
    {
        requireCondition(expected == actual, message);
        return expected;
    }

    /**
     * Requires {@code notExpected} and {@code actual} to reference different objects.
     *
     * @param <T> the reference type
     * @param notExpected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code notExpected}
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNotSame(final T notExpected, final Object actual, final String message)
            throws IllegalStateException
    {
        requireCondition(notExpected != actual, message);
        return notExpected;
    }

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNull(final T object, final String message) throws IllegalStateException
    {
        requireCondition(object == null, message);
        return object;
    }

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T> the object type
     * @param value the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNotNull(final T value, final String message) throws IllegalStateException
    {
        kRequire(value != null, message);
        return value;
    }

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
    public <T> T kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
            throws IllegalStateException
    {
        requireCondition(expectedType != null, "expectedType must not be null");
        requireCondition(expectedType.isInstance(object), message);
        return object;
    }

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
    public <T> T kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
            throws IllegalStateException
    {
        requireCondition(illegalType != null, "illegalType must not be null");
        requireCondition(!illegalType.isInstance(object), message);
        return object;
    }

    /**
     * Asserts that the supplied condition is {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertTrue(final boolean condition, final String message)
    {
        return condition;
    }

    /**
     * Asserts that the supplied condition is {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertFalse(final boolean condition, final String message)
    {
        return !condition;
    }

    /**
     * Asserts that {@code expected} and {@code actual} are equal.
     *
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertEquals(final Object expected, final Object actual, final String message)
    {
        return areEqual(expected, actual);
    }

    /**
     * Asserts that {@code expected} and {@code actual} are not equal.
     *
     * @param expected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotEquals(final Object expected, final Object actual, final String message)
    {
        return !areEqual(expected, actual);
    }

    /**
     * Asserts that {@code expected} and {@code actual} reference the same object.
     *
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertSame(final Object expected, final Object actual, final String message)
    {
        return expected == actual;
    }

    /**
     * Asserts that {@code expected} and {@code actual} reference different objects.
     *
     * @param expected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotSame(final Object expected, final Object actual, final String message)
    {
        return expected != actual;
    }

    /**
     * Asserts that the supplied object is {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNull(final Object object, final String message)
    {
        return object == null;
    }

    /**
     * Asserts that the supplied object is not {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotNull(final Object object, final String message)
    {
        return object != null;
    }

    /**
     * Asserts that the supplied object is an instance of {@code expectedType}.
     *
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertInstanceOf(final Class<?> expectedType, final Object object, final String message)
    {
        return expectedType != null && expectedType.isInstance(object);
    }

    /**
     * Asserts that the supplied object is not an instance of {@code expectedType}.
     *
     * @param expectedType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotInstanceOf(final Class<?> expectedType, final Object object, final String message)
    {
        return expectedType != null && !expectedType.isInstance(object);
    }

    /**
     * Compares two values for equality with null safety.
     *
     * @param left the left value
     * @param right the right value
     * @return {@code true} when both values are equal
     */
    private boolean areEqual(final Object left, final Object right)
    {
        if (left == right)
        {
            return true;
        }
        if (left == null)
        {
            return false;
        }
        return left.equals(right);
    }

    /**
     * Validates a condition and throws when false.
     *
     * @param condition the condition to validate
     * @param message the failure message
     * @return {@code true} when the condition is valid
     * @throws IllegalStateException when the condition is false
     */
    private boolean requireCondition(final boolean condition, final String message) throws IllegalStateException
    {
        if (!condition)
        {
            throw createAssertionError(message);
        }
        return true;
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param message the failure message
     * @return the exception to throw
     */
    private IllegalStateException createAssertionError(final String message)
    {
        if (message == null)
        {
            return new IllegalStateException("Assertion failed (No context information provided)");
        }
        return new IllegalStateException(message);
    }
}
