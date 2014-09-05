package fuse.osc;

import java.nio.charset.Charset;

import fuse.osc.utils.OSCJavaToByteArrayConverter;

public abstract class OSCPacket
{
	private Charset charset;
	private byte[] byteArray;
	
	public OSCPacket()
	{
		charset = Charset.defaultCharset();
	}
	
	public Charset getCharset()
	{
		return charset;
	}

	public void setCharset(Charset charset)
	{
		this.charset = charset;
	}
	
	public byte[] getByteArray()
	{
		if (byteArray == null) byteArray = computeByteArray();
		return byteArray;
	}
	
	/**
	 * Generate a representation of this packet conforming to the
	 * the OSC byte stream specification. Used Internally.
	 */
	private byte[] computeByteArray()
	{
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		stream.setCharset(charset);
		byte[] bytes = computeByteArray(stream);
		return bytes;
	}
	
	/**
	 * Produces a byte array representation of this packet.
	 * @param stream where to write the arguments to
	 * @return the OSC specification conform byte array representation
	 *   of this packet
	 */
	protected abstract byte[] computeByteArray(OSCJavaToByteArrayConverter stream);
	
	protected void contentChanged()
	{
		byteArray = null;
	}
}
