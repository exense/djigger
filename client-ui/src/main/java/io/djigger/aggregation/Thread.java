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
package io.djigger.aggregation;

import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ui.model.RealNodePath;

import java.util.ArrayList;
import java.util.List;

public class Thread {

    long id;

    List<RealNodePathWrapper> realNodePathSequence = new ArrayList<>();

    public Thread(long id, List<RealNodePathWrapper> realNodePathSequence) {
        super();
        this.realNodePathSequence = realNodePathSequence;
        this.id = id;
    }

    public List<RealNodePathWrapper> getRealNodePathSequence() {
        return realNodePathSequence;
    }

    public void setRealNodePathSequence(List<RealNodePathWrapper> realNodePathSequence) {
        this.realNodePathSequence = realNodePathSequence;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static class RealNodePathWrapper {

        private RealNodePath path;

        private ThreadInfo threadInfo;

        public RealNodePathWrapper(RealNodePath path, ThreadInfo threadInfo) {
            super();
            this.path = path;
            this.threadInfo = threadInfo;
        }

        public RealNodePath getPath() {
            return path;
        }

        protected void setPath(RealNodePath path) {
            this.path = path;
        }

        public ThreadInfo getThreadInfo() {
            return threadInfo;
        }

        protected void setThreadInfo(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }


    }
}
