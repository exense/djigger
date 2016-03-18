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

package io.djigger.agent;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.Set;


public class InstrumentationService {

	private final Instrumentation instrumentation;
	
	private final Set<InstrumentSubscription> subscriptions = new HashSet<InstrumentSubscription>();
	
	public InstrumentationService(Instrumentation instrumentation) {
		super();
		this.instrumentation = instrumentation;
		
		ClassFileTransformer transformer = new ClassTransformer(this);
		instrumentation.addTransformer(transformer, true);
	}
	
	public synchronized void destroy() {
		Set<InstrumentSubscription> previousSubscriptions = new HashSet<InstrumentSubscription>();
		previousSubscriptions.addAll(subscriptions);
		subscriptions.clear();
		for(InstrumentSubscription subscription:previousSubscriptions) {
			applySubscriptionChange(subscription);
		}
	}
	
	public synchronized void addSubscription(InstrumentSubscription subscription) {
		subscriptions.add(subscription);
		applySubscriptionChange(subscription);
	}
	
	public synchronized void removeSubscription(InstrumentSubscription subscription) {
		subscriptions.remove(subscription);
		applySubscriptionChange(subscription);
	}
	
	
	public synchronized Set<InstrumentSubscription> getSubscriptions() {
		HashSet<InstrumentSubscription> result = new HashSet<InstrumentSubscription>();
		result.addAll(subscriptions);
		return result;
	}

	private void applySubscriptionChange(InstrumentSubscription subscription) {
		try {
			for(Class<?> clazz:instrumentation.getAllLoadedClasses()) {
				if(subscription.isRelatedToClass(clazz.getName())) {
					instrumentation.retransformClasses(clazz);
				}
			}
		} catch (UnmodifiableClassException e) {
			System.err.println("Agent: unable to apply subscription "+subscription.getName()+ ". Class unmodifiable.");
		} catch(Throwable e) {
			System.err.println("Agent: unable to apply subscription "+subscription.getName());
			e.printStackTrace();
		}
	}
}
