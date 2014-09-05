package fuse.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class OSCSender extends OSCPort
{
	private InetAddress ip;
	
	public OSCSender(InetAddress ip, int port) throws SocketException
	{
		this.ip = ip;
		this.port = port;
		socket = new DatagramSocket();
	}
	
	public void send(OSCPacket oscPacket) throws IOException
	{
		byte[] byteArray = oscPacket.getByteArray();
		DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length, ip, port);
		socket.send(packet);
	}
}
