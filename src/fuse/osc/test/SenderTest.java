package fuse.osc.test;

import java.net.InetAddress;

import fuse.osc.OSCMessage;
import fuse.osc.OSCSender;

public class SenderTest
{
	public static void main(String[] args)
	{
		try
		{
			OSCSender sender = new OSCSender(InetAddress.getByName("127.0.0.1"), 7000);
			sender.send(new OSCMessage("/prova/", null));
			sender.send(new OSCMessage("/prova/", new Object[] { true, "Paolo" }));
			sender.send(new OSCMessage("/prova/", null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
