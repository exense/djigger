/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *  
 *  This file is part of djigger
 *  
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.ql.Filter;
import io.djigger.ui.model.RealNodePath;



public class Aggregator {

	private final Map<RealNodePath,Aggregation> aggregations;

	public Aggregator() {
		super();
		aggregations = new HashMap<RealNodePath,Aggregation>();
	}

 	public void aggregate(List<RealNodePathWrapper> samples) {
 		aggregations.clear();
 		
 		for(RealNodePathWrapper sample:samples) {			
			RealNodePath nodePath = sample.getPath();
			if(nodePath.getFullPath().size()>0) {
				Aggregation aggregation = aggregations.get(nodePath);
				if(aggregation == null) {
					aggregation = new Aggregation(nodePath);
					aggregations.put(nodePath, aggregation);
				}
				aggregation.addSample(sample);
			}
		}
	}

	public List<Aggregation> query(Filter<RealNodePath> filter) {
		List<Aggregation> result = new ArrayList<Aggregation>();
		for(Aggregation aggregation:aggregations.values()) {
			if(filter==null || filter.isValid(aggregation.getPath())) {
				result.add(aggregation);
			}
		}
		return result;
	}

}
