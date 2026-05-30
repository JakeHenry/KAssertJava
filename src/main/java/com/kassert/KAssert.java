package com.kassert;

import static com.kassert.ex.KResult.empty;
import static com.kassert.ex.KResult.err;
import static com.kassert.ex.KResult.ok;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import com.kassert.ex.KResult;

/**
 * Facade entry point for KAssert require operations.
 *
 * <p>
 * KAssert provides runtime assertion methods. For client-side compile-time
 * elimination, use the generated constant {@code
 * com.kassert.KAssertConfig.ENABLED} (created by the included annotation
 * processor).
 *
 * <p>
 * Consumers should guard assertion calls for zero-overhead elimination in
 * release builds:
 * 
 * <pre>{@code
 * if (com.kassert.KAssertConfig.ENABLED)
 * {
 *     KAssert.kRequire(condition, () -> "message").unwrap();
 * }
 * }</pre>
 *
 * <p>
 * When the generated client flag is {@code false}, the Java compiler eliminates
 * guarded blocks entirely from bytecode via dead code elimination.
 */
public final class KAssert
{
    /** Logger used for assertion failure diagnostics. */
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(KAssert.class.getName());

    /** Flag to control logging of assertion failures. */
    private static final boolean LOG_FAILURES = Boolean.parseBoolean(System.getProperty("kassert.logFailures", "true"));

    /**
     * Compile-time conditional compilation flag. This references
     * {@code KAssertConfig.ENABLED}, which is generated in client builds by
     * KAssert's annotation processor.
     */
    public static final boolean ENABLED = KAssertConfig.ENABLED;

    static
    {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true; // Intentional side effect
        if (assertionsEnabled != ENABLED)
        {
            final IllegalStateException configException = new IllegalStateException(String.format(
                    "KAssert is misconfigured: assertionsEnabled=%b, KAssertConfig.ENABLED=%b. "
                            + "This may be due to missing annotation processing in the client build.",
                    assertionsEnabled, ENABLED));
            LOG.log(Level.SEVERE, "KAssert misuse detected!", configException);
            final KPopupDialogFailureHandler kPopupDialogFailureHandler = new KPopupDialogFailureHandler(
                    JOptionPane.WARNING_MESSAGE, "KAssert Misconfiguration", TimeUnit.SECONDS.toMillis(30));
            kPopupDialogFailureHandler.onFailure(new KAssertionFailureContext(configException));
        }
    }

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
     * @param condition       the condition to evaluate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRequire(final boolean condition, final Supplier<String> messageSupplier)
    {
        if (!condition) return failedResult(messageSupplier);
        return ok(Boolean.valueOf(condition));
    }

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition       the condition to evaluate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRefuse(final boolean condition, final Supplier<String> messageSupplier)
    {
        if (condition) return failedResult(messageSupplier);
        return ok(Boolean.valueOf(!condition));
    }

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T>             the expected type
     * @param expected        the expected value
     * @param actual          the actual value
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireEquals(final T expected, final K actual,
            final Supplier<String> messageSupplier)
    {
        if (!Objects.equals(expected, actual)) return failedResult(messageSupplier);
        return actual == null ? empty() : ok(actual);
    }

    /**
     * Refuses {@code actual} when it equals {@code refusedValue}.
     *
     * @param <T>             the value type
     * @param refusedValue    the refused value
     * @param actual          the actual value
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRefuseEquals(final T refusedValue, final K actual,
            final Supplier<String> messageSupplier)
    {
        if (Objects.equals(refusedValue, actual)) return failedResult(messageSupplier);
        return actual == null ? empty() : ok(actual);
    }

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T>             the reference type
     * @param expected        the expected reference
     * @param actual          the actual reference
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRequireSame(final T expected, final K actual,
            final Supplier<String> messageSupplier)
    {
        if (expected != actual) return failedResult(messageSupplier);
        return actual == null ? empty() : ok(actual);
    }

    /**
     * Refuses {@code actual} when it references the same object as
     * {@code refusedReference}.
     *
     * @param <T>              the reference type
     * @param refusedReference the refused reference
     * @param actual           the actual reference
     * @param messageSupplier  supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T, K> KResult<K> kRefuseSame(final T refusedReference, final K actual,
            final Supplier<String> messageSupplier)
    {
        if (refusedReference == actual) return failedResult(messageSupplier);
        return actual == null ? empty() : ok(actual);
    }

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T>             the object type
     * @param object          the value to validate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNull(final T object, final Supplier<String> messageSupplier)
    {
        if (object != null) return failedResult(messageSupplier);
        return empty();
    }

    /**
     * Refuses a {@code null} supplied object.
     *
     * @param <T>             the object type
     * @param value           the value to validate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRefuseNull(final T value, final Supplier<String> messageSupplier)
    {
        if (value == null) return failedResult(messageSupplier);
        return ok(value);
    }

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T>             the object type
     * @param expectedType    the required runtime type
     * @param object          the value to validate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireInstanceOf(final Class<?> expectedType, final T object,
            final Supplier<String> messageSupplier)
    {
        if (expectedType == null) return failedResult(() -> "expectedType must not be null");
        if (!expectedType.isInstance(object)) return failedResult(messageSupplier);
        return ok(object);
    }

    /**
     * Refuses a supplied object that is an instance of {@code illegalType}.
     *
     * @param <T>             the object type
     * @param refusedType     the refused runtime type
     * @param object          the value to validate
     * @param messageSupplier supplies the failure message if the assertion fails
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRefuseInstanceOf(final Class<?> refusedType, final T object,
            final Supplier<String> messageSupplier)
    {
        if (refusedType == null) return failedResult(() -> "refusedType must not be null");
        if (refusedType.isInstance(object)) return failedResult(messageSupplier);
        return ok(object);
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
     * Creates a failed assertion result and dispatches debug handlers when enabled.
     *
     * @param <T>             result value type
     * @param messageSupplier supplies the failure message
     * @return failed assertion result
     */
    @SuppressWarnings("unchecked") // Safe: KResult is final; type erasure makes KResult<T> indistinguishable from
                                   // KResult<Object> at runtime.
    private static <T> KResult<T> failedResult(final Supplier<String> messageSupplier)
    {
        final RuntimeException error = createAssertionError(messageSupplier);
        if (LOG_FAILURES)
        {
            LOG.log(java.util.logging.Level.SEVERE, "Assertion failed: " + error.getMessage(), error);
        }
        if (KAssertConfig.ENABLED)
        {
            KFailureHandlerDispatcher.INSTANCE.dispatchDebugFailure(new KAssertionFailureContext(error));
        }
        return err((Class<T>) Object.class, error);
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param messageSupplier supplies the failure message
     * @return the exception to report
     */
    private static IllegalStateException createAssertionError(final Supplier<String> messageSupplier)
    {
        final String message = (messageSupplier == null) ? null : messageSupplier.get();
        if (message == null) return new IllegalStateException("Assertion failed (No context information provided)");
        return new IllegalStateException(message);
    }
}
