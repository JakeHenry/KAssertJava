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
 * Debug-mode KAssert implementation that shows a dialog on failures.
 */
final class KAssertDebugImplementation implements KAssertImplementation
{
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(KAssertDebugImplementation.class.getName());

    private static final int STACK_LINE_LIMIT = 1000;

    public KResult<Boolean> kRequire(final boolean condition, final String message)
    {
        return requireResult(condition, Boolean.valueOf(condition), message);
    }

    public KResult<Boolean> kRefuse(final boolean condition, final String message)
    {
        final boolean accepted = !condition;
        return requireResult(accepted, Boolean.valueOf(accepted), message);
    }

    public <T> KResult<T> kRequireEquals(final T expected, final Object actual, final String message)
    {
        return requireResult(areEqual(expected, actual), expected, message);
    }

    public <T> KResult<T> kRequireNotEquals(final T notExpected, final Object actual, final String message)
    {
        return requireResult(!areEqual(notExpected, actual), notExpected, message);
    }

    public <T> KResult<T> kRequireSame(final T expected, final Object actual, final String message)
    {
        return requireResult(expected == actual, expected, message);
    }

    public <T> KResult<T> kRequireNotSame(final T notExpected, final Object actual, final String message)
    {
        return requireResult(notExpected != actual, notExpected, message);
    }

    public <T> KResult<T> kRequireNull(final T object, final String message)
    {
        return requireResult(object == null, object, message);
    }

    public <T> KResult<T> kRequireNotNull(final T value, final String message)
    {
        return requireResult(value != null, value, message);
    }

    public <T> KResult<T> kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
    {
        if (expectedType == null)
        {
            return failedResult(object, "expectedType must not be null");
        }
        return requireResult(expectedType.isInstance(object), object, message);
    }

    public <T> KResult<T> kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
    {
        if (illegalType == null)
        {
            return failedResult(object, "illegalType must not be null");
        }
        return requireResult(!illegalType.isInstance(object), object, message);
    }

    /**
     * Compares two values for equality with null safety.
     *
     * @param left  the left value
     * @param right the right value
     * @return {@code true} when both values are equal
     */
    private boolean areEqual(final Object left, final Object right)
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

    private <T> KResult<T> requireResult(final boolean condition, final T value, final String message)
    {
        return condition ? new KOk<T>(value) : failedResult(value, message);
    }

    private <T> KResult<T> failedResult(final T value, final String message)
    {
        final RuntimeException error = createAssertionError(message);
        showFailureDialog(error);
        return new KFailed<T>(value, error);
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param message the failure message
     * @return the exception to throw
     */
    private RuntimeException createAssertionError(final String message)
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
    private void showFailureDialog(final RuntimeException error)
    {
        LOGGER.log(java.util.logging.Level.SEVERE, "Assertion failed: " + error.getMessage(), error);

        if (GraphicsEnvironment.isHeadless())
        {
            LOGGER.log(java.util.logging.Level.SEVERE,
                    "Headless environment detected, skipping failure dialog and exiting JVM.");
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
    private String buildDialogText(final RuntimeException error)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(error.toString());
        builder.append('\n');
        builder.append('\n');
        builder.append("Most recent stack trace lines:");
        builder.append('\n');

        final StackTraceElement[] stack = error.getStackTrace();
        int limit = STACK_LINE_LIMIT;
        if (stack.length < STACK_LINE_LIMIT)
        {
            limit = stack.length;
        }

        int i = 0;
        while (i < limit)
        {
            builder.append("at ");
            builder.append(stack[i].toString());
            builder.append('\n');
            i++;
        }
        return builder.toString();
    }
}
