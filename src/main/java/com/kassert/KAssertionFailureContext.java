package com.kassert;

/**
 * Immutable context for a KAssert assertion failure event.
 */
public final class KAssertionFailureContext
{
    private final RuntimeException assertionError;
    private final long timestampMillis;
    private final String threadName;
    private final long threadId;

    public KAssertionFailureContext(final RuntimeException assertionError)
    {
        if (assertionError == null)
        {
            throw new IllegalArgumentException("assertionError must not be null");
        }

        final Thread currentThread = Thread.currentThread();
        this.assertionError = assertionError;
        this.timestampMillis = System.currentTimeMillis();
        this.threadName = currentThread.getName();
        this.threadId = currentThread.getId();
    }

    public RuntimeException assertionError()
    {
        return assertionError;
    }

    public long timestampMillis()
    {
        return timestampMillis;
    }

    public String threadName()
    {
        return threadName;
    }

    public long threadId()
    {
        return threadId;
    }
}
