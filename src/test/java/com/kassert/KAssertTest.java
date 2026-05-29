package com.kassert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.kassert.ex.KResult;

/**
 * Unit tests for the {@link KAssert} facade contract.
 *
 */
public class KAssertTest
{
    /**
     * Verifies that all facade methods delegate and return consistent result
     * states.
     */
    @Test
    public void delegatesAllMethods()
    {
        final KResult<Boolean> refuseResult = KAssert.kRefuse(false, () -> "refuse");
        final KResult<String> equalsResult = KAssert.kRequireEquals("expected", "expected", () -> "equals");
        final KResult<String> sameResult = KAssert.kRequireSame("sameExpected", "sameExpected", () -> "same");
        final KResult<String> notEqualsResult = KAssert.kRefuseEquals("notExpected", "actual", () -> "notEquals");
        final KResult<String> notSameResult = KAssert.kRefuseSame("notSameExpected", "notSameActual", () -> "notSame");
        final KResult<Object> nullResult = KAssert.kRequireNull(null, () -> "null");
        final KResult<String> notNullResult = KAssert.kRefuseNull("notNull", () -> "notNull");
        final KResult<String> instanceOfResult = KAssert.kRequireInstanceOf(String.class, "instanceValue",
                () -> "instanceOf");
        final KResult<String> notInstanceResult = KAssert.kRefuseInstanceOf(Number.class, "notInstanceValue",
                () -> "notInstanceOf");

        assertTrue(refuseResult.passes());
        assertEquals(Boolean.TRUE, refuseResult.val());
        assertTrue(equalsResult.passes());
        assertEquals("expected", equalsResult.val());
        assertTrue(sameResult.passes());
        assertEquals("sameExpected", sameResult.val());
        assertTrue(notEqualsResult.passes());
        assertEquals("actual", notEqualsResult.val());
        assertTrue(notSameResult.passes());
        assertEquals("notSameActual", notSameResult.val());
        assertTrue(nullResult.passes());
        assertNull(nullResult.val());
        assertTrue(notNullResult.passes());
        assertEquals("notNull", notNullResult.val());
        assertTrue(instanceOfResult.passes());
        assertEquals("instanceValue", instanceOfResult.val());
        assertTrue(notInstanceResult.passes());
        assertEquals("notInstanceValue", notInstanceResult.val());
    }

