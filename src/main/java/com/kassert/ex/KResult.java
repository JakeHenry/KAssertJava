package com.kassert.ex;

public class KResult<T>
{
    /**
     * The value associated with this result, which may represent a success or
     * failure context depending on the result type.
     */
    protected T val;

    /**
     * The failure exception created by an implementation when this result is failed.
     */
    protected RuntimeException failureException;

    /**
     * Initializes a new result with the specified value.
     * 
     * @param val the value associated with this result
     */
    public KResult(T val)
    {
        this(val, null);
    }

    /**
     * Initializes a new result with the specified value and failure exception.
     *
     * @param val the value associated with this result
     * @param failureException the failure exception captured at assertion time
     */
    public KResult(T val, RuntimeException failureException)
    {
        this.val = val;
        this.failureException = failureException;
    }

    /**
     * Returns the value associated with this result.
     * 
     * @return the result value
     */
    public T val()
    {
        return val;
    }

    /**
     * Indicates whether this result represents a success.
     * 
     * @return {@code true} when this result is a success
     */
    public boolean ok()
    {
        return this instanceof KOk;
    }

    /**
     * Indicates whether this result represents a failure.
     * 
     * @return {@code true} when this result is a failure
     */
    public boolean failed()
    {
        return this instanceof KFailed;
    }

    /**
     * Throws the original implementation exception when this result is failed.
     *
     * @return this result for chaining
     * @throws RuntimeException when this result is a failure
     */
    public KResult<T> throwIfFailed() throws RuntimeException
    {
        if (failed())
        {
            if (failureException != null)
            {
                throw failureException;
            }
            throw new RuntimeException(String.format("Assertion failed! Result value: %s", val));
        }
        return this;
    }
}
