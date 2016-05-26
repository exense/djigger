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
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class ClassTransformer implements ClassFileTransformer {
	
	private static final Logger logger = LoggerFactory.getLogger(ClassTransformer.class);
	
	private final InstrumentationService service;

	ClassTransformer(InstrumentationService service) {
		super();
		this.service = service;
	}
		
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
		byte[] classfileBuffer) throws IllegalClassFormatException {

		ClassPool pool = ClassPool.getDefault();
		if(loader!=null) {
			pool.insertClassPath(new LoaderClassPath(loader));
		}
		CtClass currentClass = null;
		try {
			currentClass = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
		
			Set<InstrumentSubscription> subscriptions = service.getSubscriptions();
			if(subscriptions!=null && subscriptions.size()>0) {
				boolean transformed = false;
				for(InstrumentSubscription subscription:subscriptions) {
					if (subscription.isRelatedToClass(currentClass)) {
						for (CtMethod method : currentClass.getDeclaredMethods()) {
							if (subscription.isRelatedToMethod(method)) {
								if(subscription instanceof TransformingSubscription) {
									if(logger.isDebugEnabled()) {
										logger.debug("Transforming method " + className + "." + method.getLongName());
									}
									((TransformingSubscription)subscription).transform(currentClass, method);
									transformed = true;
								}
							}
						}
					}
				}

				if(transformed) {
					if(logger.isDebugEnabled()) {
						logger.debug("Transforming " + className);
					}
					classfileBuffer = currentClass.toBytecode();	
				}
			}
		} catch (Throwable e) {
			logger.error("An error occurred while transforming class "+className, e);
		} finally {
			if(currentClass!=null) {
				currentClass.detach();
			}	
		}

		return classfileBuffer;
	}
}
