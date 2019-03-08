package fuse.osc.utils;

import java.util.ArrayList;
import fuse.osc.OSCMessage;

class Info {
    public static final int INVALID_PORT = -1;

    public static String GetIp(String info) {
        if (info == null) return null;
        String[] parts = info.split(":");
        return parts.length == 3 && parts[0].equals("confirm") ? parts[1] : null;
    }

    private static String GetPortString(String info) {
        if (info == null) return null;
        String[] parts = info.split(":");
        return parts.length == 3 && parts[0].equals("confirm") ? parts[1] : null;
    }

    public static int GetPort(String info) {
        String portString = GetPortString(info);
        int port = INVALID_PORT;

        try {
          port = Integer.parseInt(portString);
        } catch(Exception e) {
        }

        return port;
    }
}

public class Confirm {

    public static final String POSTFIX = "/confirm";

    // public static OSCMessage CreateConfirmable(OSCMessage original, string ip, int port) {
    //     // var c = new OSCMessage(original.Address);
    //     //
    //     // foreach(var arg in original.Data) {
    //     //     switch (arg.GetType().Name) {
    //     //         case "Int32": c.Append<int>((int)arg); break;
    //     //         case "Int64": c.Append<long>((long)arg); break;
    //     //         case "Single":c.Append<float>((float)arg); break;
    //     //         case "Double":c.Append<double>((double)arg); break;
    //     //         case "String":c.Append<string>((string)arg); break;
    //     //         case "Byte[]":c.Append<byte>((byte)arg); break;
    //     //         default: throw new System.Exception("Unsupported OSC argument type.");
    //     //     }
    //     // }
    //     //
    //     // c.Append<string>("confirm:"+ip+":"+port.ToString());
    //     // return c;
    // }

    public static String GetConfirmationInfo(OSCMessage msg) {
        Object[] args = msg.arguments();
        if (args.length < 1) return null;
        Object arg = args[args.length-1];
        return String.class.isInstance(args) ? (String)arg : null;
    }

    public static OSCMessage CreateConfirmation(OSCMessage msg) {
        String ip = Info.GetIp(GetConfirmationInfo(msg));
        int port = Info.GetPort(GetConfirmationInfo(msg));
        if (ip == null || port == Info.INVALID_PORT) return null;

        Object[] args = msg.arguments();
        ArrayList<Object> newargs = new ArrayList<Object>();

        for(int i=0; i<args.length-1; i++) {
            newargs.add(args[i]);
        }

        OSCMessage c = new OSCMessage(msg.address()+POSTFIX, newargs.toArray());
        return c;
    }

} // class Confirm