    /**
     * Verifies that throwIfFailed rethrows the original implementation exception.
     */
    @Test
    public void throwIfFailedRethrowsOriginalException()
    {
        try
        {
            KAssert.kRequire(false, () -> "condition must be true").throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("condition must be true", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
    }

    /**
     * Verifies that failure message suppliers are evaluated only on failed
     * assertions.
     */
    @Test
    public void messageSupplierIsLazyOnSuccess()
    {
        final AtomicBoolean supplierCalled = new AtomicBoolean(false);

        final KResult<Boolean> result = KAssert.kRequire(true, () ->
        {
            supplierCalled.set(true);
            return "should not be used";
        });

        assertTrue(result.passes());
        assertFalse(supplierCalled.get());
    }

    /**
     * Verifies that supplementary handlers run when debug mode is enabled.
     *
     * @throws InterruptedException if waiting for handler completion is interrupted
     */
    @Test
    public void supplementaryHandlerExecutes() throws InterruptedException
    {
        final boolean originalEnabled = KAssertConfig.ENABLED;
        try
        {
            KAssertConfig.setEnabledForTesting(true);
            final AtomicReference<Object> invocationFlag = new AtomicReference<>();
            final CountDownLatch handlerStartedLatch = new CountDownLatch(1);
            final KAssertionFailureHandler testHandler = context ->
            {
                assertNotNull(context);
                assertNotNull(context.assertionError());
                assertEquals("condition must be true", context.assertionError().getMessage());
                assertNotNull(context.threadName());
                assertTrue(context.threadId() > 0);
                invocationFlag.set(new Object());
                handlerStartedLatch.countDown();
            };
            KFailureHandlerDispatcher.INSTANCE.registerSupplementaryHandler(testHandler);
            try
            {
                KAssert.kRequire(false, () -> "condition must be true").throwIfFailed();
                fail("Expected runtime exception from throwIfFailed");
            }
            catch (RuntimeException error)
            {
                // expected
            }
            assertTrue(handlerStartedLatch.await(30, TimeUnit.SECONDS));
            assertNotNull(invocationFlag.get());
        }
        finally
        {
            KAssertConfig.setEnabledForTesting(originalEnabled);
            KFailureHandlerDispatcher.INSTANCE.clearSupplementaryHandlers();
        }
    }

    /**
     * Verifies that supplementary handlers do not run when debug mode is disabled.
     *
     * @throws InterruptedException if waiting for handler completion is interrupted
     */
    @Test
    public void supplementaryHandlerNotExecutedWhenDisabled() throws InterruptedException
    {
        final boolean originalEnabled = KAssertConfig.ENABLED;
        try
        {
            KAssertConfig.setEnabledForTesting(false);
            final AtomicReference<Object> invocationFlag = new AtomicReference<>();
            final CountDownLatch handlerStartedLatch = new CountDownLatch(1);
            final KAssertionFailureHandler testHandler = context ->
            {
                invocationFlag.set(new Object());
                handlerStartedLatch.countDown();
            };
            KFailureHandlerDispatcher.INSTANCE.registerSupplementaryHandler(testHandler);
            try
            {
                KAssert.kRequire(false, () -> "condition must be true").throwIfFailed();
                fail("Expected runtime exception from throwIfFailed");
            }
            catch (RuntimeException error)
            {
                // expected
            }
            assertFalse(handlerStartedLatch.await(3, TimeUnit.SECONDS));
            assertNull(invocationFlag.get());
        }
        finally
        {
            KAssertConfig.setEnabledForTesting(originalEnabled);
            KFailureHandlerDispatcher.INSTANCE.clearSupplementaryHandlers();
        }
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequire(boolean, java.util.function.Supplier)}.
     */
    @Test
    public void kRequireTest()
    {
        final KResult<Boolean> result = KAssert.kRequire(true, () -> "condition must be true");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals(Boolean.TRUE, result.val());
        assertTrue(result.val());

        final KResult<Boolean> failedResult = KAssert.kRequire(false, () -> "condition must be true");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("condition must be true", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertEquals(Boolean.FALSE, failedResult.val());
        assertFalse(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuse(boolean, java.util.function.Supplier)}.
     */
    @Test
    public void kRefuseTest()
    {
        final KResult<Boolean> result = KAssert.kRefuse(false, () -> "condition must be false");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals(Boolean.TRUE, result.val());
        assertTrue(result.val());

        final KResult<Boolean> failedResult = KAssert.kRefuse(true, () -> "condition must be false");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("condition must be false", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertEquals(Boolean.FALSE, failedResult.val());
        assertFalse(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireEquals(Object, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRequireEqualsTest()
    {
        final KResult<String> result = KAssert.kRequireEquals("expected", "expected", () -> "values must be equal");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals("expected", result.val());

        final KResult<String> failedResult = KAssert.kRequireEquals("expected", "actual", () -> "values must be equal");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("values must be equal", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertEquals("actual", failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuseEquals(Object, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRefuseEqualsTest()
    {
        final KResult<String> result = KAssert.kRefuseEquals("expected", "actual", () -> "values must differ");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals("actual", result.val());

        final KResult<String> failedResult = KAssert.kRefuseEquals("same", "same", () -> "values must differ");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("values must differ", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertEquals("same", failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireSame(Object, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRequireSameTest()
    {
        final Object shared = new Object();
        final KResult<Object> result = KAssert.kRequireSame(shared, shared, () -> "references must match");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertTrue(shared == result.val());

        final Object expected = new Object();
        final Object actual = new Object();
        final KResult<Object> failedResult = KAssert.kRequireSame(expected, actual, () -> "references must match");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("references must match", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertTrue(actual == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuseSame(Object, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRefuseSameTest()
    {
        final Object left = new Object();
        final Object right = new Object();
        final KResult<Object> result = KAssert.kRefuseSame(left, right, () -> "references must differ");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertTrue(right == result.val());

        final Object shared = new Object();
        final KResult<Object> failedResult = KAssert.kRefuseSame(shared, shared, () -> "references must differ");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("references must differ", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertTrue(shared == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireNull(Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRequireNullTest()
    {
        final KResult<Object> result = KAssert.kRequireNull(null, () -> "value must be null");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertNull(result.val());

        final Object value = new Object();
        final KResult<Object> failedResult = KAssert.kRequireNull(value, () -> "value must be null");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("value must be null", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertTrue(value == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuseNull(Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRefuseNullTest()
    {
        final String value = "present";
        final KResult<String> result = KAssert.kRefuseNull(value, () -> "value must not be null");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals("present", result.val());

        final KResult<Object> failedResult = KAssert.kRefuseNull(null, () -> "value must not be null");
        try
        {
            failedResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("value must not be null", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedResult.passes());
        assertTrue(failedResult.fails());
        assertNull(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireInstanceOf(Class, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRequireInstanceOfTest()
    {
        final Integer number = Integer.valueOf(42);
        final KResult<Integer> result = KAssert.kRequireInstanceOf(Number.class, number, () -> "value must be Number");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals(Integer.valueOf(42), result.val());

        final KResult<String> failedTypeResult = KAssert.kRequireInstanceOf(Number.class, "text",
                () -> "value must be Number");
        try
        {
            failedTypeResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("value must be Number", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedTypeResult.passes());
        assertTrue(failedTypeResult.fails());
        assertEquals("text", failedTypeResult.val());

        final KResult<String> failedNullTypeResult = KAssert.kRequireInstanceOf(null, "text",
                () -> "value must be Number");
        try
        {
            failedNullTypeResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("expectedType must not be null", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedNullTypeResult.passes());
        assertTrue(failedNullTypeResult.fails());
        assertEquals("text", failedNullTypeResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuseInstanceOf(Class, Object, java.util.function.Supplier)}.
     */
    @Test
    public void kRefuseInstanceOfTest()
    {
        final String value = "text";
        final KResult<String> result = KAssert.kRefuseInstanceOf(Number.class, value, () -> "value must not be Number");
        assertTrue(result.throwIfFailed().passes());
        assertTrue(result.passes());
        assertFalse(result.fails());
        assertEquals("text", result.val());

        final KResult<String> failedTypeResult = KAssert.kRefuseInstanceOf(String.class, value,
                () -> "value must not be String");
        try
        {
            failedTypeResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("value must not be String", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedTypeResult.passes());
        assertTrue(failedTypeResult.fails());
        assertEquals("text", failedTypeResult.val());

        final KResult<String> failedNullTypeResult = KAssert.kRefuseInstanceOf(null, value,
                () -> "value must not be String");
        try
        {
            failedNullTypeResult.throwIfFailed();
            fail("Expected runtime exception from throwIfFailed");
        }
        catch (RuntimeException error)
        {
            assertEquals("refusedType must not be null", error.getMessage());
            assertNotNull(error.getStackTrace());
            assertTrue(error.getStackTrace().length > 0);
            assertFalse("com.kassert.ex.KResult".equals(error.getStackTrace()[0].getClassName()));
        }
        assertFalse(failedNullTypeResult.passes());
        assertTrue(failedNullTypeResult.fails());
        assertEquals("text", failedNullTypeResult.val());
    }
}