package com.kassert.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Generates a client-side compile-time flag class for guarding KAssert calls.
 *
 * <p>
 * Supported options:
 * <ul>
 * <li>{@code kassert.enabled} (default: {@code false})</li>
 * </ul>
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(
{ "kassert.enabled" })
public final class KAssertConfigProcessor extends AbstractProcessor
{
    /** Processor option key for the enabled flag. */
    private static final String OPTION_ENABLED = "kassert.enabled";
    /** Default enabled option value. */
    private static final String DEFAULT_ENABLED = "false";
    /** Fully qualified generated class name. */
    private static final String DEFAULT_CLASS_NAME = "com.kassert.KAssertConfig";
    /** Tracks whether generation has already happened for this compilation. */
    private boolean generated;

    /**
     * Initializes processor state for a new compilation.
     *
     * @param processingEnv annotation processing environment
     */
    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        generated = false;
    }

    /**
     * Generates the {@code com.kassert.KAssertConfig} source file once per
     * compilation.
     *
     * @param annotations annotation types requested for processing
     * @param roundEnv    current processing round environment
     * @return {@code false} to allow other processors to process annotations
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv)
    {
        if (generated || roundEnv.processingOver())
            return false;

        final boolean enabled = Boolean.parseBoolean(
                getOptionOrDefault(OPTION_ENABLED, DEFAULT_ENABLED));
        try
        {
            final JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(DEFAULT_CLASS_NAME);
            try (Writer writer = sourceFile.openWriter())
            {
                writeSource(writer, enabled);
            }
            generated = true;
        }
        catch (FilerException ignored)
        {
            generated = true;
        }
        catch (IOException error)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + DEFAULT_CLASS_NAME + ": "
                            + error.getMessage());
        }
        return false;
    }

    /**
     * Writes the source code for the generated configuration class.
     *
     * @param writer  destination writer
     * @param enabled generated enabled constant value
     * @throws IOException when writing fails
     */
    static void writeSource(final Writer writer, final boolean enabled)
            throws IOException
    {
        final String source = String.format("package com.kassert;%n"
                + "final class KAssertConfig%n" + "{%n"
                + "    static final boolean ENABLED = %b;%n"
                + "    private KAssertConfig()%n" + "    {%n"
                + "        // prevent instantiation%n" + "    }%n" + "}%n",
                enabled);
        writer.write(source);
    }

    /**
     * Gets a processor option value and falls back when absent.
     *
     * @param key      processor option key
     * @param fallback fallback value when the option is unset
     * @return option value or fallback
     */
    private String getOptionOrDefault(final String key, final String fallback)
    {
        final String value = processingEnv.getOptions().get(key);
        if (value == null)
            return fallback;
        return value.trim();
    }
}
