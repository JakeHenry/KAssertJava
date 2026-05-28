package com.kassert;

import com.kassert.ex.KFailed;
import com.kassert.ex.KOk;
import com.kassert.ex.KResult;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
    /**
     * Compile-time conditional compilation flag. This references
     * {@code KAssertConfig.ENABLED}, which is generated in client builds by
     * KAssert's annotation processor.
     */
    public static final boolean ENABLED = KAssertConfig.ENABLED;

    /**
     * When true, KAssert will call System.exit(1) on assertion failures in headless
     * environments. This is useful for CI environments where a failure dialog
     * cannot be shown and the process should terminate immediately. By default,
     * KAssert will not exit the JVM on headless failures to allow for easier
     * debugging and log capture. Can be set via the system property "kassert.cohf"
     * (e.g. -Dkassert.cohf=false).
     */
    private static final boolean CRASH_ON_HEADLESS_FAILURE;

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(KAssert.class.getName());

    static
    {
        final String cohfProperty = System.getProperty("kassert.cohf", "true");
        CRASH_ON_HEADLESS_FAILURE = Boolean.parseBoolean(cohfProperty);
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
     * @param condition the condition to evaluate
     * @param message   the failure message
     * @return the result of the requirement
     */
    public static KResult<Boolean> kRequire(final boolean condition, final String message)
    {
        if (!condition)
        {
            return failedResult(Boolean.valueOf(condition), message);
        }
        return new KOk<Boolean>(Boolean.valueOf(condition));
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
        if (condition)
        {
            return failedResult(Boolean.valueOf(!condition), message);
        }
        return new KOk<Boolean>(Boolean.valueOf(!condition));
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
        if (!areEqual(expected, actual))
        {
            return failedResult(actual, message);
        }
        return new KOk<K>(actual);
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
        if (areEqual(notExpected, actual))
        {
            return failedResult(actual, message);
        }
        return new KOk<K>(actual);
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
        if (expected != actual)
        {
            return failedResult(actual, message);
        }
        return new KOk<K>(actual);
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
        if (notExpected == actual)
        {
            return failedResult(actual, message);
        }
        return new KOk<K>(actual);
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
        if (object != null)
        {
            return failedResult(object, message);
        }
        return new KOk<T>(object);
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
        if (value == null)
        {
            return failedResult(value, message);
        }
        return new KOk<T>(value);
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
        if (expectedType == null)
        {
            return failedResult(object, "expectedType must not be null");
        }
        if (!expectedType.isInstance(object))
        {
            return failedResult(object, message);
        }
        return new KOk<T>(object);
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
        return new KOk<T>(object);
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
        if (ENABLED)
        {
            showFailureDialog(error);
        }
        return new KFailed<T>(value, error);
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param message the failure message
     * @return the exception to report
     */
    private static RuntimeException createAssertionError(final String message)
    {
        if (message == null)
        {
            return new RuntimeException("Assertion failed (No context information provided)");
        }
        return new RuntimeException(message);
    }

    /**
     * Displays the assertion failure dialog when the environment supports UI.
     *
     * @param error the assertion exception to display
     */
    private static void showFailureDialog(final RuntimeException error)
    {
        if (GraphicsEnvironment.isHeadless())
        {
            if (!CRASH_ON_HEADLESS_FAILURE)
            {
                return;
            }
            LOG.log(java.util.logging.Level.SEVERE, "Exiting JVM due to assertion failure.");
            System.exit(1);
        }

        final JTextArea area = new JTextArea(buildDialogText(error));
        area.setEditable(false);
        area.setLineWrap(false);
        area.setCaretPosition(0);
        final JScrollPane scrollPane = new JScrollPane(area);

        final String[] options = new String[]
        { "Continue", "Exit JVM" };
        final JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        final JDialog dialog = optionPane.createDialog(null, "KAssert Debug Assertion Failure");
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);

        final Object selectedValue = optionPane.getValue();
        if (options[1].equals(selectedValue))
        {
            System.exit(1);
        }
    }

    /**
     * Builds the text shown in the debug failure dialog.
     *
     * @param error the assertion exception
     * @return a formatted dialog message including recent stack trace lines
     */
    private static String buildDialogText(final RuntimeException error)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(error.toString());
        builder.append('\n');
        builder.append('\n');
        builder.append("Most recent stack trace lines:");
        builder.append('\n');

        final StackTraceElement[] stack = error.getStackTrace();
        int i = 0;
        while (i < stack.length)
        {
            builder.append("at ");
            builder.append(stack[i].toString());
            builder.append('\n');
            i++;
        }
        return builder.toString();
    }
}
