package io.djigger.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class XStreamFactory {
    public static XStream createWithAllTypesPermission() {
        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        return xstream;
    }
}
