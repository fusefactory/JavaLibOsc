package fuse.osc;

import fuse.osc.utils.OSCJavaToByteArrayConverter;

public class OSCMessage extends OSCPacket
{
	private String address;
	private Object[] arguments;
	
	public OSCMessage(String address)
	{
		this(address, null);
	}
	
	public OSCMessage(String address, Object[] arguments)
	{
		this.address = address;
		
		if (arguments == null) arguments = new Object[0];
		this.arguments = arguments;
	}
	
	public String address()
	{
		return address;
	}
	
	public Object[] arguments()
	{
		return arguments;
	}
	
	@Override
	protected byte[] computeByteArray(OSCJavaToByteArrayConverter stream)
	{
		computeAddressByteArray(stream);
		computeArgumentsByteArray(stream);
		return stream.toByteArray();
	}
	
	/**
	 * Convert the address into a byte array.
	 * Used internally only.
	 * @param stream where to write the address to
	 */
	private void computeAddressByteArray(OSCJavaToByteArrayConverter stream)
	{
		stream.write(address);
	}
	
	/**
	 * Convert the arguments into a byte array.
	 * Used internally only.
	 * @param stream where to write the arguments to
	 */
	private void computeArgumentsByteArray(OSCJavaToByteArrayConverter stream)
	{
		stream.write(',');
		if (arguments != null)
		{
			stream.writeTypes(arguments);
			for (Object argument : arguments) stream.write(argument);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(address);
		if (arguments != null)
		{
			for (Object arg : arguments) builder.append(" " + arg);
		}
		return builder.toString();
	}
}
