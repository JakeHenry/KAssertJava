package com.kassert;

/**
 * Compile-time configuration for KAssert.
 *
 * <p>This class is generated from a template during the Maven build.
 * The value of {@code ENABLED} is determined by the active Maven profile
 * ({@code -Pdebug} or {@code -Prelease}).
 */
final class KAssertConfig
{
    static final boolean ENABLED = ${kassert.enabled};

    private KAssertConfig()
    {
        // prevent instantiation
    }
}
