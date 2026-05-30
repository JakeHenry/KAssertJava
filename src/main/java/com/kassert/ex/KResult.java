package com.kassert.ex;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Result type with success and failure variants, where failure is always a
 * {@link RuntimeException}.
 *
 * <p>
 * A {@code KResult<T>} is either:
 * <ul>
 * <li><em>Ok</em> — carries a value of type {@code T}, including the special
 * {@link #empty()} form that carries {@code null}.</li>
 * <li><em>Err</em> — carries a {@link RuntimeException} describing the
 * failure.</li>
 * </ul>
 *
 * <p>
 * Create instances with {@link #ok(Object)} and
 * {@link #err(Class, RuntimeException)}. Query the variant with {@link #ok()} /
 * {@link #err()}, extract values with {@link #expect()} /
 * {@link #getOr(Object)}, and chain operations with {@link #map(Function)},
 * {@link #andThen(Function)}, etc.
 *
 * @param <T> the Ok value type
 */
public final class KResult<T>
{
    /** Non-null value on the Ok path; may be {@code null} for Ok(null). */
    private final T value;

    /**
     * The type of the Ok value, used for type inference in err() factory
     * method.
     */
    private final Class<T> type;

    /** Non-null exception on the Err path; {@code null} when Ok. */
    private final RuntimeException error;

    // -------------------------------------------------------------------------
    // Private constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new result instance.
     *
     * @param type  the Ok value type token
     * @param value the Ok value
     * @param error the Err exception
     */
    private KResult(final Class<T> type, final T value,
            final RuntimeException error)
    {
        this.value = value;
        this.type = type;
        this.error = error;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates an empty Ok result.
     *
     * <p>
     * Use this when success/failure matters but no value needs to be carried.
     *
     * @param <T> the Ok value type
     * @return an Ok {@code KResult} with a {@code null} value
     */
    @SuppressWarnings("unchecked")
    public static <T> KResult<T> empty()
    {
        return new KResult<T>((Class<T>) Object.class, null, null);
    }

    /**
     * Creates an Ok result containing {@code value}.
     *
     * @param <T>   the Ok value type
     * @param value the success value
     * @return an Ok {@code KResult}
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> KResult<T> ok(final T value)
    {
        Objects.requireNonNull(value, "Ok value must not be null");
        return new KResult<T>((Class<T>) value.getClass(), value, null);
    }

    /**
     * Creates an Err result carrying {@code error}.
     *
     * @param <T>   the Ok value type token used when the result is propagated
     * @param type  the Ok value type token
     * @param error the failure exception
     * @return an Err {@code KResult}
     * @throws NullPointerException if {@code type} or {@code error} is
     *                              {@code null}
     */
    public static <T> KResult<T> err(final Class<T> type,
            final RuntimeException error)
    {
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(error, "Exception must not be null");
        return new KResult<T>(type, (T) null, error);
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this result is Ok.
     *
     * @return {@code true} when Ok
     */
    public boolean ok()
    {
        return !err();
    }

    /**
     * Returns {@code true} if this result is Err.
     *
     * @return {@code true} when Err
     */
    public boolean err()
    {
        return error != null;
    }

    /**
     * Returns {@code true} if this result is Ok and {@code predicate} holds for
     * the contained value.
     *
     * @param predicate the predicate to test; must not be {@code null}
     * @return {@code true} when Ok and {@code predicate.test(value)} is
     *         {@code true}
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public boolean isOkAnd(final Predicate<T> predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (!ok())
            return false;
        return predicate.test(value);
    }

    /**
     * Returns {@code true} if this result is Err and {@code predicate} holds
     * for the contained exception.
     *
     * @param predicate the predicate to test; must not be {@code null}
     * @return {@code true} when Err and {@code predicate.test(error)} is
     *         {@code true}
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    public boolean isErrAnd(final Predicate<RuntimeException> predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (!err())
            return false;
        return predicate.test(error);
    }

    // -------------------------------------------------------------------------
    // Value extraction
    // -------------------------------------------------------------------------

    /**
     * Returns the Ok value, or throws if this result is Err.
     *
     * @return the Ok value
     * @throws RuntimeException if this result is Err
     */
    public T expect()
    {
        if (err())
            throw error;
        return value;
    }

    /**
     * Returns the Err exception, or throws with {@code message} as the detail
     * if this result is Ok.
     *
     * @param message the detail message for the thrown exception; must not be
     *                {@code null}
     * @return the Err exception
     * @throws IllegalStateException if this result is Ok
     * @throws NullPointerException  if {@code message} is {@code null}
     */
    public RuntimeException expectErr(final String message)
    {
        Objects.requireNonNull(message, "expectErr() message must not be null");
        if (ok())
            throw new IllegalStateException(message + ": " + value);
        return error;
    }

    /**
     * Returns the Ok value, or {@code defaultValue} if this result is Err.
     *
     * @param defaultValue the fallback value
     * @return the Ok value, or {@code defaultValue}
     */
    public T getOr(final T defaultValue)
    {
        return getOrElse(e -> defaultValue);
    }

    /**
     * Returns the Ok value, or computes a default from the Err exception using
     * {@code fn}.
     *
     * @param fn the fallback function; must not be {@code null}
     * @return the Ok value, or the result of {@code fn.apply(error)}
     * @throws NullPointerException if {@code fn} is {@code null}
     */
    public T getOrElse(final Function<RuntimeException, T> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (ok())
            return value;
        return fn.apply(error);
    }

    // -------------------------------------------------------------------------
    // Transformation
    // -------------------------------------------------------------------------

    /**
     * Applies {@code mapper} to the Ok value and wraps the result in Ok, or
     * propagates the Err unchanged.
     *
     * @param <U>    the mapped Ok value type
     * @param mapper the mapping function
     * @return the mapped Ok result, or the original Err as {@code KResult<U>}
     * @throws NullPointerException if {@code mapper} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public <U> KResult<? extends U> map(final Function<T, U> mapper)
    {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (!ok())
            return err((Class<U>) Object.class, error);
        U mapped = mapper.apply(value);
        return mapped == null ? empty() : ok(mapped);
    }

    /**
     * Applies {@code mapper} to the Err exception and wraps the result in Err,
     * or propagates the Ok unchanged.
     *
     * @param mapper the error-mapping function
     * @return the mapped Err result, or the original Ok
     * @throws NullPointerException if {@code mapper} is {@code null} or returns
     *                              {@code null}
     */
    public KResult<T> mapErr(
            final Function<RuntimeException, RuntimeException> mapper)
    {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (!err())
            return this;
        RuntimeException mapped = mapper.apply(error);
        Objects.requireNonNull(mapped, "mapper must not return null");
        return err(type, mapped);
    }

    /**
     * Returns {@code defaultValue} if Err, or applies {@code mapper} to the Ok
     * value.
     * 
     * @param mapper       the mapping function
     * @param defaultValue the fallback value
     *
     * @param <U>          the result type
     * @return the mapped Ok value, or {@code defaultValue}
     * @throws NullPointerException if {@code mapper} is {@code null}
     */
    public <U> U mapOr(final Function<T, U> mapper, final U defaultValue)
    {
        return mapOrElse(e -> defaultValue, mapper);
    }

    /**
     * Applies {@code okMapper} to the Ok value, or {@code errMapper} to the Err
     * exception.
     *
     * @param <U>       the result type
     * @param errMapper the function applied to the Err exception
     * @param okMapper  the function applied to the Ok value
     * @return the result of the appropriate mapping function
     * @throws NullPointerException if either mapper is {@code null}
     */
    public <U> U mapOrElse(final Function<RuntimeException, U> errMapper,
            final Function<T, U> okMapper)
    {
        Objects.requireNonNull(errMapper, "errMapper must not be null");
        Objects.requireNonNull(okMapper, "okMapper must not be null");
        if (ok())
            return okMapper.apply(value);
        return errMapper.apply(error);
    }

    // -------------------------------------------------------------------------
    // Boolean AND chaining
    // -------------------------------------------------------------------------

    /**
     * Returns {@code other} if this result is Ok, or propagates this Err.
     *
     * @param <U>   the Ok value type of the other result
     * @param other the result to return when Ok
     * @return {@code other} when Ok, or this Err propagated as
     *         {@code KResult<U>}
     * @throws NullPointerException if {@code other} is {@code null} and this
     *                              result is Err
     */
    public <U> KResult<U> and(final KResult<U> other)
    {
        Objects.requireNonNull(other, "other must not be null");
        return andThen(v -> other);
    }

    /**
     * Returns the result of applying {@code fn} to the Ok value, or propagates
     * this Err.
     *
     * @param <U> the Ok value type returned by {@code fn}
     * @param fn  the chaining function
     * @return the result of {@code fn}, or this Err propagated as
     *         {@code KResult<U>}
     * @throws NullPointerException if {@code fn} is {@code null} or returns
     *                              {@code null}
     */
    @SuppressWarnings("unchecked")
    public <U> KResult<U> andThen(final Function<T, KResult<U>> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (!ok())
            return err((Class<U>) Object.class, error);
        final KResult<U> result = fn.apply(value);
        Objects.requireNonNull(result, "andThen function must not return null");
        return result;
    }

    // -------------------------------------------------------------------------
    // Boolean OR chaining
    // -------------------------------------------------------------------------

    /**
     * Returns this result if Ok, or {@code other} if Err.
     *
     * <p>
     * Accepts {@code KResult<? extends T>} (covariant) so that a
     * {@code KResult<Integer>} can serve as fallback for a
     * {@code KResult<Number>}, and so that {@code err(e)} constructed via
     * static import is accepted without an explicit type witness.
     *
     * @param other the fallback result
     * @return this result if Ok, or {@code other} coerced to {@code KResult<T>}
     * @throws NullPointerException if {@code other} is {@code null} and this
     *                              result is Err
     */
    public KResult<? extends T> or(final KResult<? extends T> other)
    {
        return orElse(e -> other);
    }

    /**
     * Returns this result if Ok, or applies {@code fn} to the Err exception.
     *
     * <p>
     * Accepts a function returning {@code ? extends KResult<? extends T>}
     * (covariant) for the same reasons as {@link #or(KResult)}: allows subtype
     * results and {@code err(e)} via static import without an explicit type
     * witness.
     *
     * @param fn the recovery function
     * @return this result if Ok, or the result of {@code fn} coerced to
     *         {@code KResult<T>}
     * @throws NullPointerException if {@code fn} is {@code null} or returns
     *                              {@code null}
     */
    public KResult<? extends T> orElse(
            final Function<RuntimeException, ? extends KResult<? extends T>> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (ok())
            return this;
        final KResult<? extends T> result = fn.apply(error);
        Objects.requireNonNull(result, "orElse function must not return null");
        return result;
    }

    // -------------------------------------------------------------------------
    // Side-effect inspection
    // -------------------------------------------------------------------------

    /**
     * Calls {@code consumer} with the Ok value if this result is Ok, then
     * returns this result unchanged.
     *
     * <p>
     * Intended for side effects that observe the value (e.g. logging) without
     * modifying it. Callers must not mutate the contained value through the
     * consumer.
     *
     * @param consumer the observing consumer
     * @return this result
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    public KResult<T> inspect(final Consumer<T> consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        if (ok())
            consumer.accept(value);
        return this;
    }

    /**
     * Calls {@code consumer} with the Err exception if this result is Err, then
     * returns this result unchanged.
     *
     * <p>
     * Intended for side effects that observe the exception (e.g. logging)
     * without modifying it. Callers must not mutate the contained exception
     * through the consumer.
     *
     * @param consumer the observing consumer
     * @return this result
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    public KResult<T> inspectErr(final Consumer<RuntimeException> consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        if (err())
            consumer.accept(error);
        return this;
    }
}
