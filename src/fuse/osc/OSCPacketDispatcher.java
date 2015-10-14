package fuse.osc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OSCPacketDispatcher
{
	private List<OSCListener> listeners;
	
	public OSCPacketDispatcher()
	{
		listeners = new ArrayList<OSCListener>();
	}
	
	public void addListener(OSCListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(OSCListener listener)
	{
		listeners.remove(listener);
	}
	
	public void dispatchPacket(OSCPacket packet)
	{
		dispatchPacket(packet, null);
	}
	
	public void dispatchPacket(OSCPacket packet, Date timestamp)
	{
		if (packet instanceof OSCBundle) dispatchBundle((OSCBundle) packet);
		else dispatchMessage((OSCMessage) packet, timestamp);
	}
	
	private void dispatchBundle(OSCBundle bundle)
	{
		Date timestamp = bundle.getTimestamp();
		List<OSCPacket> packets = bundle.getPackets();
		for (OSCPacket packet : packets) dispatchPacket(packet, timestamp);
	}
	
	private void dispatchMessage(OSCMessage message, Date time)
	{
		for (OSCListener listener : listeners) listener.acceptMessage(message);
	}
}
