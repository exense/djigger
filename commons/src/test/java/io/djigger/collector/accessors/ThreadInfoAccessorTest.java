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
package io.djigger.collector.accessors;

import ch.exense.djigger.collector.accessors.ThreadInfoAccessor;
import io.djigger.monitoring.java.model.ThreadInfo;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class ThreadInfoAccessorTest {


    public void test() throws Exception {

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss");


        Date from = f.parse("20140902 09:00:00");
        Date to = f.parse("20140902 09:00:10");


        System.out.println("Mongo");
        for (int i = 0; i < 10; i++) {
//			ThreadInfoAccessorImpl b = new ThreadInfoAccessorImpl();
//			b.start("c600883", "djigger_test");
//			query(b, from, to);
        }


    }

    private void query(ThreadInfoAccessor a, Date from, Date to) throws Exception {
        long t1 = System.currentTimeMillis();
        Iterator<ThreadInfo> it = a.query(new Document("env", "S01"), from, to).iterator();
        int c = 0;
        while (it.hasNext()) {
            it.next();
            c++;
        }
        System.out.println(c + "," + (System.currentTimeMillis() - t1));
    }
}
