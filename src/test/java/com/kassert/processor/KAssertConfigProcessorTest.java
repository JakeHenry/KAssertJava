package com.kassert.processor;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;

/**
 * Integration tests for {@link KAssertConfigProcessor} code generation.
 */
public class KAssertConfigProcessorTest
{
    /**
     * Verifies generated source for release mode.
     *
     * @throws Exception if writing source fails
     */
    @Test
    public void releaseModeGeneratedSourceTest() throws Exception
    {
        final StringWriter writer = new StringWriter();
        KAssertConfigProcessor.writeSource(writer, Boolean.FALSE);

        final String generated = writer.toString();
        assertTrue(generated.contains("final class KAssertConfig"));
        assertTrue(generated.contains("static final boolean ENABLED = false;"));
    }

    /**
     * Verifies generated source for debug mode.
     *
     * @throws Exception if writing source fails
     */
    @Test
    public void debugModeGeneratedSourceTest() throws Exception
    {
        final StringWriter writer = new StringWriter();
        KAssertConfigProcessor.writeSource(writer, Boolean.TRUE);

        final String generated = writer.toString();
        assertTrue(generated.contains("final class KAssertConfig"));
        assertTrue(generated.contains("static final boolean ENABLED = true;"));
    }
}
