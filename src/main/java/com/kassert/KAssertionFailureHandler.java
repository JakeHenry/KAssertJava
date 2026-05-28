package com.kassert;

/**
 * Handles a single assertion failure event.
 */
@FunctionalInterface
public interface KAssertionFailureHandler
{
    /**
     * Executes failure handling for the supplied assertion failure context.
     *
     * @param context failure details
     */
    void onFailure(KAssertionFailureContext context);
}
