package io.djigger.ql;

import java.util.ArrayList;
import java.util.List;

public class Filters {

	public static <T> List<T> apply(Filter<T> filter, List<T> list) {
		if(filter!=null) {
			List<T> result = new ArrayList<>();
			for(T entry:list) {
				if(filter.isValid(entry)) {
					result.add(entry);
				}
			}			
			return result;
		} else {
			return new ArrayList<>(list);
		}
	}
}
