package com.kassert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

/**
 * Coordinates supplementary failure handlers and the built-in popup handler.
 * 
 * @author Jacob Henry
 */
public final class KFailureDispatcher
{

    /** Singleton instance of the dispatcher. */
    static final KFailureDispatcher INSTANCE = new KFailureDispatcher();

    /** Logger for this class. */
    private static final Logger LOGGER = Logger
            .getLogger(KFailureDispatcher.class.getName());

    /** Property for whether to crash the JVM on assertion failures. */
    private static final String CRASH_ON_FAILURE_PROPERTY = System.getProperty(
            "kassert.crashOnFailure",
            java.awt.GraphicsEnvironment.isHeadless() ? "true" : "false");

    /** Whether to crash the JVM on assertion failures. */
    private static final boolean CRASH_ON_FAILURE = Boolean
            .parseBoolean(CRASH_ON_FAILURE_PROPERTY);

    /** Property for whether to disable the built-in popup handler. */
    private static final String DISABLE_POPUP_HANDLER_PROPERTY = System
            .getProperty("kassert.disablePopupHandler", "false");

    /** Whether to disable the built-in popup handler. */
    private static final boolean DISABLE_POPUP_HANDLER = Boolean
            .parseBoolean(DISABLE_POPUP_HANDLER_PROPERTY);

    /**
     * Thread factory for running supplementary handlers in separate threads.
     */
    private final ThreadFactory supplementaryHandlerThreadFactory = createDefaultThreadFactory();

    /** List of registered supplementary failure handlers. */
    private final List<KAssertionFailureHandler> supplementaryHandlers;

    /** Built-in popup failure handler. */
    private final KAssertionFailureHandler popupHandler;

    /**
     * Creates a new dispatcher with the default popup handler and an executor
     * for running supplementary handlers in separate threads.
     */
    private KFailureDispatcher()
    {
        supplementaryHandlers = new ArrayList<KAssertionFailureHandler>();
        popupHandler = new KPopupDialogFailureHandler(JOptionPane.ERROR_MESSAGE,
                "Assertion Failure");
    }

    /**
     * Registers a supplementary failure handler to be invoked on assertion
     * failures in debug mode. Supplementary handlers are invoked before the
     * built-in popup` handler, and exceptions thrown by supplementary handlers
     * are caught and logged, but do not prevent the popup handler from being
     * invoked.
     * 
     * @param handler the supplementary handler to register
     */
    public synchronized void registerSupplementaryHandler(
            final KAssertionFailureHandler handler)
    {
        if (handler == null)
            throw new IllegalArgumentException("handler must not be null");
        if (handler instanceof KPopupDialogFailureHandler)
            return; // no duplicates of the popup handler allowed
        supplementaryHandlers.add(handler);
    }

    /**
     * Dispatches the given failure context to all registered supplementary
     * handlers and the built-in popup handler. Exceptions thrown by
     * supplementary handlers are caught and logged, but do not prevent the
     * popup handler from being invoked.
     * 
     * @param context the context to pass to the handlers when they run
     */
    public void dispatchDebugFailure(final KAssertionFailureContext context)
    {
        Objects.requireNonNull(context, "context must not be null");

        final List<KAssertionFailureHandler> handlersToRun;
        handlersToRun = getSuppHandlersSnapshot();
        final Thread[] supplementaryHandlerThreads;
        supplementaryHandlerThreads = new Thread[handlersToRun.size()];
        for (int i = 0, l = handlersToRun.size(); i < l; i++)
        {
            final KAssertionFailureHandler handler;
            handler = handlersToRun.get(i);
            final Thread registeredHandler;
            registeredHandler = scheduleSuppHandler(handler, context);
            supplementaryHandlerThreads[i] = registeredHandler;
        }

        if (!DISABLE_POPUP_HANDLER)
        {
            popupHandler.onFailure(context); // blocking
        }

        if (CRASH_ON_FAILURE)
        {
            for (int i = 0, l = supplementaryHandlerThreads.length; i < l; i++)
            {
                final Thread thread = supplementaryHandlerThreads[i];
                try
                {
                    thread.join(); // wait until done
                }
                catch (InterruptedException e)
                {
                    final String msg;
                    msg = "Interrupted while waiting for supplementary handler"
                            + " thread to finish: " + thread.getName();
                    LOGGER.log(Level.WARNING, msg, e);

                    Thread.currentThread().interrupt();
                }
            }
            System.exit(1);
        }
    }

    /**
     * Gets a snapshot of the registered supplementary handlers.
     * 
     * @return a list of the registered supplementary handlers at the time of
     *         this call
     */
    private synchronized List<KAssertionFailureHandler> getSuppHandlersSnapshot()
    {
        return new ArrayList<KAssertionFailureHandler>(supplementaryHandlers);
    }

    /**
     * Clears all registered supplementary handlers. This is primarily intended
     * for testing purposes.
     */
    synchronized void clearSuppHandlers()
    {
        supplementaryHandlers.clear();
    }

    /**
     * Schedules the given handler to run with the given context, catching and
     * logging any exceptions thrown by the handler.
     * 
     * @param handler the handler to schedule
     * @param context the context to pass to the handler when it runs
     */
    private Thread scheduleSuppHandler(final KAssertionFailureHandler handler,
            final KAssertionFailureContext context)
    {
        final Thread worker = supplementaryHandlerThreadFactory.newThread(() ->
        {
            try
            {
                handler.onFailure(context);
            }
            catch (Exception e)
            {
                final String msg;
                msg = "Exception thrown by supplementary failure handler: "
                        + handler;
                LOGGER.log(Level.SEVERE, msg, e);
            }
        });
        worker.start();
        return worker;
    }

    /**
     * Creates the default thread factory for running supplementary handlers in
     * separate threads.
     * 
     * @return the default thread factory
     */
    private ThreadFactory createDefaultThreadFactory()
    {
        final AtomicInteger threadCounter = new AtomicInteger(1);
        return runnable ->
        {
            final String tName = "kassert-supplementary-handler-"
                    + threadCounter.getAndIncrement();
            final Thread worker = new Thread(runnable, tName);
            worker.setDaemon(true);
            return worker;
        };
    }
}
