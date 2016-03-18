/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/

package io.djigger.aggregation.filter;

import io.djigger.model.NodeID;
import io.djigger.model.RealNodePath;
import io.djigger.ui.common.NodePresentationHelper;

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
		for(NodeID node:branch.getFullPath()) {
			if(presentationHelper.getFullname(node).contains(pattern)) {
				return true;
			}
		}
		return false;
	}
}
