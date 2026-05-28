package com.kassert;

/**
 * KAssert compile-time configuration flag holder.
 *
 * <p>
 * This default value keeps the library self-contained. Consumer builds can and
 * should generate/override this class via KAssert's annotation processor.
 */
final class KAssertConfig
{
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(KAssertConfig.class.getName());

    static volatile boolean ENABLED = getEnabled();

    private KAssertConfig()
    {
        // prevent instantiation
    }

    private static boolean getEnabled()
    {
        LOGGER.log(java.util.logging.Level.SEVERE, "Default KAssertConfig is being used. Enabled = " + ENABLED
                + ". To enable KAssert, generate a custom KAssertConfig with the annotation processor.");
        return false;
    }

    /**
     * Sets the enabled flag for testing purposes. This should not be used in
     * production code.
     * 
     * @param enabled the value to set for the enabled flag
     */
    public static synchronized void setEnabledForTesting(final boolean enabled)
    {
        ENABLED = enabled;
    }
}
