package com.kassert;

import com.kassert.ex.KFailed;
import com.kassert.ex.KOk;
import com.kassert.ex.KResult;

/**
 * Release-mode KAssert implementation that reports failures as results.
 */
final class KAssertReleaseImplementation implements KAssertImplementation
{
    public KResult<Boolean> kRequire(final boolean condition, final String message)
    {
        return requireResult(condition, Boolean.valueOf(condition), message);
    }

    public KResult<Boolean> kRefuse(final boolean condition, final String message)
    {
        final boolean accepted = !condition;
        return requireResult(accepted, Boolean.valueOf(accepted), message);
    }

    public <T> KResult<T> kRequireEquals(final T expected, final Object actual, final String message)
    {
        return requireResult(areEqual(expected, actual), expected, message);
    }

    public <T> KResult<T> kRequireNotEquals(final T notExpected, final Object actual, final String message)
    {
        return requireResult(!areEqual(notExpected, actual), notExpected, message);
    }

    public <T> KResult<T> kRequireSame(final T expected, final Object actual, final String message)
    {
        return requireResult(expected == actual, expected, message);
    }

    public <T> KResult<T> kRequireNotSame(final T notExpected, final Object actual, final String message)
    {
        return requireResult(notExpected != actual, notExpected, message);
    }

    public <T> KResult<T> kRequireNull(final T object, final String message)
    {
        return requireResult(object == null, object, message);
    }

    public <T> KResult<T> kRequireNotNull(final T value, final String message)
    {
        return requireResult(value != null, value, message);
    }

    public <T> KResult<T> kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
    {
        if (expectedType == null)
        {
            return new KFailed<T>(object, createAssertionError("expectedType must not be null"));
        }
        return requireResult(expectedType.isInstance(object), object, message);
    }

    public <T> KResult<T> kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
    {
        if (illegalType == null)
        {
            return new KFailed<T>(object, createAssertionError("illegalType must not be null"));
        }
        return requireResult(!illegalType.isInstance(object), object, message);
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

    private <T> KResult<T> requireResult(final boolean condition, final T value, final String message)
    {
        return condition ? new KOk<T>(value) : new KFailed<T>(value, createAssertionError(message));
    }

    private RuntimeException createAssertionError(final String message)
    {
        if (message == null)
        {
            return new RuntimeException("Assertion failed (No context information provided)");
        }
        return new RuntimeException(message);
    }
}
