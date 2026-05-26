package com.kassert.ex;

public class KFailed<T> extends KResult<T>
{
    public KFailed(T result)
    {
        super(result);
    }

    public KFailed(T result, RuntimeException failureException)
    {
        super(result, failureException);
    }
}
