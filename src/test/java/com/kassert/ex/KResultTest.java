package com.kassert.ex;

import static com.kassert.ex.KResult.empty;
import static com.kassert.ex.KResult.err;
import static com.kassert.ex.KResult.ok;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Exhaustive unit tests for {@link KResult}. One test method per public method
 * in the class under test, targeting 100% code coverage of every branch and
 * edge case.
 */
public class KResultTest
{
    /**
     * Verifies {@link KResult#empty()}.
     */
    @Test
    public void testEmpty()
    {
        // Basic state: empty() is Ok with null value
        KResult<String> r = empty();
        at(r.ok());
        af(r.err());
        an(r.get());

        // Type flexibility: empty can be assigned to any type parameter
        KResult<Integer> ri = empty();
        at(ri.ok());
        an(ri.get());

        KResult<Object> ro = empty();
        at(ro.ok());
        an(ro.get());

        // Two calls produce independent instances
        KResult<String> r1 = empty();
        KResult<String> r2 = empty();
        ans(r1, r2);

        // Chaining: empty is Ok so transformations on the Ok path run
        KResult<? extends Integer> mapped = r.map(v -> 42);
        // v is null here so mapper receives null and returns 42
        at(mapped.ok());
        aeq(Integer.valueOf(42), mapped.get());

        // empty works with expect (returns null without throwing)
        an(r.expect("should not throw"));

        // empty throws on expectErr
        try
        {
            r.expectErr("should throw");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("should throw"));
        }
    }

    /**
     * Verifies {@link KResult#ok(Object)}.
     */
    @Test
    public void testOk()
    {
        // Basic string value
        KResult<String> rs = ok("hello");
        at(rs.ok());
        af(rs.err());
        aeq("hello", rs.get());

        // Integer value
        KResult<Integer> ri = ok(42);
        at(ri.ok());
        aeq(Integer.valueOf(42), ri.get());

        // Object value
        Object obj = new Object();
        KResult<Object> ro = ok(obj);
        as(obj, ro.get());

        // Empty string is a valid value
        KResult<String> empty = ok("");
        at(empty.ok());
        aeq("", empty.get());

        // Zero is a valid value
        KResult<Integer> zero = ok(0);
        at(zero.ok());
        aeq(Integer.valueOf(0), zero.get());

        // Negative value
        KResult<Integer> neg = ok(-1);
        at(neg.ok());
        aeq(Integer.valueOf(-1), neg.get());

        // Boolean value
        KResult<Boolean> bt = ok(Boolean.TRUE);
        at(bt.ok());
        aeq(Boolean.TRUE, bt.get());
        KResult<Boolean> bf = ok(Boolean.FALSE);
        at(bf.ok());
        aeq(Boolean.FALSE, bf.get());

        // Null argument throws NullPointerException
        try
        {
            ok(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            ann(e.getMessage());
            at(e.getMessage().contains("null"));
        }

        // Two ok() with equal values are independent instances
        KResult<String> r1 = ok("same");
        KResult<String> r2 = ok("same");
        ans(r1, r2);
        aeq(r1.get(), r2.get());

        // Subtype value: Integer stored as Number
        Number n = Integer.valueOf(7);
        @SuppressWarnings("unchecked")
        KResult<Number> rn = (KResult<Number>) (KResult<?>) ok(n);
        aeq(Integer.valueOf(7), rn.get());
    }

    /**
     * Verifies {@link KResult#err(Class, RuntimeException)}.
     */
    @Test
    public void testErr()
    {
        RuntimeException ex = new RuntimeException("fail");
        KResult<String> r = err(String.class, ex);

        // Basic state: err variant
        af(r.ok());
        at(r.err());

        // get() throws the stored exception
        try
        {
            r.get();
            fail("Expected RuntimeException");
        }
        catch (RuntimeException caught)
        {
            as(ex, caught);
        }

        // getErr() returns the exception
        as(ex, r.getErr());

        // Various exception subtypes
        IllegalArgumentException iae = new IllegalArgumentException("bad arg");
        KResult<Integer> r2 = err(Integer.class, iae);
        at(r2.err());
        as(iae, r2.getErr());

        IllegalStateException ise = new IllegalStateException("bad state");
        KResult<Object> r3 = err(Object.class, ise);
        at(r3.err());
        as(ise, r3.getErr());

        // Null type throws
        try
        {
            err(null, new RuntimeException("x"));
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("Type"));
        }

        // Null exception throws
        try
        {
            err(String.class, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("Exception"));
        }

        // Both null throws (type is checked first)
        try
        {
            err(null, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("Type"));
        }

        // Two err() with same params are independent
        KResult<String> e1 = err(String.class, ex);
        KResult<String> e2 = err(String.class, ex);
        ans(e1, e2);
    }

    /**
     * Verifies {@link KResult#ok()}.
     */
    @Test
    public void testOkQuery()
    {
        // Ok variant
        at(ok("x").ok());
        at(ok(1).ok());
        at(empty().ok());

        // Err variant
        af(err(String.class, new RuntimeException()).ok());
        af(err(Integer.class, new IllegalArgumentException()).ok());
    }

    /**
     * Verifies {@link KResult#err()}.
     */
    @Test
    public void testErrQuery()
    {
        // Err variant
        at(err(String.class, new RuntimeException()).err());
        at(err(Object.class, new IllegalStateException()).err());

        // Ok variant
        af(ok("x").err());
        af(ok(1).err());
        af(empty().err());
    }

    /**
     * Verifies {@link KResult#isOkAnd(java.util.function.Predicate)}.
     */
    @Test
    public void testIsOkAnd()
    {
        KResult<Integer> ok5 = ok(5);
        KResult<Integer> err = err(Integer.class, new RuntimeException("x"));
        KResult<String> empty = empty();

        // Null predicate throws
        try
        {
            ok5.isOkAnd(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("predicate"));
        }

        // Ok with predicate that returns true
        at(ok5.isOkAnd(v -> v == 5));
        at(ok5.isOkAnd(v -> v > 0));
        at(ok5.isOkAnd(v -> v < 10));

        // Ok with predicate that returns false
        af(ok5.isOkAnd(v -> v == 6));
        af(ok5.isOkAnd(v -> v < 0));
        af(ok5.isOkAnd(v -> v > 100));

        // Err variant: predicate never called, always false
        af(err.isOkAnd(v -> true));
        af(err.isOkAnd(v -> false));
        af(err.isOkAnd(v ->
        {
            fail("Should not be called");
            return true;
        }));

        // Empty (Ok with null value): predicate receives null
        at(empty.isOkAnd(v -> v == null));
        af(empty.isOkAnd(v -> v != null));

        // Predicate with complex logic
        KResult<String> okStr = ok("hello");
        at(okStr.isOkAnd(s -> s.length() == 5));
        af(okStr.isOkAnd(s -> s.startsWith("x")));
        at(okStr.isOkAnd(s -> s.contains("ell")));

        // Null predicate on err still throws (null check is first)
        try
        {
            err.isOkAnd(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("predicate"));
        }
    }

    /**
     * Verifies {@link KResult#isErrAnd(java.util.function.Predicate)}.
     */
    @Test
    public void testIsErrAnd()
    {
        RuntimeException rex = new RuntimeException("oops");
        IllegalArgumentException iae = new IllegalArgumentException("bad");
        KResult<String> errR = err(String.class, rex);
        KResult<String> errIae = err(String.class, iae);
        KResult<String> okR = ok("hello");
        KResult<String> emptyR = empty();

        // Null predicate throws
        try
        {
            errR.isErrAnd(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("predicate"));
        }

        // Err with predicate that returns true
        at(errR.isErrAnd(e -> e.getMessage().equals("oops")));
        at(errR.isErrAnd(e -> e instanceof RuntimeException));
        at(errIae.isErrAnd(e -> e instanceof IllegalArgumentException));

        // Err with predicate that returns false
        af(errR.isErrAnd(e -> e.getMessage().equals("other")));
        af(errR.isErrAnd(e -> e instanceof IllegalStateException));

        // Ok variant: predicate never called, always false
        af(okR.isErrAnd(e -> true));
        af(okR.isErrAnd(e -> false));
        af(okR.isErrAnd(e ->
        {
            fail("Should not be called");
            return true;
        }));

        // Empty (Ok): always false
        af(emptyR.isErrAnd(e -> true));

        // Null predicate on ok still throws (null check is first)
        try
        {
            okR.isErrAnd(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("predicate"));
        }
    }

    /**
     * Verifies {@link KResult#get()}.
     */
    @Test
    public void testGet()
    {
        // Ok value returned
        aeq("hello", ok("hello").get());
        aeq(Integer.valueOf(42), ok(42).get());

        // Empty returns null
        an(empty().get());

        // Err throws the exact stored exception
        RuntimeException ex = new RuntimeException("boom");
        KResult<String> errR = err(String.class, ex);
        try
        {
            errR.get();
            fail("Expected RuntimeException");
        }
        catch (RuntimeException caught)
        {
            as(ex, caught);
            aeq("boom", caught.getMessage());
        }

        // Err with subtype exception
        IllegalStateException ise = new IllegalStateException("state");
        KResult<Integer> errIse = err(Integer.class, ise);
        try
        {
            errIse.get();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException caught)
        {
            as(ise, caught);
        }
    }

    /**
     * Verifies {@link KResult#getErr()}.
     */
    @Test
    public void testGetErr()
    {
        // Err returns the exception
        RuntimeException ex = new RuntimeException("error");
        KResult<String> errR = err(String.class, ex);
        as(ex, errR.getErr());

        // Err with subtype
        IllegalArgumentException iae = new IllegalArgumentException("arg");
        KResult<Integer> errIae = err(Integer.class, iae);
        RuntimeException returned = errIae.getErr();
        as(iae, returned);
        at(returned instanceof IllegalArgumentException);

        // Ok throws IllegalStateException
        KResult<String> okR = ok("value");
        try
        {
            okR.getErr();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("getErr()"));
            at(e.getMessage().contains("Ok"));
            at(e.getMessage().contains("value"));
        }

        // Empty throws IllegalStateException
        KResult<Object> emptyR = empty();
        try
        {
            emptyR.getErr();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("getErr()"));
        }
    }

    /**
     * Verifies {@link KResult#expect(String)}.
     */
    @Test
    public void testExpect()
    {
        // Ok returns value
        aeq("hello", ok("hello").expect("should not throw"));
        aeq(Integer.valueOf(99), ok(99).expect("should not throw"));

        // Empty returns null
        an(empty().expect("should not throw"));

        // Err throws IllegalStateException with the provided message as detail
        RuntimeException original = new RuntimeException("original");
        KResult<String> errR = err(String.class, original);
        try
        {
            errR.expect("custom message");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            aeq("custom message", e.getMessage());
            as(original, e.getCause());
        }

        // Err with different message
        try
        {
            errR.expect("another message");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            aeq("another message", e.getMessage());
            as(original, e.getCause());
        }

        // Null message throws NullPointerException
        try
        {
            ok("x").expect(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("expect()"));
        }

        // Null message on err also throws NullPointerException (null check
        // first)
        try
        {
            errR.expect(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("expect()"));
        }

        // Empty string message is valid
        try
        {
            errR.expect("");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            aeq("", e.getMessage());
        }
    }

    /**
     * Verifies {@link KResult#expectErr(String)}.
     */
    @Test
    public void testExpectErr()
    {
        // Err returns the exception
        RuntimeException ex = new RuntimeException("fail");
        KResult<String> errR = err(String.class, ex);
        as(ex, errR.expectErr("should not throw"));

        // Ok throws IllegalStateException with message containing the value
        KResult<String> okR = ok("myvalue");
        try
        {
            okR.expectErr("expected error");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("expected error"));
            at(e.getMessage().contains("myvalue"));
        }

        // Ok with integer value
        KResult<Integer> okI = ok(42);
        try
        {
            okI.expectErr("not an error");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("not an error"));
            at(e.getMessage().contains("42"));
        }

        // Empty throws with null value representation
        KResult<Object> emptyR = empty();
        try
        {
            emptyR.expectErr("should fail");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("should fail"));
            at(e.getMessage().contains("null"));
        }

        // Null message throws NullPointerException
        try
        {
            errR.expectErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("expectErr()"));
        }

        // Null message on ok also throws NullPointerException (null check
        // first)
        try
        {
            okR.expectErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("expectErr()"));
        }

        // Empty string message is valid
        KResult<String> okE = ok("v");
        try
        {
            okE.expectErr("");
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            at(e.getMessage().contains("v"));
        }
    }

    /**
     * Verifies {@link KResult#getOr(Object)}.
     */
    @Test
    public void testGetOr()
    {
        // Ok returns the value, ignoring default
        aeq("hello", ok("hello").getOr("default"));
        aeq(Integer.valueOf(5), ok(5).getOr(99));

        // Empty (Ok null) returns null, not the default
        an(empty().getOr("default"));

        // Err returns the default
        KResult<String> errR = err(String.class, new RuntimeException());
        aeq("default", errR.getOr("default"));

        // Err with null default returns null
        an(errR.getOr(null));

        // Ok with null default still returns the value
        aeq("hello", ok("hello").getOr(null));

        // Err returns various defaults
        KResult<Integer> errI = err(Integer.class, new RuntimeException());
        aeq(Integer.valueOf(0), errI.getOr(0));
        aeq(Integer.valueOf(-1), errI.getOr(-1));
        aeq(Integer.valueOf(Integer.MAX_VALUE), errI.getOr(Integer.MAX_VALUE));
    }

    /**
     * Verifies {@link KResult#getOrElse(java.util.function.Function)}.
     */
    @Test
    public void testGetOrElse()
    {
        RuntimeException ex = new RuntimeException("problem");
        KResult<String> okR = ok("value");
        KResult<String> errR = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Ok returns value, function not called
        aeq("value", okR.getOrElse(e ->
        {
            fail("Should not be called on Ok");
            return "x";
        }));

        // Empty (Ok null) returns null, function not called
        an(emptyR.getOrElse(e ->
        {
            fail("Should not be called on empty Ok");
            return "x";
        }));

        // Err calls function with the exception
        AtomicReference<RuntimeException> captured = new AtomicReference<>();
        String result = errR.getOrElse(e ->
        {
            captured.set(e);
            return "fallback";
        });
        aeq("fallback", result);
        as(ex, captured.get());

        // Err function can return null
        an(errR.getOrElse(e -> null));

        // Err function receives exact exception type
        IllegalArgumentException iae = new IllegalArgumentException("bad");
        KResult<String> errIae = err(String.class, iae);
        errIae.getOrElse(e ->
        {
            as(iae, e);
            at(e instanceof IllegalArgumentException);
            return "recovered";
        });

        // Null function throws
        try
        {
            okR.getOrElse(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }

        // Null function on err also throws (null check before state check)
        try
        {
            errR.getOrElse(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }
    }

    /**
     * Verifies {@link KResult#map(java.util.function.Function)}.
     */
    @Test
    public void testMap()
    {
        KResult<String> okR = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errR = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Null mapper throws
        try
        {
            okR.map(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("mapper"));
        }

        // Ok: mapper transforms the value
        KResult<? extends Integer> mapped = okR.map(String::length);
        at(mapped.ok());
        aeq(Integer.valueOf(5), mapped.get());

        // Ok: mapper to different type
        KResult<? extends Boolean> boolMapped = okR.map(s -> s.startsWith("h"));
        at(boolMapped.ok());
        aeq(Boolean.TRUE, boolMapped.get());

        // Ok: mapper returns null -> produces empty Ok
        KResult<? extends Integer> nullMapped = okR.map(s -> null);
        at(nullMapped.ok());
        an(nullMapped.get());

        // Ok: identity-like mapping
        KResult<? extends String> identity = okR.map(s -> s);
        at(identity.ok());
        aeq("hello", identity.get());

        // Ok: mapper to same type but different value
        KResult<? extends String> upper = okR.map(String::toUpperCase);
        aeq("HELLO", upper.get());

        // Err: mapper is NOT called, err propagates
        KResult<? extends Integer> errMapped = errR.map(s ->
        {
            fail("Should not be called on Err");
            return 42;
        });
        at(errMapped.err());
        as(ex, errMapped.getErr());

        // Empty: mapper receives null
        KResult<? extends String> emptyMapped = emptyR.map(v -> "was null");
        at(emptyMapped.ok());
        aeq("was null", emptyMapped.get());

        // Chained maps
        KResult<? extends Integer> chained = okR.map(String::length)
                .map(n -> n * 2);
        at(chained.ok());
        aeq(Integer.valueOf(10), chained.get());

        // Null mapper on err still throws (null check first)
        try
        {
            errR.map(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("mapper"));
        }
    }

    /**
     * Verifies {@link KResult#mapErr(java.util.function.Function)}.
     */
    @Test
    public void testMapErr()
    {
        RuntimeException original = new RuntimeException("original");
        RuntimeException replacement = new RuntimeException("replaced");
        KResult<String> okR = ok("hello");
        KResult<String> errR = err(String.class, original);
        KResult<String> emptyR = empty();

        // Null mapper throws
        try
        {
            errR.mapErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("mapper"));
        }

        // Ok: mapper not called, returns same instance
        KResult<String> okMapped = okR.mapErr(e ->
        {
            fail("Should not be called on Ok");
            return new RuntimeException();
        });
        as(okR, okMapped);

        // Empty: mapper not called, returns same instance
        KResult<String> emptyMapped = emptyR.mapErr(e ->
        {
            fail("Should not be called on empty Ok");
            return new RuntimeException();
        });
        as(emptyR, emptyMapped);

        // Err: mapper transforms the error
        KResult<String> errMapped = errR.mapErr(e -> replacement);
        at(errMapped.err());
        as(replacement, errMapped.getErr());

        // Err: mapper receives the original exception
        AtomicReference<RuntimeException> captured = new AtomicReference<>();
        errR.mapErr(e ->
        {
            captured.set(e);
            return new RuntimeException("new");
        });
        as(original, captured.get());

        // Err: mapper that wraps exception
        KResult<String> wrapped = errR
                .mapErr(e -> new IllegalStateException("wrapped", e));
        at(wrapped.err());
        aeq("wrapped", wrapped.getErr().getMessage());
        as(original, wrapped.getErr().getCause());

        // Err: mapper that returns null throws NullPointerException
        try
        {
            errR.mapErr(e -> null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("mapper must not return null"));
        }

        // Mapped result is a new KResult (not same instance)
        ans(errR, errMapped);

        // Null mapper on ok still throws (null check first)
        try
        {
            okR.mapErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("mapper"));
        }
    }

    /**
     * Verifies {@link KResult#mapOr(java.util.function.Function, Object)}.
     */
    @Test
    public void testMapOr()
    {
        KResult<String> okR = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errR = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Null mapper throws
        try
        {
            okR.mapOr(null, 0);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            ann(e.getMessage());
        }

        // Ok: mapper applied, default ignored
        aeq(Integer.valueOf(5), okR.mapOr(String::length, 99));
        aeq("HELLO", okR.mapOr(String::toUpperCase, "DEFAULT"));

        // Err: mapper NOT called, default returned
        aeq(Integer.valueOf(99), errR.mapOr(s ->
        {
            fail("Should not be called on Err");
            return 0;
        }, 99));
        aeq("DEFAULT", errR.mapOr(s -> "X", "DEFAULT"));

        // Err with null default returns null
        an(errR.mapOr(s -> "X", null));

        // Empty (Ok null): mapper receives null
        aeq("was null", emptyR.mapOr(v -> "was null", "default"));

        // Ok with null default: mapper result returned
        aeq(Integer.valueOf(5), okR.mapOr(String::length, null));

        // Null mapper on err still throws (delegated to mapOrElse which checks)
        try
        {
            errR.mapOr(null, 0);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            ann(e.getMessage());
        }
    }

    /**
     * Verifies
     * {@link KResult#mapOrElse(java.util.function.Function, java.util.function.Function)}.
     */
    @Test
    public void testMapOrElse()
    {
        RuntimeException ex = new RuntimeException("err");
        KResult<String> okR = ok("hello");
        KResult<String> errR = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Both null throws
        try
        {
            okR.mapOrElse(null, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            ann(e.getMessage());
        }

        // Null errMapper throws
        try
        {
            okR.mapOrElse(null, s -> 1);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("errMapper"));
        }

        // Null okMapper throws
        try
        {
            okR.mapOrElse(e -> 1, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("okMapper"));
        }

        // Ok: okMapper is called, errMapper is not
        Integer okResult = okR.mapOrElse(e ->
        {
            fail("errMapper should not be called on Ok");
            return -1;
        }, String::length);
        aeq(Integer.valueOf(5), okResult);

        // Err: errMapper is called, okMapper is not
        Integer errResult = errR.mapOrElse(e ->
        {
            as(ex, e);
            return -1;
        }, s ->
        {
            fail("okMapper should not be called on Err");
            return 0;
        });
        aeq(Integer.valueOf(-1), errResult);

        // Empty (Ok null): okMapper receives null
        String emptyResult = emptyR.mapOrElse(e -> "err", v -> "ok:" + v);
        aeq("ok:null", emptyResult);

        // Mappers can return null
        an(okR.mapOrElse(e -> null, s -> null));
        an(errR.mapOrElse(e -> null, s -> null));

        // Type transformations
        Boolean boolOk = okR.mapOrElse(e -> false, s -> s.length() > 3);
        at(boolOk);

        Boolean boolErr = errR.mapOrElse(e -> false, s -> true);
        af(boolErr);
    }

    /**
     * Verifies {@link KResult#and(KResult)}.
     */
    @Test
    public void testAnd()
    {
        KResult<String> okStr = ok("hello");
        KResult<Integer> okInt = ok(42);
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<Integer> okInt2 = ok(99);
        KResult<String> emptyR = empty();

        // Ok.and(Ok) returns the other
        KResult<Integer> result = okStr.and(okInt);
        at(result.ok());
        aeq(Integer.valueOf(42), result.get());

        // Ok.and(Err) returns Err
        RuntimeException otherEx = new RuntimeException("other");
        KResult<Integer> errInt = err(Integer.class, otherEx);
        KResult<Integer> result2 = okStr.and(errInt);
        at(result2.err());
        as(otherEx, result2.getErr());

        // Err.and(Ok) returns the original Err
        KResult<Integer> result3 = errStr.and(okInt2);
        at(result3.err());
        as(ex, result3.getErr());

        // Err.and(Err) returns the first Err
        RuntimeException ex2 = new RuntimeException("second");
        KResult<Integer> errInt2 = err(Integer.class, ex2);
        KResult<Integer> result4 = errStr.and(errInt2);
        at(result4.err());
        as(ex, result4.getErr());

        // Empty.and(Ok) returns the other
        KResult<Integer> result5 = emptyR.and(okInt);
        at(result5.ok());
        aeq(Integer.valueOf(42), result5.get());

        // Null argument throws
        try
        {
            okStr.and(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("other"));
        }

        // Null argument on err also throws
        try
        {
            errStr.and(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("other"));
        }

        // Type conversion: different types
        KResult<Boolean> okBool = ok(true);
        KResult<Boolean> andBool = okStr.and(okBool);
        at(andBool.ok());
        aeq(Boolean.TRUE, andBool.get());
    }

    /**
     * Verifies {@link KResult#andThen(java.util.function.Function)}.
     */
    @Test
    public void testAndThen()
    {
        KResult<String> okStr = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Ok: function is called with value, returns Ok
        KResult<? extends Integer> result = okStr.andThen(s -> ok(s.length()));
        at(result.ok());
        aeq(Integer.valueOf(5), result.get());

        // Ok: function returns Err
        RuntimeException fnErr = new RuntimeException("fn error");
        KResult<? extends Integer> result2 = okStr
                .andThen(s -> err(Integer.class, fnErr));
        at(result2.err());
        as(fnErr, result2.getErr());

        // Ok: function returns empty
        KResult<? extends Integer> result3 = okStr.andThen(s -> empty());
        at(result3.ok());
        an(result3.get());

        // Err: function not called, error propagated
        KResult<? extends Integer> result4 = errStr.andThen(s ->
        {
            fail("Should not be called on Err");
            return ok(0);
        });
        at(result4.err());
        as(ex, result4.getErr());

        // Empty: function receives null
        KResult<? extends String> result5 = emptyR
                .andThen(v -> ok("from null: " + v));
        at(result5.ok());
        aeq("from null: null", result5.get());

        // Function returns null -> throws NullPointerException
        try
        {
            okStr.andThen(s -> null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("andThen"));
        }

        // Null function throws
        try
        {
            okStr.andThen(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }

        // Null function on err also throws
        try
        {
            errStr.andThen(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }

        // Chaining multiple andThen
        KResult<? extends Integer> chained = okStr.andThen(s -> ok(s.length()))
                .andThen(n -> ok(n * 2));
        at(chained.ok());
        aeq(Integer.valueOf(10), chained.get());

        // Chaining: early err short-circuits
        AtomicBoolean secondCalled = new AtomicBoolean(false);
        KResult<? extends Integer> chainedErr = errStr
                .andThen(s -> ok(s.length())).andThen(n ->
                {
                    secondCalled.set(true);
                    return ok(n * 2);
                });
        at(chainedErr.err());
        af(secondCalled.get());
    }

    /**
     * Verifies {@link KResult#or(KResult)}.
     */
    @Test
    public void testOr()
    {
        KResult<String> okStr = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<String> fallback = ok("fallback");
        KResult<String> emptyR = empty();

        // Ok.or(other) returns this (Ok)
        KResult<? extends String> result = okStr.or(fallback);
        at(result.ok());
        aeq("hello", result.get());
        as(okStr, result);

        // Err.or(Ok) returns the fallback
        KResult<? extends String> result2 = errStr.or(fallback);
        at(result2.ok());
        aeq("fallback", result2.get());
        as(fallback, result2);

        // Err.or(Err) returns the other Err
        RuntimeException ex2 = new RuntimeException("err2");
        KResult<String> errFallback = err(String.class, ex2);
        KResult<? extends String> result3 = errStr.or(errFallback);
        at(result3.err());
        as(ex2, result3.getErr());

        // Empty.or(other) returns this (empty is Ok)
        KResult<? extends String> result4 = emptyR.or(fallback);
        at(result4.ok());
        as(emptyR, result4);

        // Err path rejects a null fallback.
        try
        {
            errStr.or(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            ann(e.getMessage());
        }

        // Ok path returns this even when the fallback reference is null.
        KResult<? extends String> okOrNull = okStr.or(null);
        as(okStr, okOrNull);
    }

    /**
     * Verifies {@link KResult#orElse(java.util.function.Function)}.
     */
    @Test
    public void testOrElse()
    {
        KResult<String> okStr = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Ok: function not called, returns this
        KResult<? extends String> result = okStr.orElse(e ->
        {
            fail("Should not be called on Ok");
            return ok("x");
        });
        as(okStr, result);

        // Empty (Ok): function not called
        KResult<? extends String> result2 = emptyR.orElse(e ->
        {
            fail("Should not be called on empty Ok");
            return ok("x");
        });
        as(emptyR, result2);

        // Err: function called with the error, returns Ok
        AtomicReference<RuntimeException> captured = new AtomicReference<>();
        KResult<String> recovery = ok("recovered");
        KResult<? extends String> result3 = errStr.orElse(e ->
        {
            captured.set(e);
            return recovery;
        });
        as(ex, captured.get());
        as(recovery, result3);
        at(result3.ok());
        aeq("recovered", result3.get());

        // Err: function returns another Err
        RuntimeException newEx = new RuntimeException("new error");
        KResult<String> newErr = err(String.class, newEx);
        KResult<? extends String> result4 = errStr.orElse(e -> newErr);
        at(result4.err());
        as(newEx, result4.getErr());

        // Err: function returns empty
        KResult<? extends String> result5 = errStr.orElse(e -> empty());
        at(result5.ok());
        an(result5.get());

        // Function returns null -> throws NullPointerException
        try
        {
            errStr.orElse(e -> null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("orElse"));
        }

        // Null function throws
        try
        {
            errStr.orElse(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }

        // Null function on ok also throws (null check first)
        try
        {
            okStr.orElse(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("fn"));
        }

        // Chaining: first err recovered
        KResult<? extends String> chained = errStr.orElse(e -> ok("recovered"));
        at(chained.ok());
        aeq("recovered", chained.get());
    }

    /**
     * Verifies {@link KResult#inspect(java.util.function.Consumer)}.
     */
    @Test
    public void testInspect()
    {
        KResult<String> okStr = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Ok: consumer called with value, returns same instance
        AtomicReference<String> captured = new AtomicReference<>();
        KResult<String> returned = okStr.inspect(captured::set);
        as(okStr, returned);
        aeq("hello", captured.get());

        // Ok: consumer called multiple times with same value
        AtomicReference<String> captured2 = new AtomicReference<>();
        okStr.inspect(captured2::set);
        aeq("hello", captured2.get());

        // Err: consumer NOT called, returns same instance
        KResult<String> errReturned = errStr.inspect(v ->
        {
            fail("Should not be called on Err");
        });
        as(errStr, errReturned);

        // Empty: consumer called with null
        AtomicReference<String> capturedEmpty = new AtomicReference<>(
                "sentinel");
        KResult<String> emptyReturned = emptyR.inspect(capturedEmpty::set);
        as(emptyR, emptyReturned);
        an(capturedEmpty.get());

        // Null consumer throws
        try
        {
            okStr.inspect(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("consumer"));
        }

        // Null consumer on err also throws (null check first)
        try
        {
            errStr.inspect(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("consumer"));
        }

        // Chaining inspect does not alter result
        KResult<String> chained = okStr.inspect(v ->
        {
        }).inspect(v ->
        {
        });
        as(okStr, chained);

        // Consumer side effect is observable
        AtomicBoolean called = new AtomicBoolean(false);
        okStr.inspect(v -> called.set(true));
        at(called.get());
    }

    /**
     * Verifies {@link KResult#inspectErr(java.util.function.Consumer)}.
     */
    @Test
    public void testInspectErr()
    {
        KResult<String> okStr = ok("hello");
        RuntimeException ex = new RuntimeException("err");
        KResult<String> errStr = err(String.class, ex);
        KResult<String> emptyR = empty();

        // Err: consumer called with error, returns same instance
        AtomicReference<RuntimeException> captured = new AtomicReference<>();
        KResult<String> returned = errStr.inspectErr(captured::set);
        as(errStr, returned);
        as(ex, captured.get());

        // Err: consumer receives exact exception type
        IllegalArgumentException iae = new IllegalArgumentException("bad");
        KResult<String> errIae = err(String.class, iae);
        AtomicReference<RuntimeException> captured2 = new AtomicReference<>();
        errIae.inspectErr(captured2::set);
        as(iae, captured2.get());
        at(captured2.get() instanceof IllegalArgumentException);

        // Ok: consumer NOT called, returns same instance
        KResult<String> okReturned = okStr.inspectErr(e ->
        {
            fail("Should not be called on Ok");
        });
        as(okStr, okReturned);

        // Empty (Ok): consumer NOT called
        KResult<String> emptyReturned = emptyR.inspectErr(e ->
        {
            fail("Should not be called on empty Ok");
        });
        as(emptyR, emptyReturned);

        // Null consumer throws
        try
        {
            errStr.inspectErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("consumer"));
        }

        // Null consumer on ok also throws (null check first)
        try
        {
            okStr.inspectErr(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            at(e.getMessage().contains("consumer"));
        }

        // Chaining inspectErr does not alter result
        KResult<String> chained = errStr.inspectErr(e ->
        {
        }).inspectErr(e ->
        {
        });
        as(errStr, chained);

        // Consumer side effect is observable
        AtomicBoolean called = new AtomicBoolean(false);
        errStr.inspectErr(e -> called.set(true));
        at(called.get());
    }

    /**
     * assertTrue
     *
     * @param c condition to assert
     */
    private static void at(boolean c)
    {
        assertTrue(c);
    }

    /**
     * assertFalse
     *
     * @param c condition to assert
     */
    private static void af(boolean c)
    {
        assertFalse(c);
    }

    /**
     * assertNull
     *
     * @param o object to assert
     */
    private static void an(Object o)
    {
        assertNull(o);
    }

    /**
     * assertNotNull
     *
     * @param o object to assert
     */
    private static void ann(Object o)
    {
        assertNotNull(o);
    }

    /**
     * assertSame
     *
     * @param expected expected object
     * @param actual   actual object
     */
    private static void as(Object expected, Object actual)
    {
        assertSame(expected, actual);
    }

    /**
     * assertNotSame
     *
     * @param expected expected object
     * @param actual   actual object
     */
    private static void ans(Object expected, Object actual)
    {
        assertNotSame(expected, actual);
    }

    /**
     * assertEquals
     *
     * @param expected expected object
     * @param actual   actual object
     */
    private static void aeq(Object expected, Object actual)
    {
        assertEquals(expected, actual);
    }

    /**
     * assertNotEquals
     *
     * @param expected expected object
     * @param actual   actual object
     */
    private static void ane(Object expected, Object actual)
    {
        assertNotEquals(expected, actual);
    }
}
