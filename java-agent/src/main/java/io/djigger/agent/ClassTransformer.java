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
import io.djigger.monitoring.java.instrumentation.InstrumentationAttributes;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

public class ClassTransformer implements ClassFileTransformer {

	private final InstrumentationService service;

	ClassTransformer(InstrumentationService service) {
		super();
		this.service = service;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if(classBeingRedefined!=null) {
			Set<InstrumentSubscription> subscriptions = service.getSubscriptions();
			if(subscriptions!=null && subscriptions.size()>0) {
				System.out.println("Redefining " + classBeingRedefined.getName());
				ClassPool pool = ClassPool.getDefault();
				if(loader!=null) {
					System.out.println("Appending classpath: " + loader);
					pool.appendClassPath(new LoaderClassPath(loader));
				}
				try {
					CtClass ctClass = pool
							.makeClass(new java.io.ByteArrayInputStream(
									classfileBuffer));
					CtClass currentClass = ctClass;
					do {
						for (CtMethod method : currentClass.getDeclaredMethods()) {
							InstrumentationAttributes attributes = null;
							for(InstrumentSubscription subscription:subscriptions) {
								if (subscription.isRelatedToClass(currentClass.getName()) && subscription.isRelatedToMethod(method.getName())) {
									if(attributes == null) {
										attributes = new InstrumentationAttributes();
									}
									attributes.merge(subscription.getInstrumentationAttributes());
								}
							}
							if(attributes!=null && !Modifier.isNative(method.getModifiers())) {
								//method.instrument(new CodeConverter());
								StringBuilder attributesBuilder = new StringBuilder();
								attributesBuilder.append("new boolean[]{");
								for(boolean attribute:attributes.getAttributes()) {
									attributesBuilder.append(attribute).append(",");
								}
								attributesBuilder.append("true");
								attributesBuilder.append("}");
								
								method.addLocalVariable("t1", CtClass.longType);
								method.insertBefore("t1 = System.currentTimeMillis();io.djigger.agent.Collector.start(this, \"" + currentClass.getName() + "\",\"" + method.getName() + "\");");
								System.out.println("io.djigger.agent.Collector.report(\"" + currentClass.getName() + "\",\"" + method.getName() + "\", t1," + attributesBuilder.toString() + ");");
								method.insertAfter("io.djigger.agent.Collector.report(\"" + currentClass.getName() + "\",\"" + method.getName() + "\", t1," + attributesBuilder.toString() + ");");
							}
						}

					} while ((currentClass = currentClass.getSuperclass()) != null);

					byte[] result = ctClass.toBytecode();
					ctClass.defrost();
					return result;


				} catch (CannotCompileException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
