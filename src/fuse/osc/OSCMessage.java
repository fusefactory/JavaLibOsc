package fuse.osc;

import java.util.Arrays;
import java.util.List;

import fuse.osc.utils.OSCJavaToByteArrayConverter;

public class OSCMessage
{
	private String address;
	private Object[] arguments;
	private boolean isByteArrayComputed;
	private byte[] byteArray;
	
	public OSCMessage(String address)
	{
		this(address, null);
	}
	
	public OSCMessage(String address, Object[] arguments)
	{
		this.address = address;
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
	
	public byte[] getByteArray()
	{
		if (!isByteArrayComputed)
		{
			computeByteArray();
			isByteArrayComputed = true;
		}
		return byteArray;
	}
	
	private void computeByteArray()
	{
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		computeAddressByteArray(stream);
		computeArgumentsByteArray(stream);
		byteArray = stream.toByteArray();
	}
	
	private void computeAddressByteArray(OSCJavaToByteArrayConverter stream)
	{
		stream.write(address);
	}
	
	private void computeArgumentsByteArray(OSCJavaToByteArrayConverter stream)
	{
		stream.write(',');
		if (arguments != null)
		{
			List<Object> objects = Arrays.asList(arguments);
			stream.writeTypes(objects);
			for (int i = 0; i < objects.size(); i++) stream.write(objects.get(i));
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
