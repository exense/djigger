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
package io.djigger.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;


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
		for(Class<?> clazz:instrumentation.getAllLoadedClasses()) {
			try {
				if(subscription.isRelatedToClass(clazz.getName())&&subscription.isRelatedToClass(clazz)) {
					instrumentation.retransformClasses(clazz);
				}
			} catch (UnmodifiableClassException e) {
				System.err.println("Agent: unable to apply subscription "+subscription.getName()+ ". Class '"+clazz.getName()+"' unmodifiable.");
			} catch(Throwable e) {
				System.err.println("Agent: unable to apply subscription "+subscription.getName());
				e.printStackTrace();
			}
		}
	}
}
