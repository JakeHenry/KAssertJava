package com.kassert.ex;

import java.util.function.Consumer;

/**
 * Base result type returned by KAssert operations.
 *
 * @param <T> value type carried by the result
 */
public class KResult<T>
{
    /**
     * The value associated with this result, which may represent a success or
     * failure context depending on the result type.
     */
    protected T val;

    /**
     * The failure exception created by an implementation when this result is
     * failed.
     */
    protected RuntimeException failureException;

    /**
     * Initializes a new result with the specified value.
     * 
     * @param val the value associated with this result
     */
    public KResult(final T val)
    {
        this(val, null);
    }

    /**
     * Initializes a new result with the specified value and failure exception.
     *
     * @param val              the value associated with this result
     * @param failureException the failure exception captured at assertion time
     */
    public KResult(final T val, final RuntimeException failureException)
    {
        this.val = val;
        this.failureException = failureException;
    }

    /**
     * Executes the provided error handler if this result represents a failure.
     *
     * @param errHandler the error handler to execute
     * @return this result for chaining
     */
    public KResult<T> onErr(final Consumer<RuntimeException> errHandler)
    {
        if (err() && failureException != null)
        {
            errHandler.accept(failureException);
        }
        return this;
    }

    /**
     * Executes the provided success handler if this result represents a success.
     *
     * @param okHandler the success handler to execute
     * @return this result for chaining
     */
    public KResult<T> onOk(final Consumer<T> okHandler)
    {
        if (ok())
        {
            okHandler.accept(val);
        }
        return this;
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
        return this instanceof KSuccess;
    }

    /**
     * Indicates whether this result represents a failure.
     * 
     * @return {@code true} when this result is a failure
     */
    public boolean err()
    {
        return this instanceof KFailed;
    }

    /**
     * Throws the original implementation exception when this result is a failure.
     *
     * @return this result for chaining
     * @throws RuntimeException when this result is a failure
     */
    public KResult<T> throwIfErr() throws RuntimeException
    {
        if (err())
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
