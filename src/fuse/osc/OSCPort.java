package fuse.osc;

import java.net.DatagramSocket;

public abstract class OSCPort
{
	protected int port;
	protected DatagramSocket socket;
	
	public void close()
	{
		socket.close();
	}
}
