package com.kassert.ex;

/**
 * Successful KAssert result.
 *
 * @param <T> result value type
 */
public class KSuccess<T> extends KResult<T>
{
    /**
     * Creates a successful result containing the supplied value.
     *
     * @param result successful result value
     */
    public KSuccess(final T result)
    {
        super(result);
    }
}
