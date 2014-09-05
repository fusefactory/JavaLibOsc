package fuse.osc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fuse.osc.utils.OSCJavaToByteArrayConverter;

public class OSCBundle extends OSCPacket
{
	/**
	 * 2208988800 seconds -- includes 17 leap years
	 */
	private static final long SECONDS_FROM_1900_TO_1970 = 2208988800L;

	/**
	 * The Java representation of an OSC timestamp with the semantics of
	 * "immediately".
	 */
	private static final Date TIMESTAMP_IMMEDIATE = new Date(0);
	
	private Date timestamp;
	private List<OSCPacket> packets;
	
	public OSCBundle()
	{
		this(TIMESTAMP_IMMEDIATE);
	}
	
	public OSCBundle(Date timestamp)
	{
		this(null, timestamp);
	}
	
	public OSCBundle(Collection<OSCPacket> packets)
	{
		this(packets, TIMESTAMP_IMMEDIATE);
	}
	
	public OSCBundle(Collection<OSCPacket> packets, Date timestamp)
	{
		if (null == packets) this.packets = new LinkedList<OSCPacket>();
		else this.packets = new ArrayList<OSCPacket>(packets);
		this.timestamp = timestamp;
	}
	
	/**
	 * Return the time the bundle will execute.
	 * @return a Date
	 */
	public Date getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Set the time the bundle will execute.
	 * @param timestamp Date
	 */
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public List<OSCPacket> getPackets()
	{
		return Collections.unmodifiableList(packets);
	}
	
	public void addPacket(OSCPacket packet)
	{
		packets.add(packet);
		contentChanged();
	}

	@Override
	protected byte[] computeByteArray(OSCJavaToByteArrayConverter stream)
	{
		stream.write("#bundle");
		computeTimeTagByteArray(stream);
		byte[] packetBytes;
		for (OSCPacket pkg : packets) {
			packetBytes = pkg.getByteArray();
			stream.write(packetBytes);
		}
		return stream.toByteArray();
	}
	
	/**
	 * Convert the time-tag (a Java Date) into the OSC byte stream.
	 * Used Internally.
	 * @param stream where to write the time-tag to
	 */
	private void computeTimeTagByteArray(OSCJavaToByteArrayConverter stream)
	{
		if ((null == timestamp) || (timestamp == TIMESTAMP_IMMEDIATE)) {
			stream.write((int) 0);
			stream.write((int) 1);
			return;
		}

		long millisecs = timestamp.getTime();
		long secsSince1970 = (long) (millisecs / 1000);
		long secs = secsSince1970 + SECONDS_FROM_1900_TO_1970;

		// this line was cribbed from jakarta commons-net's NTP TimeStamp code
		long fraction = ((millisecs % 1000) * 0x100000000L) / 1000;

		stream.write((int) secs);
		stream.write((int) fraction);
	}
}
