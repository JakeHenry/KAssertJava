package com.kassert.ex;

/**
 * Failed KAssert result.
 *
 * @param <T> result value type
 */
public class KFailed<T> extends KResult<T>
{
    /**
     * Creates a failed result with the captured assertion exception.
     *
     * @param result failed result value
     * @param failureException captured assertion exception
     */
    public KFailed(final T result, final RuntimeException failureException)
    {
        super(result, failureException);
    }
}
