package io.djigger.ql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OQLTest {

    @Test
    public void test() {
        Filter<String> filter = OQLFilterBuilder.getFilter("a and b", buildFilterFactory());

        assertFalse(filter.isValid("a"));
        assertFalse(filter.isValid("b"));
        assertTrue(filter.isValid("ab"));

        filter = OQLFilterBuilder.getFilter("a or b", buildFilterFactory());

        assertTrue(filter.isValid("a"));
        assertTrue(filter.isValid("b"));
        assertTrue(filter.isValid("ab"));

        filter = OQLFilterBuilder.getFilter("a or b and c", buildFilterFactory());

        assertTrue(filter.isValid("a"));
        assertFalse(filter.isValid("b"));
        assertFalse(filter.isValid("c"));
        assertTrue(filter.isValid("bc"));

        filter = OQLFilterBuilder.getFilter("not a", buildFilterFactory());

        assertFalse(filter.isValid("a"));
        assertTrue(filter.isValid("b"));

        filter = OQLFilterBuilder.getFilter("a and (b and c)", buildFilterFactory());

        assertFalse(filter.isValid("a"));
        assertFalse(filter.isValid("b"));
        assertFalse(filter.isValid("c"));
        assertFalse(filter.isValid("bc"));
        assertTrue(filter.isValid("abc"));

        filter = OQLFilterBuilder.getFilter("\"a\"", buildFilterFactory());

        assertTrue(filter.isValid("a"));
        assertFalse(filter.isValid("b"));

        filter = OQLFilterBuilder.getFilter("\"or\"", buildFilterFactory());

        assertTrue(filter.isValid("or"));
        assertFalse(filter.isValid("ro"));
    }

    private FilterFactory<String> buildFilterFactory() {
        return new FilterFactory<String>() {

            @Override
            public Filter<String> createFullTextFilter(final String expression) {
                return new Filter<String>() {
                    @Override
                    public boolean isValid(String input) {
                        return input.contains(expression);
                    }
                };
            }

            @Override
            public Filter<String> createAttributeFilter(String operator,
                                                        String attribute, String value) {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }

}
