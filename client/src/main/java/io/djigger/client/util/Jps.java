package io.djigger.client.util;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
//import java.util.Properties;

public class Jps {
    public static void main(String[] args) {
        try {
            Class.forName("com.sun.tools.attach.VirtualMachineDescriptor");
        } catch (ClassNotFoundException ex) {
            System.err.println("Class com.sun.tools.attach.VirtualMachineDescriptor was not found.");
            System.err.println("Make sure that you are running on a supported JDK and that tools.jar is in the classpath if needed.");
            System.exit(1);
        }
        for (String vm : listVms()) {
            System.out.println(vm);
        }
    }

    public static List<String> listVms() {
        List<String> vms = new ArrayList<>();
        for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
            vms.add("pid=" + vm.id() + " displayName=" + vm.displayName());
        }
        return vms;
    }

//    private static void getMoreInfo(VirtualMachineDescriptor vmd) {
//        try {
//            VirtualMachine vm = VirtualMachine.attach(vmd);
//            dumpProperties("agentProperties", vm.getAgentProperties());
//            dumpProperties("systemProperties", vm.getSystemProperties());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void dumpProperties(String propsName, Properties props) {
//        System.out.println(propsName+":");
//        for (Map.Entry e: props.entrySet()) {
//            System.out.println("  " + e.getKey()+"="+e.getValue());
//        }
//        System.out.println();
//    }
}
