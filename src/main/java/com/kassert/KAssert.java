package com.kassert;

import com.kassert.ex.KFailed;
import com.kassert.ex.KSuccess;
import com.kassert.ex.KResult;

/**
 * Facade entry point for KAssert require operations.
 *
 * <p> KAssert provides runtime assertion methods. For client-side compile-time
 * elimination, use the generated constant {@code
 * com.kassert.KAssertConfig.ENABLED} (created by the included annotation
 * processor).
 *
 * <p> Consumers should guard assertion calls for zero-overhead elimination in
 * release builds:
 * 
 * <pre>{@code if (com.kassert.KAssertConfig.ENABLED) {
 * KAssert.kRequire(condition, "message").throwIfFailed(); } }</pre>
 *
 * <p> When the generated client flag is {@code false}, the Java compiler
 * eliminates guarded blocks entirely from bytecode via dead code elimination.
 */
public final class KAssert
{
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(KAssert.class.getName());

    /**
     * Compile-time conditional compilation flag. This references
     * {@code KAssertConfig.ENABLED}, which is generated in client builds by
     * KAssert's annotation processor.
     */
    public static final boolean ENABLED = KAssertConfig.ENABLED;

    /**
     * Prevents instantiation of this utility class.
     */
    private KAssert()
    {
        // prevent instantiation
    }

    /**
     * Requires the supplied condition to be {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message   the failure message
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRequire(final boolean condition, final String message)
    {
        if (!condition) return failedResult(Boolean.valueOf(condition), message);
        return new KSuccess<Boolean>(Boolean.valueOf(condition));
    }

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message   the failure message
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRefuse(final boolean condition, final String message)
    {
        if (condition) return failedResult(Boolean.valueOf(!condition), message);
        return new KSuccess<Boolean>(Boolean.valueOf(!condition));
    }

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T>      the expected type
     * @param expected the expected value
     * @param actual   the actual value
     * @param message  the failure message
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireEquals(final T expected, final K actual, final String message)
    {
        if (!areEqual(expected, actual)) return failedResult(actual, message);
        return new KSuccess<K>(actual);
    }

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T>         the value type
     * @param notExpected the disallowed value
     * @param actual      the actual value
     * @param message     the failure message
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireNotEquals(final T notExpected, final K actual, final String message)
    {
        if (areEqual(notExpected, actual)) return failedResult(actual, message);
        return new KSuccess<K>(actual);
    }

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T>      the reference type
     * @param expected the expected reference
     * @param actual   the actual reference
     * @param message  the failure message
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireSame(final T expected, final K actual, final String message)
    {
        if (expected != actual) return failedResult(actual, message);
        return new KSuccess<K>(actual);
    }

    /**
     * Requires {@code notExpected} and {@code actual} to reference different
     * objects.
     *
     * @param <T>         the reference type
     * @param notExpected the disallowed reference
     * @param actual      the actual reference
     * @param message     the failure message
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireNotSame(final T notExpected, final K actual, final String message)
    {
        if (notExpected == actual) return failedResult(actual, message);
        return new KSuccess<K>(actual);
    }

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T>     the object type
     * @param object  the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNull(final T object, final String message)
    {
        if (object != null) return failedResult(object, message);
        return new KSuccess<T>(object);
    }

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T>     the object type
     * @param value   the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotNull(final T value, final String message)
    {
        if (value == null) return failedResult(value, message);
        return new KSuccess<T>(value);
    }

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T>          the object type
     * @param expectedType the required runtime type
     * @param object       the value to validate
     * @param message      the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
    {
        if (expectedType == null) return failedResult(object, "expectedType must not be null");
        if (!expectedType.isInstance(object)) return failedResult(object, message);
        return new KSuccess<T>(object);
    }

    /**
     * Requires the supplied object not to be an instance of {@code illegalType}.
     *
     * @param <T>         the object type
     * @param illegalType the disallowed runtime type
     * @param object      the value to validate
     * @param message     the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
    {
        if (illegalType == null)
        {
            return failedResult(object, "illegalType must not be null");
        }
        if (illegalType.isInstance(object))
        {
            return failedResult(object, message);
        }
        return new KSuccess<T>(object);
    }

    /**
     * Registers a supplementary assertion failure handler for debug mode only.
     *
     * <p>
     * When {@link #ENABLED} is {@code false}, this method is a no-op.
     *
     * @param handler the supplementary handler to register
     */
    public static void registerDebugFailureHandler(final KAssertionFailureHandler handler)
    {
        if (KAssertConfig.ENABLED) KFailureHandlerDispatcher.INSTANCE.registerSupplementaryHandler(handler);
    }

    /**
     * Compares two values for equality with null safety.
     *
     * @param left  the left value
     * @param right the right value
     * @return {@code true} when both values are equal
     */
    private static boolean areEqual(final Object left, final Object right)
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

    private static <T> KResult<T> failedResult(final T value, final String message)
    {
        final RuntimeException error = createAssertionError(message);
        LOG.log(java.util.logging.Level.SEVERE, "Assertion failed: " + error.getMessage(), error);
        if (KAssertConfig.ENABLED)
        {
            KFailureHandlerDispatcher.INSTANCE.dispatchDebugFailure(new KAssertionFailureContext(error));
        }
        return new KFailed<T>(value, error);
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param message the failure message
     * @return the exception to report
     */
    private static IllegalStateException createAssertionError(final String message)
    {
        if (message == null)
        {
            return new IllegalStateException("Assertion failed (No context information provided)");
        }
        return new IllegalStateException(message);
    }
}
