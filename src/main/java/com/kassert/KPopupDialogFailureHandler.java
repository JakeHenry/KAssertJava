package com.kassert;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

/**
 * Built-in popup dialog assertion failure handler used in KAssert debug mode.
 */
public final class KPopupDialogFailureHandler
        implements KAssertionFailureHandler
{
    /** Logger for popup handling failures. */
    private static final Logger LOGGER = Logger
            .getLogger(KPopupDialogFailureHandler.class.getName());

    /** Timeout for the dialog in milliseconds. */
    private long dialogTimeoutMillis;

    /**
     * The type of dialog to show. @see JOptionPane message types (e.g.
     * JOptionPane.ERROR_MESSAGE)
     */
    private int dialogType;

    /** The title of the dialog. */
    private String dialogTitle;

    /**
     * Constructs a KPopupDialogFailureHandler with no timeout.
     * 
     * @param dialogType  the type of dialog to show @see JOptionPane message
     *                    types (e.g. JOptionPane.ERROR_MESSAGE)
     * @param dialogTitle the title of the dialog (if null or empty, a default
     *                    title based on the dialog type will be used)
     */
    public KPopupDialogFailureHandler(final int dialogType,
            final String dialogTitle)
    {
        final String defaultDialogTitle;
        switch (dialogType)
        {
        case JOptionPane.ERROR_MESSAGE:
            defaultDialogTitle = "Error";
            break;
        case JOptionPane.INFORMATION_MESSAGE:
            defaultDialogTitle = "Information";
            break;
        case JOptionPane.WARNING_MESSAGE:
            defaultDialogTitle = "Warning";
            break;
        case JOptionPane.QUESTION_MESSAGE:
            defaultDialogTitle = "Question";
            break;
        case JOptionPane.PLAIN_MESSAGE:
            defaultDialogTitle = "Message";
            break;
        default:
            throw new IllegalArgumentException(
                    "Invalid dialogType: " + dialogType);
        }
        this.dialogType = dialogType;
        this.dialogTitle = dialogTitle != null && !dialogTitle.isEmpty()
                ? dialogTitle
                : defaultDialogTitle;
        this.dialogTimeoutMillis = 0; // No timeout by default
    }

    /**
     * Constructs a KPopupDialogFailureHandler with the specified dialog
     * timeout.
     *
     * @param dialogType          the type of dialog to show @see JOptionPane
     *                            message types (e.g. JOptionPane.ERROR_MESSAGE)
     * @param dialogTitle         the title of the dialog (if null or empty, a
     *                            default title based on the dialog type will be
     *                            used)
     * @param dialogTimeoutMillis the timeout for the dialog in milliseconds
     */
    public KPopupDialogFailureHandler(final int dialogType,
            final String dialogTitle, final long dialogTimeoutMillis)
    {
        this(dialogType, dialogTitle);
        this.dialogTimeoutMillis = dialogTimeoutMillis;
    }

    /**
     * Handles an assertion failure by showing a modal debug dialog.
     *
     * @param context assertion failure context
     */
    @Override
    public void onFailure(final KAssertionFailureContext context)
    {
        if (GraphicsEnvironment.isHeadless())
            return; // Cannot show dialog in headless environment
        if (context == null)
            throw new IllegalArgumentException("context must not be null");
        if (context.err() == null)
            throw new IllegalStateException("context.error() must not be null");

        final Runnable showDialog = () ->
        {
            final RuntimeException error = context.err();
            final JTextArea area = new JTextArea(
                    buildDialogText(context, error));
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setCaretPosition(0);
            final JScrollPane scrollPane = new JScrollPane(area);

            final String[] options =
            { "Continue", "Exit JVM" };
            final JOptionPane optionPane = new JOptionPane(scrollPane,
                    dialogType, JOptionPane.DEFAULT_OPTION, null, options,
                    options[0]);
            final JDialog dialog = optionPane.createDialog(null, dialogTitle);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(600, 400);
            dialog.setResizable(true);
            dialog.setLocationRelativeTo(null);
            dialog.setAlwaysOnTop(true);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

            // Start a timer to automatically close the dialog after the
            // specified timeout,
            // if enabled
            if (dialogTimeoutMillis > 0)
            {
                Timer timer = new Timer((int) dialogTimeoutMillis, e ->
                {
                    if (dialog != null && dialog.isShowing())
                    {
                        dialog.dispose();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }

            dialog.setVisible(true);

            final Object selectedValue = optionPane.getValue();
            if (options[1].equals(selectedValue))
            {
                System.exit(1);
            }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread())
        {
            showDialog.run();
        }
        else
        {
            try
            {
                java.awt.EventQueue.invokeAndWait(showDialog);
            }
            catch (Exception e)
            {
                LOGGER.log(Level.SEVERE,
                        "Failed to show assertion failure dialog.", e);
            }
        }
    }

    /**
     * Builds the text to be shown in the assertion failure dialog, including
     * the error message and stack trace.
     * 
     * @param context failure context metadata
     * @param error   the RuntimeException representing the assertion failure
     * @return a string containing the error message and stack trace formatted
     *         for display in the dialog.
     */
    private String buildDialogText(final KAssertionFailureContext context,
            final RuntimeException error)
    {
        if (context == null)
            throw new IllegalArgumentException("context must not be null");
        if (error == null)
            throw new IllegalArgumentException("error must not be null");

        final StringBuilder builder = new StringBuilder();
        builder.append(error.toString());
        builder.append('\n');
        builder.append('\n');
        builder.append("Timestamp: ");
        builder.append(formatTimestamp(context.timestampMillis()));
        builder.append('\n');
        builder.append("Thread: ");
        builder.append(context.threadName());
        builder.append('\n');
        builder.append("Thread ID: ");
        builder.append(context.threadId());
        builder.append('\n');
        builder.append('\n');
        builder.append("Stack trace:");
        builder.append('\n');

        final StackTraceElement[] stack = error.getStackTrace();
        for (final StackTraceElement element : stack)
        {
            builder.append("at ");
            builder.append(element.toString());
            builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * Formats a millisecond epoch timestamp for human-readable display.
     *
     * @param timestampMillis timestamp in epoch milliseconds
     * @return formatted timestamp string
     */
    private String formatTimestamp(final long timestampMillis)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z(Z)")
                .format(new Date(timestampMillis));
    }
}
