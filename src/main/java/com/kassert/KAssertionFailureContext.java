package com.kassert;

import java.util.Objects;

/**
 * Immutable context for a KAssert assertion failure event.
 */
public final class KAssertionFailureContext
{
    /** Assertion exception raised by the failed assertion. */
    private final RuntimeException assertionError;
    /** Failure timestamp in epoch milliseconds. */
    private final long timestampMillis;
    /** Name of the thread that observed the failure. */
    private final String threadName;
    /** Identifier of the thread that observed the failure. */
    private final long threadId;

    /**
     * Creates a new immutable assertion failure context.
     *
     * @param assertionError assertion exception raised by the failed assertion
     */
    public KAssertionFailureContext(final RuntimeException assertionError)
    {
        Objects.requireNonNull(assertionError, "assertionError cannot be null");

        final Thread currentThread = Thread.currentThread();
        this.assertionError = assertionError;
        this.timestampMillis = System.currentTimeMillis();
        this.threadName = currentThread.getName();
        this.threadId = currentThread.getId();
    }

    /**
     * Gets the assertion exception raised by the failed assertion.
     *
     * @return assertion exception for this failure context
     */
    public RuntimeException err()
    {
        return assertionError;
    }

    /**
     * Gets the failure timestamp in epoch milliseconds.
     *
     * @return failure timestamp in milliseconds since epoch
     */
    public long timestampMillis()
    {
        return timestampMillis;
    }

    /**
     * Gets the name of the thread that observed the failure.
     *
     * @return failure thread name
     */
    public String threadName()
    {
        return threadName;
    }

    /**
     * Gets the id of the thread that observed the failure.
     *
     * @return failure thread id
     */
    public long threadId()
    {
        return threadId;
    }
}
