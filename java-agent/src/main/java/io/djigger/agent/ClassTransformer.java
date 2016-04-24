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
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

public class ClassTransformer implements ClassFileTransformer {

	private static final Logger logger = LoggerFactory.getLogger(ClassTransformer.class);
	
	private final InstrumentationService service;

	ClassTransformer(InstrumentationService service) {
		super();
		this.service = service;
	}

	private static final String START_TIMENANO_VAR = "$djigger$startnano";
		
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if(classBeingRedefined!=null) {
			Set<InstrumentSubscription> subscriptions = service.getSubscriptions();
			if(subscriptions!=null && subscriptions.size()>0) {
				if(logger.isDebugEnabled()) {
					logger.debug("Redefining " + classBeingRedefined.getName());
				}
				ClassPool pool = ClassPool.getDefault();
				if(loader!=null) {
					if(logger.isDebugEnabled()) {
						logger.debug("Appending classpath: " + loader);
					}
					pool.appendClassPath(new LoaderClassPath(loader));
				}
				try {
					CtClass ctClass = pool
							.makeClass(new java.io.ByteArrayInputStream(
									classfileBuffer));
					CtClass currentClass = ctClass;
					do {
						for (CtMethod method : currentClass.getDeclaredMethods()) {							
							boolean matches = false;
							boolean captureThreadInfo = false;
							
							for(InstrumentSubscription subscription:subscriptions) {
								if (subscription.isRelatedToClass(currentClass.getName()) && subscription.isRelatedToMethod(method.getName())) {
									matches = true;
									
									if(subscription.captureThreadInfo()) {
										captureThreadInfo = true;
										break;
									}
								}
							}
							if(matches && !Modifier.isNative(method.getModifiers())) {								
								method.addLocalVariable(START_TIMENANO_VAR, CtClass.longType);
								method.insertBefore(START_TIMENANO_VAR+" = System.nanoTime();");
								
//								if(!Modifier.isStatic(method.getModifiers())) {
//									method.insertBefore("io.djigger.agent.Collector.start(this, \"" + currentClass.getName() + "\",\"" + method.getName() + "\");");
//								} else {
//									method.insertBefore("io.djigger.agent.Collector.start(null, \"" + currentClass.getName() + "\",\"" + method.getName() + "\");");
//								}

								String methodName = captureThreadInfo?"reportWithThreadInfo":"report";
								String callStr = "io.djigger.agent.InstrumentationEventCollector."+methodName+"(\"" + currentClass.getName() + "\",\"" + method.getName() + "\","+START_TIMENANO_VAR+", System.nanoTime());";
								if(logger.isDebugEnabled()) {
									logger.debug("Transforming method '"+className+"."+method+"': inserting report call: "+callStr);
								}
								method.insertAfter(callStr);
							}
						}

					} while ((currentClass = currentClass.getSuperclass()) != null);

					byte[] result = ctClass.toBytecode();
					ctClass.defrost();
					return result;
				} catch (Exception e) {
					logger.error("An error occurred while transforming class "+className, e);
				}
			}
		}

		return null;
	}
}
