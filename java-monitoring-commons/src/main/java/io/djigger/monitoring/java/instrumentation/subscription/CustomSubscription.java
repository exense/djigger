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

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class CustomSubscription extends RegexSubscription {

	private static final long serialVersionUID = -6393188686449175487L;

	protected boolean captureThreadInfos = false;
	
	protected String capture;
	
	protected Integer maxCaptureSize;
	
	protected String beforeMethod;
	
	protected String afterMethod;
	
	protected boolean measureDuration;
	
	public String getCapture() {
		return capture;
	}

	public void setCapture(String capture) {
		this.capture = capture;
	}
	
	public boolean isCaptureThreadInfos() {
		return captureThreadInfos;
	}

	public void setCaptureThreadInfos(boolean captureThreadInfos) {
		this.captureThreadInfos = captureThreadInfos;
	}

	public Integer getMaxCaptureSize() {
		return maxCaptureSize;
	}

	public void setMaxCaptureSize(Integer maxCaptureSize) {
		this.maxCaptureSize = maxCaptureSize;
	}

	public CustomSubscription(String classNameRegex, String methodNameRegex, boolean tagEvent) {
		super(classNameRegex, methodNameRegex, tagEvent);
	}

	@Override
	public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
		if(beforeMethod!=null) {
			method.insertBefore(beforeMethod);			
		}
		if(afterMethod!=null) {
			method.insertAfter(afterMethod);
		}
		if(measureDuration || capture!=null) {
			if(capture==null) {
				TimeMeasureTransformer.transform(clazz, method, this, captureThreadInfos);							
			} else {
				TimeMeasureTransformer.transform(clazz, method, this, captureThreadInfos, capture, maxCaptureSize);							
			}
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "/Before=" + beforeMethod + "/After=" + afterMethod + "/Capture=" + capture;
	}
}
