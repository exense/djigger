package io.djigger.test.e2e;

import java.io.Serializable;

public class TestClass implements Serializable {

    private static final long serialVersionUID = -4439843298237047511L;

    String att1;

    public TestClass() {
        super();
        // TODO Auto-generated constructor stub
    }

    public TestClass(String att1) {
        super();
        this.att1 = att1;
    }
}
