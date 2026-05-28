package com.kassert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coordinates supplementary failure handlers and the built-in popup handler.
 * 
 * @author Jacob Henry
 */
public final class KFailureHandlerDispatcher
{
    /** Singleton instance of the dispatcher. */
    public static final KFailureHandlerDispatcher INSTANCE = new KFailureHandlerDispatcher();

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(KFailureHandlerDispatcher.class.getName());

    /** Executor for running supplementary handlers in separate threads. */
    private final Executor supplementaryExecutor;

    /** List of registered supplementary failure handlers. */
    private final List<KAssertionFailureHandler> supplementaryHandlers;

    /** Built-in popup failure handler. */
    private final KAssertionFailureHandler popupHandler;

    /**
     * Creates a new dispatcher with the default popup handler and an executor for
     * running supplementary handlers in separate threads.
     */
    private KFailureHandlerDispatcher()
    {
        supplementaryExecutor = createDefaultExecutor();
        supplementaryHandlers = new ArrayList<KAssertionFailureHandler>();
        popupHandler = new KPopupDialogFailureHandler();
    }

    /**
     * Registers a supplementary failure handler to be invoked on assertion failures
     * in debug mode. Supplementary handlers are invoked before the built-in popup
     * handler, and exceptions thrown by supplementary handlers are caught and
     * logged, but do not prevent the popup handler from being invoked.
     * 
     * @param handler the supplementary handler to register
     */
    public synchronized void registerSupplementaryHandler(final KAssertionFailureHandler handler)
    {
        if (handler == null) throw new IllegalArgumentException("handler must not be null");
        if (handler instanceof KPopupDialogFailureHandler) return; // no duplicates of the popup handler allowed
        supplementaryHandlers.add(handler);
    }

    /**
     * Dispatches the given failure context to all registered supplementary handlers
     * and the built-in popup handler. Exceptions thrown by supplementary handlers
     * are caught and logged, but do not prevent the popup handler from being
     * invoked.
     * 
     * @param context the context to pass to the handlers when they run
     */
    public void dispatchDebugFailure(final KAssertionFailureContext context)
    {
        if (context == null) throw new IllegalArgumentException("context must not be null");

        final List<KAssertionFailureHandler> handlersToRun = getSupplementaryHandlersSnapshot();
        for (KAssertionFailureHandler handler : handlersToRun)
        {
            if (handler == null) continue;
            scheduleSupplementaryHandler(handler, context);
        }

        popupHandler.onFailure(context);
    }

    /**
     * Gets a snapshot of the registered supplementary handlers.
     * 
     * @return a list of the registered supplementary handlers at the time of this
     *         call
     */
    private synchronized List<KAssertionFailureHandler> getSupplementaryHandlersSnapshot()
    {
        return new ArrayList<KAssertionFailureHandler>(supplementaryHandlers);
    }

    /**
     * Clears all registered supplementary handlers. This is primarily intended for
     * testing purposes.
     */
    synchronized void clearSupplementaryHandlers()
    {
        supplementaryHandlers.clear();
    }

    /**
     * Schedules the given handler to run with the given context, catching and
     * logging any exceptions thrown by the
     * 
     * @param handler the handler to schedule
     * @param context the context to pass to the handler when it runs
     */
    private void scheduleSupplementaryHandler(final KAssertionFailureHandler handler,
            final KAssertionFailureContext context)
    {
        try
        {
            supplementaryExecutor.execute(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        handler.onFailure(context);
                    }
                    catch (RuntimeException error)
                    {
                        LOGGER.log(Level.SEVERE, "Supplementary assertion failure handler threw an exception.", error);
                    }
                }
            });
        }
        catch (RuntimeException error)
        {
            LOGGER.log(Level.SEVERE, "Failed to schedule supplementary assertion failure handler.", error);
        }
    }

    /**
     * Creates the default executor for running supplementary handlers.
     *
     * @return the default executor
     */
    private static Executor createDefaultExecutor()
    {
        final AtomicInteger threadCounter = new AtomicInteger(1);
        return Executors.newCachedThreadPool(new ThreadFactory()
        {
            public Thread newThread(final Runnable runnable)
            {
                final Thread worker = new Thread(runnable,
                        "kassert-failure-handler-" + threadCounter.getAndIncrement());
                worker.setDaemon(true);
                return worker;
            }
        });
    }
}
