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
package io.djigger.monitoring.java.instrumentation.subscription;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class TimeMeasureTransformer {

	public static void transform(CtClass clazz, CtMethod method, InstrumentSubscription subscription, boolean captureThreadInfos) throws CannotCompileException {
		method.insertBefore("io.djigger.agent.InstrumentationEventCollector.enterMethod(\"" + clazz.getName() + "\",\"" + method.getName() + "\","+Boolean.toString(captureThreadInfos)+","+subscription.getId()+");");
		method.insertAfter("io.djigger.agent.InstrumentationEventCollector.leaveMethod();", false);
	}
}
