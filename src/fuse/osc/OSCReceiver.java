package fuse.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import fuse.osc.utils.OSCByteArrayToJavaConverter;

public class OSCReceiver extends OSCPort implements Runnable
{
	private boolean isListening;
	private List<OSCListener> listeners;
	private OSCByteArrayToJavaConverter converter;
	
	public OSCReceiver(int port) throws SocketException
	{
		this.port = port;
		socket = new DatagramSocket(port);
		listeners = new ArrayList<OSCListener>();
		converter = new OSCByteArrayToJavaConverter();
	}
	
	public void startListening()
	{
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void stopListening()
	{
		isListening = false;
	}
	
	public boolean isListening()
	{
		return isListening;
	}
	
	public void addListener(OSCListener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void run()
	{
		byte[] buffer = new byte[1536];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (isListening)
		{
			try
			{
				socket.receive(packet);
				OSCMessage message = converter.convert(buffer, packet.getLength());
				for (OSCListener listener : listeners) listener.acceptMessage(message);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}
