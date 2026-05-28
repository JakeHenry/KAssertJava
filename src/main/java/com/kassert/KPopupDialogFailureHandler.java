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

/**
 * Built-in popup dialog assertion failure handler used in KAssert debug mode.
 */
public final class KPopupDialogFailureHandler implements KAssertionFailureHandler
{
    private static final boolean CRASH_ON_HEADLESS_FAILURE = Boolean.getBoolean("kassert.cohf");
    private static final Logger LOGGER = Logger.getLogger(KPopupDialogFailureHandler.class.getName());

    public void onFailure(final KAssertionFailureContext context)
    {
        if (context == null) throw new IllegalArgumentException("context must not be null");
        if (context.assertionError() == null)
            throw new IllegalStateException("context.assertionError() must not be null");
        if (GraphicsEnvironment.isHeadless() && CRASH_ON_HEADLESS_FAILURE) System.exit(1);

        final Runnable showDialog = new Runnable()
        {
            @Override
            public void run()
            {
                final RuntimeException error = context.assertionError();
                final JTextArea area = new JTextArea(buildDialogText(context, error));
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
                LOGGER.log(Level.SEVERE, "Failed to show assertion failure dialog.", e);
                if (CRASH_ON_HEADLESS_FAILURE) System.exit(1);
            }
        }
    }

    /**
     * Builds the text to be shown in the assertion failure dialog, including the
     * error message and stack trace.
     * 
     * @param error The RuntimeException representing the assertion failure.
     * @return A string containing the error message and stack trace formatted for
     *         display in the dialog.
     */
    private String buildDialogText(final KAssertionFailureContext context, final RuntimeException error)
    {
        if (context == null) throw new IllegalArgumentException("context must not be null");
        if (error == null) throw new IllegalArgumentException("error must not be null");

        final StringBuilder builder = new StringBuilder();
        builder.append(error.toString());
        builder.append('\n');
        builder.append('\n');
        builder.append("Timestamp: ");
        builder.append(formatTimestamp(context.timestampMillis()));
        builder.append(" (");
        builder.append(context.timestampMillis());
        builder.append(" ms)");
        builder.append('\n');
        builder.append("Thread: ");
        builder.append(context.threadName());
        builder.append(" (#");
        builder.append(context.threadId());
        builder.append(")");
        builder.append('\n');
        builder.append('\n');
        builder.append("Stack trace:");
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

    private String formatTimestamp(final long timestampMillis)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(timestampMillis));
    }
}
