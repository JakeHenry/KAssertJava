package com.kassert;

import java.util.logging.Logger;

/**
 * KAssert compile-time configuration flag holder.
 *
 * <p>
 * This default value keeps the library self-contained. Consumer builds can and
 * should generate/override this class via KAssert's annotation processor.
 */
final class KAssertConfig
{
    /** Logger for configuration diagnostics. */
    private static final Logger LOGGER = Logger
            .getLogger(KAssertConfig.class.getName());

    /** Mutable enablement flag used by library code and tests. */
    static volatile boolean ENABLED = getEnabled();

    /**
     * Prevents instantiation of this utility class.
     */
    private KAssertConfig()
    {
        // prevent instantiation
    }

    /**
     * Resolves the default enablement state for this fallback configuration.
     *
     * @return {@code false}, indicating assertions are disabled by default
     */
    private static boolean getEnabled()
    {
        LOGGER.log(java.util.logging.Level.SEVERE,
                "Default KAssertConfig is being used. Enabled = " + ENABLED
                        + ". To enable KAssert, generate a custom KAssertConfig "
                        + "with the annotation processor.");
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
