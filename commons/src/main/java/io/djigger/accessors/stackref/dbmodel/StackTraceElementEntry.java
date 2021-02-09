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
package io.djigger.accessors.stackref.dbmodel;

import io.djigger.monitoring.java.model.StackTraceElement;

public class StackTraceElementEntry {

    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;


    public StackTraceElementEntry() {
        super();
    }

    public static StackTraceElementEntry[] toEntries(StackTraceElement[] elements) {
        StackTraceElementEntry[] e = new StackTraceElementEntry[elements.length];
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            e[i] = new StackTraceElementEntry(element.getClassName(), element.getMethodName(), element.getFileName(), element.getLineNumber());
        }
        return e;

    }

    public static StackTraceElement[] fromEntries(StackTraceElementEntry[] elements) {
        StackTraceElement[] e = new StackTraceElement[elements.length];
        for (int i = 0; i < elements.length; i++) {
            StackTraceElementEntry element = elements[i];
            e[i] = new StackTraceElement(element.getDeclaringClass(), element.getMethodName(), element.getFileName(), element.getLineNumber());
        }
        return e;

    }


    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public StackTraceElementEntry(String declaringClass, String methodName,
                                  String fileName, int lineNumber) {
        super();
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((declaringClass == null) ? 0 : declaringClass.hashCode());
        result = prime * result
            + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + lineNumber;
        result = prime * result
            + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StackTraceElementEntry other = (StackTraceElementEntry) obj;
        if (declaringClass == null) {
            if (other.declaringClass != null)
                return false;
        } else if (!declaringClass.equals(other.declaringClass))
            return false;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (lineNumber != other.lineNumber)
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }
}
