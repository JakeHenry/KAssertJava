package com.kassert;

import com.kassert.ex.KResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

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
        final KResult<String> notEqualsResult = KAssert.kRequireNotEquals("notExpected", "actual", "notEquals");
        final KResult<String> notSameResult = KAssert.kRequireNotSame("notSameExpected", "notSameActual", "notSame");
        final KResult<Object> nullResult = KAssert.kRequireNull(null, "null");
        final KResult<String> notNullResult = KAssert.kRequireNotNull("notNull", "notNull");
        final KResult<String> instanceOfResult = KAssert.kRequireInstanceOf(String.class, "instanceValue",
                "instanceOf");
        final KResult<String> notInstanceResult = KAssert.kRequireNotInstanceOf(Number.class, "notInstanceValue",
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
}