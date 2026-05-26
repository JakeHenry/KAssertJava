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
 */
public class KAssertTest
{
    /**
     * Verifies that {@link KAssert#kRequire(boolean, String)} succeeds for true
     * conditions.
     */
    @Test
    public void kRequireAllowsTrueCondition()
    {
        final KResult<Boolean> result = KAssert.kRequire(true, "condition must be true");

        assertTrue(result.ok());
        assertFalse(result.failed());
        assertEquals(Boolean.TRUE, result.val());
    }

    /**
     * Verifies that {@link KAssert#kRequire(boolean, String)} fails when the
     * condition is false.
     */
    @Test
    public void kRequireRejectsFalseCondition()
    {
        final KResult<Boolean> result = KAssert.kRequire(false, "condition must be true");

        assertFalse(result.ok());
        assertTrue(result.failed());
        assertEquals(Boolean.FALSE, result.val());
    }

    /**
     * Verifies that {@link KAssert#kRequireNotNull(Object, String)} succeeds for
     * non-null values.
     */
    @Test
    public void kRequireNotNullReturnsInputValue()
    {
        final String value = "ok";
        final KResult<String> result = KAssert.kRequireNotNull(value, "value must not be null");

        assertTrue(result.ok());
        assertSame(value, result.val());
    }

    /**
     * Verifies that {@link KAssert#kRequireNotNull(Object, String)} fails for null
     * values.
     */
    @Test
    public void kRequireNotNullRejectsNullValue()
    {
        final KResult<Object> result = KAssert.kRequireNotNull(null, "value must not be null");

        assertTrue(result.failed());
        assertNull(result.val());
    }

    /**
     * Verifies that {@link KAssert#kRequireEquals(Object, Object, String)} succeeds
     * for equal values.
     */
    @Test
    public void kRequireEqualsReturnsExpectedValue()
    {
        final KResult<String> result = KAssert.kRequireEquals("ok", "ok", "must be equal");

        assertTrue(result.ok());
        assertEquals("ok", result.val());
    }

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
        assertEquals("notExpected", notEqualsResult.val());
        assertTrue(notSameResult.ok());
        assertEquals("notSameExpected", notSameResult.val());
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
     * Verifies that throwIfFailed is chainable and keeps success results unchanged.
     */
    @Test
    public void throwIfFailedIsChainableForSuccess()
    {
        assertTrue(KAssert.kRequire(true, "ok").throwIfFailed().ok());
    }

    /**
     * Verifies that throwIfFailed rethrows the original implementation exception.
     */
    @Test
    public void throwIfFailedRethrowsOriginalImplementationException()
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
    public void resultBuilderPatternTest()
    {
        System.setProperty("existing.value", "true");
        String value = KAssert.kRequireNotNull(System.getProperty("existing.value"), "existing.value must not be null")
                .throwIfFailed().val();
        assertEquals("true", value);

        try
        {
            String value2 = KAssert
                    .kRequireNotNull(System.getProperty("missing.value"), "missing.value must not be null")
                    .throwIfFailed().val();
            assertNull(value2);
            fail("Expected exception for missing value");
        }
        catch (RuntimeException error)
        {
            assertEquals("missing.value must not be null", error.getMessage());
        }

        String value3 = KAssert.kRequireNotNull(System.getProperty("existing.value"), "existing.value must not be null")
                .val();
        assertEquals("true", value3);

        String value4 = KAssert.kRequireNotNull(System.getProperty("missing.value"), "missing.value must not be null")
                .val();
        assertNull(value4);

        KResult<String> result = KAssert.kRequireNotNull(System.getProperty("existing.value"), "existing.value must not be null").throwIfFailed();
        assertTrue(result.ok());
        assertEquals("true", result.val());
    }
}
