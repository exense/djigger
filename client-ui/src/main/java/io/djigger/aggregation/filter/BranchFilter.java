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
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNodePath;

public class BranchFilter implements Filter<RealNodePath> {

    private final String pattern;

    private final NodePresentationHelper presentationHelper;

    public BranchFilter(String pattern,
                        NodePresentationHelper presentationHelper) {
        super();
        this.pattern = pattern;
        this.presentationHelper = presentationHelper;
    }

    @Override
    public boolean isValid(RealNodePath branch) {
        for (NodeID node : branch.getFullPath()) {
            if (presentationHelper.getFullname(node).contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
