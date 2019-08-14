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

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class InstrumentationService {

    private final Logger logger = Logger.getLogger(InstrumentationService.class.getName());

    private final Instrumentation instrumentation;
    
    private final InstrumentationErrorListener errorListener;

    private final Set<InstrumentSubscription> subscriptions = new HashSet<InstrumentSubscription>();

    public InstrumentationService(Instrumentation instrumentation, InstrumentationErrorListener errorListener) {
        super();
        this.instrumentation = instrumentation;
        this.errorListener = errorListener;

        ClassFileTransformer transformer = new ClassTransformer(this, errorListener);
        instrumentation.addTransformer(transformer, true);
    }

    public void destroy() {
        Set<InstrumentSubscription> previousSubscriptions = new HashSet<InstrumentSubscription>();
        synchronized (subscriptions) {
            previousSubscriptions.addAll(subscriptions);
            subscriptions.clear();
        }
        for (InstrumentSubscription subscription : previousSubscriptions) {
            applySubscriptionChange(subscription);
        }
    }

    public void addSubscription(InstrumentSubscription subscription) {
        synchronized (subscriptions) {
            subscriptions.add(subscription);
        }
        applySubscriptionChange(subscription);
    }

    public void removeSubscription(InstrumentSubscription subscription) {
        synchronized (subscriptions) {
            subscriptions.remove(subscription);
        }
        applySubscriptionChange(subscription);
    }


    public Set<InstrumentSubscription> getSubscriptions() {
        HashSet<InstrumentSubscription> result = new HashSet<InstrumentSubscription>();
        synchronized (subscriptions) {
            result.addAll(subscriptions);
        }
        return result;
    }

    private void applySubscriptionChange(InstrumentSubscription subscription) {
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            try {
                if (subscription.retransformClass(clazz)) {
                    instrumentation.retransformClasses(clazz);
                }
            } catch (UnmodifiableClassException e) {
            	errorListener.onInstrumentationError(new InstrumentationError(subscription, clazz.getName(), e));
                logger.log(Level.WARNING, "Agent: unable to apply subscription " + subscription.toString() + ". Class '" + clazz.getName() + "' unmodifiable.");
            } catch (Throwable e) {
            	errorListener.onInstrumentationError(new InstrumentationError(subscription, clazz.getName(), e));
                logger.log(Level.WARNING, "Agent: unable to apply subscription " + subscription.toString(), e);
            }
        }
    }
    
    public Class<?> getFirstClassMatching(String name) {
    	Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();
    	for (Class<?> class1 : allLoadedClasses) {
			if(name.equals(class1.getName())) {
				return class1;
			}
		}
    	return null;
    }
}
