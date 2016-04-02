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

import io.djigger.model.RealNodePath;
import io.djigger.ui.common.NodePresentationHelper;

public class BranchFilterFactory implements AtomicFilterFactory<RealNodePath> {

	private final NodePresentationHelper presentationHelper;

	public BranchFilterFactory(NodePresentationHelper presentationHelper) {
		super();
		this.presentationHelper = presentationHelper;
	}

	@Override
	public Filter<RealNodePath> createFilter(String expression) {
		return new BranchFilter(expression, presentationHelper);
	}
}