package com.kassert;

import com.kassert.ex.KResult;

/**
 * Facade entry point for KAssert require operations.
 */
public final class KAssert
{
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(KAssert.class.getName());

    private static volatile KAssertImplementation implementation = createDefaultImplementation();

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
     * @param message the failure message
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRequire(final boolean condition, final String message)
    {
        return implementation.kRequire(condition, message);
    }

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRefuse(final boolean condition, final String message)
    {
        return implementation.kRefuse(condition, message);
    }

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T> the expected type
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireEquals(final T expected, final Object actual, final String message)
    {
        return implementation.kRequireEquals(expected, actual, message);
    }

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T> the value type
     * @param notExpected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotEquals(final T notExpected, final Object actual, final String message)
    {
        return implementation.kRequireNotEquals(notExpected, actual, message);
    }

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T> the reference type
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireSame(final T expected, final Object actual, final String message)
    {
        return implementation.kRequireSame(expected, actual, message);
    }

    /**
     * Requires {@code notExpected} and {@code actual} to reference different objects.
     *
     * @param <T> the reference type
     * @param notExpected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotSame(final T notExpected, final Object actual, final String message)
    {
        return implementation.kRequireNotSame(notExpected, actual, message);
    }

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNull(final T object, final String message)
    {
        return implementation.kRequireNull(object, message);
    }

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T> the object type
     * @param value the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotNull(final T value, final String message)
    {
        return implementation.kRequireNotNull(value, message);
    }

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T> the object type
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
    {
        return implementation.kRequireInstanceOf(expectedType, object, message);
    }

    /**
     * Requires the supplied object not to be an instance of {@code illegalType}.
     *
     * @param <T> the object type
     * @param illegalType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the result of the requirement
     */
    public static <T> KResult<T> kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
    {
        return implementation.kRequireNotInstanceOf(illegalType, object, message);
    }

    /**
     * Creates the default KAssertImplementation based on whether JVM assertions are
     * enabled. If assertions are enabled, it will attempt to load a custom debug
     * implementation specified by the "kassert.debug.implementation" system
     * property, falling back to KAssertDebugImplementation if not specified or
     * invalid. If assertions are not enabled, it will attempt to load a custom
     * release implementation specified by the "kassert.release.implementation"
     * system property, falling back to KAssertReleaseImplementation if not
     * specified or invalid.
     *
     * @return the default KAssertImplementation based on the JVM assertion status
     */
    private static KAssertImplementation createDefaultImplementation()
    {
        if (isJvmAssertionsEnabled())
        {
            final KAssertImplementation customImpl = getImplementation("kassert.debug.implementation");
            return customImpl != null ? customImpl : new KAssertDebugImplementation();
        }
        final KAssertImplementation customImpl = getImplementation("kassert.release.implementation");
        return customImpl != null ? customImpl : new KAssertReleaseImplementation();
    }

    /**
     * Attempts to load a custom KAssertImplementation based on the provided system
     * property. If the system property is set and points to a valid class that
     * implements KAssertImplementation, an instance of that class will be returned.
     * If the class cannot be loaded or does not implement the interface, an error
     * will be logged and {@code null} will be returned.
     *
     * @param systemProperty the name of the system property to check for a custom
     *                       implementation class name
     * @return an instance of the custom KAssertImplementation if specified and
     *         valid, or {@code null} if not specified or invalid
     */
    private static KAssertImplementation getImplementation(final String systemProperty)
    {
        final String customImplementationClassName = System.getProperty(systemProperty);
        if (customImplementationClassName != null)
        {
            try
            {
                final Class<?> customClass = Class.forName(customImplementationClassName);
                if (KAssertImplementation.class.isAssignableFrom(customClass))
                {
                    return (KAssertImplementation) customClass.getDeclaredConstructor().newInstance();
                }
                else
                {
                    LOG.log(java.util.logging.Level.SEVERE, "Custom KAssertImplementation class '"
                            + customImplementationClassName + "' does not implement KAssertImplementation interface.");
                }
            }
            catch (Exception e)
            {
                LOG.log(java.util.logging.Level.SEVERE, "Failed to load custom KAssertImplementation '"
                        + customImplementationClassName + "': " + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Checks whether JVM assertions are enabled.
     *
     * @return {@code true} when assertions are enabled
     */
    static boolean isJvmAssertionsEnabled()
    {
        boolean enabled = false;
        assert enabled = true; // intentional side effect to detect if assertions are enabled
        return enabled;
    }
}
