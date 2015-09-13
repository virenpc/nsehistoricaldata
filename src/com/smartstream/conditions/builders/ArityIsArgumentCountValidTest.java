package com.smartstream.conditions.builders;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.viren.conditions.Operator.Arity;

public class ArityIsArgumentCountValidTest {

    private static final List<Object> emptyTestList = Collections.emptyList();
    private static final List<Object> oneEntryTestList = Arrays.asList(new Object());
    private static final List<Object> twoEntriesTestList = Arrays.asList(new Object(), new Object());
    private static final List<Object> multipleEntriesTestList = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object());

    // rare case but who knows
    @SuppressWarnings("serial")
    private static final ArrayList<Object> negativeEntryTestList = new ArrayList<Object>(){

        @Override
        public Object get(int index) {
            return null;
        }

        @Override
        public int size() {
            return -1;
        }};

    @Test
    public void testArityUnaryParameter(){
        assertTrue("<null> should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(null));
        assertTrue("An empty list should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(emptyTestList));

        assertFalse("Only empty or null lists should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(oneEntryTestList));
        assertFalse("Only empty or null lists should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(twoEntriesTestList));
        assertFalse("Only empty or null lists should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(multipleEntriesTestList));
        assertFalse("Only empty or null lists should be allowed on arity of type UNARY!", Arity.UNARY.isArgumentsCountValid(negativeEntryTestList));
    }

    @Test
    public void testArityBinaryParameter(){
        assertFalse("<null> shouldn't be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(null));
        assertFalse("An empty list shouldn't be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(emptyTestList));

        assertTrue("Only a list with 1 entry should be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(oneEntryTestList));
        assertFalse("Only a list with 1 entry should be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(twoEntriesTestList));
        assertFalse("Only a list with 1 entry should be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(multipleEntriesTestList));
        assertFalse("Only a list with 1 entry should be allowed on arity of type BINARY!", Arity.BINARY.isArgumentsCountValid(negativeEntryTestList));
    }

    @Test
    public void testArityPolyadicParameter(){
        assertFalse("<null> shouldn't be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(null));
        assertFalse("An empty list shouldn't be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(emptyTestList));

        assertTrue("Only a list with at least 1 entry should be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(oneEntryTestList));
        assertTrue("Only a list with at least 1 entry should be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(twoEntriesTestList));
        assertTrue("Only a list with at least 1 entry should be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(multipleEntriesTestList));
        assertFalse("Only a list with at least 1 entry should be allowed on arity of type POLYADIC!", Arity.POLYADIC.isArgumentsCountValid(negativeEntryTestList));
    }

    @Test
    public void testArityTernaryParameter(){
        assertFalse("<null> shouldn't be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(null));
        assertFalse("An empty list shouldn't be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(emptyTestList));

        assertFalse("Only a list with 2 entry should be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(oneEntryTestList));
        assertTrue("Only a list with 2 entry should be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(twoEntriesTestList));
        assertFalse("Only a list with 2 entry should be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(multipleEntriesTestList));
        assertFalse("Only a list with 2 entry should be allowed on arity of type TERNARY!", Arity.TERNARY.isArgumentsCountValid(negativeEntryTestList));
    }

}
