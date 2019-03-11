package fuse.osc.utils;

import java.util.ArrayList;
import fuse.osc.OSCMessage;

/**
 * Helper class that provides methods to extract addressee information (host/port)
 * from a confirm info string.
 *
 * An confirm info string has the following format:
 * "confirm:<host>:<port>[:<code>]"
 * for example:
 * "confirm:127.0.0.1:8080"
 * "confirm:192.168.1.100:8001"
 * "confirm:192.168.1.100:8001:confirmcode"
 * "confirm:192.168.1.100:8001:abc"
 * "confirm:192.168.1.100:8001:123"
 */
class Info {
    public static final int INVALID_PORT = -1;

    public static String GetIp(String info) {
        if (info == null) return null;
        String[] parts = info.split(":");
        return parts.length >= 3 && parts[0].equals("confirm") ? parts[1] : null;
    }

    private static String GetPortString(String info) {
        if (info == null) return null;
        String[] parts = info.split(":");
        return parts.length >= 3 && parts[0].equals("confirm") ? parts[2] : null;
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

    public static String GetCode(String info) {
        if (info == null) return null;
        String[] parts = info.split(":");
        return parts.length == 4 && parts[0].equals("confirm") ? parts[3] : null;
    }
}

/**
 * The Confirm class provides helper methods for implementing a confirmation
 * response protocol on OSC.
 *
 * The 'protocol' basically means that sender of OSC messages
 * that wants to receive a confirmation repsonse, adds a string-based argument
 * to the message to which it wants a confirmation response. This argument contains
 * the information necessary to send a confirmation (the host and port to which to send it).
 *
 * The receiver that supports this protocol is expected to respond with a confirmation
 * to the specified host/port and the confirmation is expected to be a copy of the
 * original message, with a "/confirm" postfix to the message's address and minus
 * the last argument that contains the confirm details.
 */
public class Confirm {

    public static final String POSTFIX = "/confirm";

    public static String GetConfirmHost(OSCMessage msg) {
      return Info.GetIp(GetConfirmationInfo(msg));
    }

    public static int GetConfirmPort(OSCMessage msg) {
      return Info.GetPort(GetConfirmationInfo(msg));
    }

    public static String GetConfirmCode(OSCMessage msg) {
      return Info.GetCode(GetConfirmationInfo(msg));
    }

    public static String GetConfirmationInfo(OSCMessage msg) {
        Object[] args = msg.arguments();
        if (args.length < 1) return null;
        Object arg = args[args.length-1];
        // String result = null;
        // try {
        //     result = (String)arg;
        // } catch(Exception e) {
        //     result = null;
        // }
        // return result;
        String info = (arg instanceof String) ? (String)arg : null;
        return info != null && info.startsWith("confirm:") ? info : null;
    }

    public static OSCMessage CreateConfirmation(OSCMessage confirmableMessage) {
        String ip = GetConfirmHost(confirmableMessage);
        int port = GetConfirmPort(confirmableMessage);
        // did not find valid confirm addressee info; cannot create confirmation
        if (ip == null || port == Info.INVALID_PORT) return null;

        String code = GetConfirmCode(confirmableMessage);

        // assemble message with confirm postfix
        OSCMessage confirmation;
        if (code != null) {
            confirmation = new OSCMessage(confirmableMessage.address()+POSTFIX, new Object[]{ code });
        } else {
            // copy all argumnets from the confirmable message to the confirmation,
            // except for the last one (which contains confirm details)
            Object[] args = confirmableMessage.arguments();
            ArrayList<Object> newargs = new ArrayList<Object>();

            for(int i=0; i<args.length-1; i++) {
                newargs.add(args[i]);
            }

            confirmation = new OSCMessage(confirmableMessage.address()+POSTFIX, newargs.toArray());
        }

        return confirmation;
    }

    // public static OSCMessage CreateConfirmable(OSCMessage original, String confirmHost, int confirmPort) {
    //  // TODO: this method should return a new OSCMessage that's a clone
    //  // of the original, with a string-based argument appended to it that contains
    //  // the confirmation host and port.
    // }

} // class Confirm
