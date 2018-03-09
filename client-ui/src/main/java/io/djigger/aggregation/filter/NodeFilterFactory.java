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
package io.djigger.aggregation.filter;

import io.djigger.ql.Filter;
import io.djigger.ql.FilterFactory;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.NodeID;

public class NodeFilterFactory implements FilterFactory<NodeID> {

    private final NodePresentationHelper presentationHelper;

    public NodeFilterFactory(NodePresentationHelper presentationHelper) {
        super();
        this.presentationHelper = presentationHelper;
    }

    @Override
    public Filter<NodeID> createFullTextFilter(String expression) {
        return new NodeFilter(expression, presentationHelper);
    }

    @Override
    public Filter<NodeID> createAttributeFilter(String operator,
                                                String attribute, String value) {
        // TODO Auto-generated method stub
        return null;
    }

}
