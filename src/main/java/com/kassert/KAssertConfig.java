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

    static final boolean ENABLED = getEnabled();

    private KAssertConfig()
    {
        // prevent instantiation
    }

    private static boolean getEnabled()
    {
        LOGGER.log(java.util.logging.Level.WARNING, "Default KAssertConfig is being used. Enabled = " + ENABLED
                + ". To enable KAssert, generate a custom KAssertConfig with the annotation processor.");

        return false;
    }
}
