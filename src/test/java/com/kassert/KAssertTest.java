package com.kassert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
        final KResult<Boolean> refuseResult = KAssert.kRefuse(false, "refuse");
        final KResult<String> equalsResult = KAssert.kRequireEquals("expected", "expected", "equals");
        final KResult<String> sameResult = KAssert.kRequireSame("sameExpected", "sameExpected", "same");
        final KResult<String> notEqualsResult = KAssert.kRefuseEquals("notExpected", "actual", "notEquals");
        final KResult<String> notSameResult = KAssert.kRefuseSame("notSameExpected", "notSameActual", "notSame");
        final KResult<Object> nullResult = KAssert.kRequireNull(null, "null");
        final KResult<String> notNullResult = KAssert.kRefuseNull("notNull", "notNull");
        final KResult<String> instanceOfResult = KAssert.kRequireInstanceOf(String.class, "instanceValue",
                "instanceOf");
        final KResult<String> notInstanceResult = KAssert.kRefuseInstanceOf(Number.class, "notInstanceValue",
                "notInstanceOf");

        assertTrue(refuseResult.ok());
        assertEquals(Boolean.TRUE, refuseResult.val());
        assertTrue(equalsResult.ok());
        assertEquals("expected", equalsResult.val());
        assertTrue(sameResult.ok());
        assertEquals("sameExpected", sameResult.val());
        assertTrue(notEqualsResult.ok());
        assertEquals("actual", notEqualsResult.val());
        assertTrue(notSameResult.ok());
        assertEquals("notSameActual", notSameResult.val());
        assertTrue(nullResult.ok());
        assertNull(nullResult.val());
        assertTrue(notNullResult.ok());
        assertEquals("notNull", notNullResult.val());
        assertTrue(instanceOfResult.ok());
        assertEquals("instanceValue", instanceOfResult.val());
        assertTrue(notInstanceResult.ok());
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
            KAssert.kRequire(false, "condition must be true").throwIfFailed();
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
                KAssert.kRequire(false, "condition must be true").throwIfFailed();
                fail("Expected runtime exception from throwIfFailed");
            }
            catch (RuntimeException error)
            {
                // expected
            }
            assertNotNull(invocationFlag.get());
            assertTrue(handlerStartedLatch.await(30, TimeUnit.SECONDS));
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
                KAssert.kRequire(false, "condition must be true").throwIfFailed();
                fail("Expected runtime exception from throwIfFailed");
            }
            catch (RuntimeException error)
            {
                // expected
            }
            assertNull(invocationFlag.get());
            assertFalse(handlerStartedLatch.await(3, TimeUnit.SECONDS));
        }
        finally
        {
            KAssertConfig.setEnabledForTesting(originalEnabled);
            KFailureHandlerDispatcher.INSTANCE.clearSupplementaryHandlers();
        }
    }

    /**
     * Verifies success and failure behavior of {@link KAssert#kRequire(boolean, String)}.
     */
    @Test
    public void kRequireTest()
    {
        final KResult<Boolean> result = KAssert.kRequire(true, "condition must be true");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals(Boolean.TRUE, result.val());
        assertTrue(result.val());

        final KResult<Boolean> failedResult = KAssert.kRequire(false, "condition must be true");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertEquals(Boolean.FALSE, failedResult.val());
        assertFalse(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of {@link KAssert#kRefuse(boolean, String)}.
     */
    @Test
    public void kRefuseTest()
    {
        final KResult<Boolean> result = KAssert.kRefuse(false, "condition must be false");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals(Boolean.TRUE, result.val());
        assertTrue(result.val());

        final KResult<Boolean> failedResult = KAssert.kRefuse(true, "condition must be false");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertEquals(Boolean.FALSE, failedResult.val());
        assertFalse(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireEquals(Object, Object, String)}.
     */
    @Test
    public void kRequireEqualsTest()
    {
        final KResult<String> result = KAssert.kRequireEquals("expected", "expected", "values must be equal");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals("expected", result.val());

        final KResult<String> failedResult = KAssert.kRequireEquals("expected", "actual", "values must be equal");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertEquals("actual", failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRefuseEquals(Object, Object, String)}.
     */
    @Test
    public void kRefuseEqualsTest()
    {
        final KResult<String> result = KAssert.kRefuseEquals("expected", "actual", "values must differ");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals("actual", result.val());

        final KResult<String> failedResult = KAssert.kRefuseEquals("same", "same", "values must differ");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertEquals("same", failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireSame(Object, Object, String)}.
     */
    @Test
    public void kRequireSameTest()
    {
        final Object shared = new Object();
        final KResult<Object> result = KAssert.kRequireSame(shared, shared, "references must match");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertTrue(shared == result.val());

        final Object expected = new Object();
        final Object actual = new Object();
        final KResult<Object> failedResult = KAssert.kRequireSame(expected, actual, "references must match");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertTrue(actual == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
    * {@link KAssert#kRefuseSame(Object, Object, String)}.
     */
    @Test
    public void kRefuseSameTest()
    {
        final Object left = new Object();
        final Object right = new Object();
        final KResult<Object> result = KAssert.kRefuseSame(left, right, "references must differ");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertTrue(right == result.val());

        final Object shared = new Object();
        final KResult<Object> failedResult = KAssert.kRefuseSame(shared, shared, "references must differ");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertTrue(shared == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireNull(Object, String)}.
     */
    @Test
    public void kRequireNullTest()
    {
        final KResult<Object> result = KAssert.kRequireNull(null, "value must be null");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertNull(result.val());

        final Object value = new Object();
        final KResult<Object> failedResult = KAssert.kRequireNull(value, "value must be null");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertTrue(value == failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
    * {@link KAssert#kRefuseNull(Object, String)}.
     */
    @Test
    public void kRefuseNullTest()
    {
        final String value = "present";
        final KResult<String> result = KAssert.kRefuseNull(value, "value must not be null");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals("present", result.val());

        final KResult<Object> failedResult = KAssert.kRefuseNull(null, "value must not be null");
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
        assertFalse(failedResult.ok());
        assertTrue(failedResult.failed());
        assertNull(failedResult.val());
    }

    /**
     * Verifies success and failure behavior of
     * {@link KAssert#kRequireInstanceOf(Class, Object, String)}.
     */
    @Test
    public void kRequireInstanceOfTest()
    {
        final Integer number = Integer.valueOf(42);
        final KResult<Integer> result = KAssert.kRequireInstanceOf(Number.class, number, "value must be Number");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals(Integer.valueOf(42), result.val());

        final KResult<String> failedTypeResult = KAssert.kRequireInstanceOf(Number.class, "text",
                "value must be Number");
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
        assertFalse(failedTypeResult.ok());
        assertTrue(failedTypeResult.failed());
        assertEquals("text", failedTypeResult.val());

        final KResult<String> failedNullTypeResult = KAssert.kRequireInstanceOf(null, "text",
                "value must be Number");
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
        assertFalse(failedNullTypeResult.ok());
        assertTrue(failedNullTypeResult.failed());
        assertEquals("text", failedNullTypeResult.val());
    }

    /**
     * Verifies success and failure behavior of
    * {@link KAssert#kRefuseInstanceOf(Class, Object, String)}.
     */
    @Test
    public void kRefuseInstanceOfTest()
    {
        final String value = "text";
        final KResult<String> result = KAssert.kRefuseInstanceOf(Number.class, value,
                "value must not be Number");
        assertTrue(result.throwIfFailed().ok());
        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals("text", result.val());

        final KResult<String> failedTypeResult = KAssert.kRefuseInstanceOf(String.class, value,
                "value must not be String");
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
        assertFalse(failedTypeResult.ok());
        assertTrue(failedTypeResult.failed());
        assertEquals("text", failedTypeResult.val());

        final KResult<String> failedNullTypeResult = KAssert.kRefuseInstanceOf(null, value,
                "value must not be String");
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
        assertFalse(failedNullTypeResult.ok());
        assertTrue(failedNullTypeResult.failed());
        assertEquals("text", failedNullTypeResult.val());
    }
}