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
package io.djigger.ql;

import io.djigger.ql.OSQL;
import junit.framework.Assert;

import org.junit.Test;

public class QLTest {
	
	public void test1() throws Exception {
		String expression = "att1 = value";
		Assert.assertEquals("{att1:\"value\"}",OSQL.toMongoQuery(expression));
		
		expression = "att1 = 1";
		Assert.assertEquals("{att1:1}",OSQL.toMongoQuery(expression));
		
		expression = "att1 = \"value\"";
		Assert.assertEquals("{att1:\"value\"}",OSQL.toMongoQuery(expression));
		
		expression = "att1 = \"value with space\"";
		Assert.assertEquals("{att1:\"value with space\"}",OSQL.toMongoQuery(expression));

		expression = "att1 = \"value with space\" and att2=value";
		Assert.assertEquals("{$and:[{att1:\"value with space\"},{att2:\"value\"}]}",OSQL.toMongoQuery(expression));

	}

}
