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
import io.djigger.monitoring.java.instrumentation.TransformingSubscription;
import io.djigger.monitoring.java.model.StackTraceElement;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class RealNodePathSubscription extends InstrumentSubscription implements TransformingSubscription {

    static final long serialVersionUID = 173774663260136913L;

    private final StackTraceElement[] path;

    public RealNodePathSubscription(StackTraceElement[] path, boolean tagEvent) {
        super(tagEvent);
        this.path = path;
    }

    public StackTraceElement[] getPath() {
        return path;
    }

    @Override
    public boolean isRelatedToClass(CtClass class_) {
        return isRelatedToClassname(class_.getName());
    }

    private boolean isRelatedToClassname(String classname) {
        StackTraceElement lastNode = path[0];
        return lastNode.getClassName().equals(classname);
    }

    @Override
    public boolean isRelatedToMethod(CtMethod method) {
        StackTraceElement lastNode = path[0];
        return lastNode.getMethodName().equals(method.getName());
    }

    @Override
    public String toString() {
        StackTraceElement lastNode = path[0];
        return ".../" + lastNode.getClassName() + '.' + lastNode.getMethodName();
    }

    @Override
    public boolean retransformClass(Class<?> class_) {
        return isRelatedToClassname(class_.getName());
    }

    @Override
    public void transform(CtClass clazz, CtMethod method) throws CannotCompileException {
        TimeMeasureTransformer.transform(clazz, method, this, true);
    }
}
