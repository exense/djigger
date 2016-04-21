package io.djigger.ql;

import org.junit.Assert;
import org.junit.Test;

public class OQLTest {

	@Test
	public void test() {
		Filter<String> filter = OQLFilterBuilder.getFilter("a and b", buildFilterFactory());
		
		Assert.assertFalse(filter.isValid("a"));
		Assert.assertFalse(filter.isValid("b"));
		Assert.assertTrue(filter.isValid("ab"));
		
		filter = OQLFilterBuilder.getFilter("a or b", buildFilterFactory());
		
		Assert.assertTrue(filter.isValid("a"));
		Assert.assertTrue(filter.isValid("b"));
		Assert.assertTrue(filter.isValid("ab"));
		
		filter = OQLFilterBuilder.getFilter("a or b and c", buildFilterFactory());
		
		Assert.assertTrue(filter.isValid("a"));
		Assert.assertFalse(filter.isValid("b"));
		Assert.assertFalse(filter.isValid("c"));
		Assert.assertTrue(filter.isValid("bc"));
		
		filter = OQLFilterBuilder.getFilter("not a", buildFilterFactory());
		
		Assert.assertFalse(filter.isValid("a"));
		Assert.assertTrue(filter.isValid("b"));
		
		filter = OQLFilterBuilder.getFilter("a and (b and c)", buildFilterFactory());
		
		Assert.assertFalse(filter.isValid("a"));
		Assert.assertFalse(filter.isValid("b"));
		Assert.assertFalse(filter.isValid("c"));
		Assert.assertFalse(filter.isValid("bc"));
		Assert.assertTrue(filter.isValid("abc"));
		
		filter = OQLFilterBuilder.getFilter("\"a\"", buildFilterFactory());
		
		Assert.assertTrue(filter.isValid("a"));
		Assert.assertFalse(filter.isValid("b"));
		
		filter = OQLFilterBuilder.getFilter("\"or\"", buildFilterFactory());
		
		Assert.assertTrue(filter.isValid("or"));
		Assert.assertFalse(filter.isValid("ro"));
	}

	private FilterFactory<String> buildFilterFactory() {
		return new FilterFactory<String>() {

			@Override
			public Filter<String> createFullTextFilter(final String expression) {
				return new Filter<String>() {
					@Override
					public boolean isValid(String input) {
						return input.contains(expression);
					}};
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
