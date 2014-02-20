package fuse.osc.test;

import fuse.osc.OSCListener;
import fuse.osc.OSCMessage;
import fuse.osc.OSCReceiver;

public class ReceiverTest {

	public static void main(String[] args)
	{
		try
		{
			OSCReceiver receiver = new OSCReceiver(7000);
			receiver.addListener(new OSCListener()
			{
				@Override
				public void acceptMessage(OSCMessage message)
				{
					System.out.println("RECEIVED " + message);
				}
			});
			receiver.startListening();
			
			while (true) { }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
