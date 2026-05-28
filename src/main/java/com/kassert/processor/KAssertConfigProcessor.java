package com.kassert.processor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * <li>{@code kassert.className} (default:
 * {@code com.kassert.KAssertConfig})</li>
 * </ul>
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions(
{ "kassert.enabled", "kassert.className" })
public final class KAssertConfigProcessor extends AbstractProcessor
{
    private static final String OPTION_ENABLED = "kassert.enabled";
    private static final String DEFAULT_ENABLED = "false";
    private static final String DEFAULT_CLASS_NAME = "com.kassert.KAssertConfig";
    private boolean generated;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        generated = false;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
    {
        if (generated || roundEnv.processingOver())
        {
            return false;
        }

        final boolean enabled = Boolean.parseBoolean(getOptionOrDefault(OPTION_ENABLED, DEFAULT_ENABLED));
        Writer writer = null;
        try
        {
            final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(DEFAULT_CLASS_NAME);
            writer = sourceFile.openWriter();
            writeSource(writer, enabled);
            generated = true;
        }
        catch (FilerException ignored)
        {
            generated = true;
        }
        catch (IOException error)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate " + DEFAULT_CLASS_NAME + ": " + error.getMessage());
        } finally
        {
            closeQuietly(writer);
        }

        return false;
    }

    static void writeSource(final Writer writer, final boolean enabled) throws IOException
    {
        final String source = String.format(
            "package com.kassert;%n"
          + "final class KAssertConfig%n"
          + "{%n"
          + "    static final boolean ENABLED = %b;%n"
          + "    private KAssertConfig()%n"
          + "    {%n"
          + "        // prevent instantiation%n"
          + "    }%n"
          + "}%n",
            enabled);
        writer.write(source);
    }

    private String getOptionOrDefault(final String key, final String fallback)
    {
        final String value = processingEnv.getOptions().get(key);
        if (value == null)
        {
            return fallback;
        }
        return value.trim();
    }

    private static void closeQuietly(final Writer writer)
    {
        if (writer == null)
        {
            return;
        }
        try
        {
            writer.close();
        }
        catch (IOException ignored)
        {
            // no action required
        }
    }
}
