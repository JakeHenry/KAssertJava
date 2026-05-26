package com.kassert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link KAssert} facade contract.
 */
public class KAssertTest
{
    /**
     * Verifies that {@link KAssert#kRequire(boolean, String)} accepts true conditions.
     */
    @Test
    public void kRequireAllowsTrueCondition()
    {
        KAssert.kRequire(true, "condition must be true");
    }

    /**
     * Verifies that {@link KAssert#kRequire(boolean, String)} throws when the condition is false.
     */
    @Test(expected = IllegalStateException.class)
    public void kRequireRejectsFalseCondition() throws IllegalStateException
    {
        KAssert.kRequire(false, "condition must be true");
    }

    /**
     * Verifies that {@link KAssert#kRequireNotNull(Object, String)} returns the same non-null value.
     */
    @Test
    public void kRequireNotNullReturnsInputValue()
    {
        final String value = "ok";
        assertSame(value, KAssert.kRequireNotNull(value, "value must not be null"));
    }

    /**
     * Verifies that {@link KAssert#kRequireNotNull(Object, String)} throws for null values.
     */
    @Test(expected = IllegalStateException.class)
    public void kRequireNotNullRejectsNullValue() throws IllegalStateException
    {
        KAssert.kRequireNotNull(null, "value must not be null");
    }

    /**
     * Verifies that {@link KAssert#kRequireEquals(Object, Object, String)} returns the expected value.
     */
    @Test
    public void kRequireEqualsReturnsExpectedValue()
    {
        assertEquals("ok", KAssert.kRequireEquals("ok", "ok", "must be equal"));
    }

    /**
     * Verifies that all facade methods delegate and return consistent success values.
     */
    @Test
    public void delegatesAllMethods()
    {
        final String notExpected = "notExpected";
        final String actual = "actual";
        final String notEqualsResult = KAssert.kRequireNotEquals(notExpected, actual, "notEquals");
        final String notSameExpected = "notSameExpected";
        final String notSameActual = "notSameActual";
        final String notSameResult = KAssert.kRequireNotSame(notSameExpected, notSameActual, "notSame");

        assertTrue(KAssert.kRefuse(false, "refuse"));
        assertEquals("expected", KAssert.kRequireEquals("expected", "expected", "equals"));
        assertEquals("sameExpected", KAssert.kRequireSame("sameExpected", "sameExpected", "same"));
        assertTrue(notEqualsResult.equals(notExpected) || notEqualsResult.equals(actual));
        assertTrue(notSameResult.equals(notSameExpected) || notSameResult.equals(notSameActual));
        assertNull(KAssert.kRequireNull(null, "null"));
        assertEquals("notNull", KAssert.kRequireNotNull("notNull", "notNull"));
        assertEquals("instanceValue", KAssert.kRequireInstanceOf(String.class, "instanceValue", "instanceOf"));
        assertEquals("notInstanceValue", KAssert.kRequireNotInstanceOf(Number.class, "notInstanceValue", "notInstanceOf"));

        assertTrue(KAssert.kAssertTrue(true, "assertTrue"));
        assertTrue(KAssert.kAssertFalse(false, "assertFalse"));
        assertTrue(KAssert.kAssertEquals("e1", "e1", "assertEquals"));
        assertTrue(KAssert.kAssertNotEquals("e2", "a2", "assertNotEquals"));
        assertTrue(KAssert.kAssertSame("e3", "e3", "assertSame"));
        assertTrue(KAssert.kAssertNotSame("e4", "a4", "assertNotSame"));
        assertTrue(KAssert.kAssertNull(null, "assertNull"));
        assertTrue(KAssert.kAssertNotNull("value", "assertNotNull"));
        assertTrue(KAssert.kAssertInstanceOf(String.class, "value", "assertInstanceOf"));
        assertTrue(KAssert.kAssertNotInstanceOf(Number.class, "value", "assertNotInstanceOf"));
    }
}
