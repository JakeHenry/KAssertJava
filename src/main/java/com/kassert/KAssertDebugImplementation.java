package com.kassert;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Debug-mode KAssert implementation that shows a dialog before throwing.
 */
final class KAssertDebugImplementation implements KAssertImplementation
{
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KAssertDebugImplementation.class.getName());

    private static final int STACK_LINE_LIMIT = 1000;

    /**
     * Requires the supplied condition to be {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the requirement succeeds
     * @throws IllegalStateException when the requirement fails
     */
    public boolean kRequire(final boolean condition, final String message) throws IllegalStateException
    {
        return assertCondition(condition, message);
    }

    /**
     * Requires the supplied condition to be {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the condition is false
     */
    public boolean kRefuse(final boolean condition, final String message)
    {
        return !condition;
    }

    /**
     * Requires {@code expected} and {@code actual} to be equal.
     *
     * @param <T> the expected type
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    @SuppressWarnings("unchecked")
    public <T> T kRequireEquals(final T expected, final Object actual, final String message) throws IllegalStateException
    {
        assertCondition(areEqual(expected, actual), message);
        return (T) actual;
    }

    /**
     * Requires {@code notExpected} and {@code actual} to be different.
     *
     * @param <T> the value type
     * @param notExpected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    @SuppressWarnings("unchecked")
    public <T> T kRequireNotEquals(final T notExpected, final Object actual, final String message)
            throws IllegalStateException
    {
        assertCondition(!areEqual(notExpected, actual), message);
        return (T) actual;
    }

    /**
     * Requires {@code expected} and {@code actual} to reference the same object.
     *
     * @param <T> the reference type
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    @SuppressWarnings("unchecked")
    public <T> T kRequireSame(final T expected, final Object actual, final String message) throws IllegalStateException
    {
        assertCondition(expected == actual, message);
        return (T) actual;
    }

    /**
     * Requires {@code notExpected} and {@code actual} to reference different objects.
     *
     * @param <T> the reference type
     * @param notExpected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    @SuppressWarnings("unchecked")
    public <T> T kRequireNotSame(final T notExpected, final Object actual, final String message)
            throws IllegalStateException
    {
        assertCondition(notExpected != actual, message);
        return (T) actual;
    }

    /**
     * Requires the supplied object to be {@code null}.
     *
     * @param <T> the object type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code null}
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNull(final T object, final String message) throws IllegalStateException
    {
        assertCondition(object == null, message);
        return null;
    }

    /**
     * Requires the supplied object to be non-null.
     *
     * @param <T> the object type
     * @param value the value to validate
     * @param message the failure message
     * @return the validated non-null value
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNotNull(final T value, final String message) throws IllegalStateException
    {
        assertCondition(value != null, message);
        return value;
    }

    /**
     * Requires the supplied object to be an instance of {@code expectedType}.
     *
     * @param <T> the object type
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireInstanceOf(final Class<?> expectedType, final T object, final String message)
            throws IllegalStateException
    {
        assertCondition(expectedType != null, "expectedType must not be null");
        assertCondition(expectedType.isInstance(object), message);
        return object;
    }

    /**
     * Requires the supplied object not to be an instance of {@code illegalType}.
     *
     * @param <T> the object type
     * @param illegalType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return the validated value
     * @throws IllegalStateException when the requirement fails
     */
    public <T> T kRequireNotInstanceOf(final Class<?> illegalType, final T object, final String message)
            throws IllegalStateException
    {
        assertCondition(illegalType != null, "illegalType must not be null");
        assertCondition(!illegalType.isInstance(object), message);
        return object;
    }

    /**
     * Asserts that the supplied condition is {@code true}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertTrue(final boolean condition, final String message)
    {
        return assertCheck(condition, message);
    }

    /**
     * Asserts that the supplied condition is {@code false}.
     *
     * @param condition the condition to evaluate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertFalse(final boolean condition, final String message)
    {
        return assertCheck(!condition, message);
    }

    /**
     * Asserts that {@code expected} and {@code actual} are equal.
     *
     * @param expected the expected value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertEquals(final Object expected, final Object actual, final String message)
    {
        return assertCheck(areEqual(expected, actual), message);
    }

    /**
     * Asserts that {@code expected} and {@code actual} are not equal.
     *
     * @param expected the disallowed value
     * @param actual the actual value
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotEquals(final Object expected, final Object actual, final String message)
    {
        return assertCheck(!areEqual(expected, actual), message);
    }

    /**
     * Asserts that {@code expected} and {@code actual} reference the same object.
     *
     * @param expected the expected reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertSame(final Object expected, final Object actual, final String message)
    {
        return assertCheck(expected == actual, message);
    }

    /**
     * Asserts that {@code expected} and {@code actual} reference different objects.
     *
     * @param expected the disallowed reference
     * @param actual the actual reference
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotSame(final Object expected, final Object actual, final String message)
    {
        return assertCheck(expected != actual, message);
    }

    /**
     * Asserts that the supplied object is {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNull(final Object object, final String message)
    {
        return assertCheck(object == null, message);
    }

    /**
     * Asserts that the supplied object is not {@code null}.
     *
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotNull(final Object object, final String message)
    {
        return assertCheck(object != null, message);
    }

    /**
     * Asserts that the supplied object is an instance of {@code expectedType}.
     *
     * @param expectedType the required runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertInstanceOf(final Class<?> expectedType, final Object object, final String message)
    {
        if (!assertCheck(expectedType != null, "expectedType must not be null"))
        {
            return false;
        }
        return assertCheck(expectedType.isInstance(object), message);
    }

    /**
     * Asserts that the supplied object is not an instance of {@code expectedType}.
     *
     * @param expectedType the disallowed runtime type
     * @param object the value to validate
     * @param message the failure message
     * @return {@code true} when the assertion succeeds
     */
    public boolean kAssertNotInstanceOf(final Class<?> expectedType, final Object object, final String message)
    {
        if (!assertCheck(expectedType != null, "expectedType must not be null"))
        {
            return false;
        }
        return assertCheck(!expectedType.isInstance(object), message);
    }

    /**
     * Compares two values for equality with null safety.
     *
     * @param left the left value
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

    /**
     * Validates a condition and fails with dialog + exception if false.
     *
     * @param condition the condition to validate
     * @param message the failure message
     * @return {@code true} when the condition is valid
     * @throws IllegalStateException when the condition is false
     */
    private boolean assertCondition(final boolean condition, final String message) throws IllegalStateException
    {
        if (!condition)
        {
            failWithDialog(message);
        }
        return true;
    }

    /**
     * Validates an assertion condition, showing a dialog on failure without throwing.
     *
     * @param condition the condition to validate
     * @param message the failure message
     * @return {@code true} when the condition is valid, otherwise {@code false}
     */
    private boolean assertCheck(final boolean condition, final String message)
    {
        if (!condition)
        {
            showFailureDialog(createAssertionError(message));
            return false;
        }
        return true;
    }

    /**
     * Shows failure information and throws the assertion exception.
     *
     * @param message the failure message
     * @throws IllegalStateException always thrown after dialog handling
     */
    private void failWithDialog(final String message) throws IllegalStateException
    {
        final IllegalStateException error = createAssertionError(message);
        showFailureDialog(error);
        throw error;
    }

    /**
     * Creates the assertion exception for a failed condition.
     *
     * @param message the failure message
     * @return the exception to throw
     */
    private IllegalStateException createAssertionError(final String message)
    {
        if (message == null)
        {
            return new IllegalStateException("Assertion failed (No context information provided)");
        }
        return new IllegalStateException(message);
    }

    /**
     * Displays the assertion failure dialog when the environment supports UI.
     *
     * @param error the assertion exception to display
     */
    private void showFailureDialog(final IllegalStateException error)
    {
        LOGGER.log(java.util.logging.Level.SEVERE, "Assertion failed: " + error.getMessage(), error);
        
        if (GraphicsEnvironment.isHeadless())
        {
            LOGGER.log(java.util.logging.Level.SEVERE, "Headless environment detected, skipping failure dialog and exiting JVM.");
            System.exit(1);
        }

        final JTextArea area = new JTextArea(buildDialogText(error));
        area.setEditable(false);
        area.setLineWrap(false);
        area.setCaretPosition(0);
        final JScrollPane scrollPane = new JScrollPane(area);

        final String[] options = new String[] { "Continue", "Exit JVM" };
        final JOptionPane optionPane =
                new JOptionPane(scrollPane, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
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
    private String buildDialogText(final IllegalStateException error)
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
