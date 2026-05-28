package com.kassert.ex;

public class KSuccess<T> extends KResult<T>
{
    public KSuccess(T result)
    {
        super(result);
    }
}
