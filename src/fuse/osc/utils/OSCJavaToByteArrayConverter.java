package fuse.osc.utils;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

/**
 * OSCJavaToByteArrayConverter is a helper class that translates
 * from Java types to their byte stream representations according to
 * the OSC spec.
 */
public class OSCJavaToByteArrayConverter
{
	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private byte[] intBytes = new byte[4];
	private byte[] longintBytes = new byte[8];
	private char[] stringChars = new char[2048];
	private byte[] stringBytes = new byte[2048];

	/**
	 * Line up the Big end of the bytes to a 4 byte boundry
	 * @return byte[]
	 * @param bytes byte[]
	 */
	private byte[] alignBigEndToFourByteBoundry(byte[] bytes)
	{
		int mod = bytes.length % 4;
		if (mod == 0) return bytes;
		int pad = 4 - mod;
		byte[] newBytes = new byte[pad + bytes.length];
		for (int i = 0; i < pad; i++) newBytes[i] = 0;
		for (int i = 0; i < bytes.length; i++) newBytes[pad + i] = bytes[i];
		return newBytes;
	}

	/**
	 * Pad the stream to have a size divisible by 4.
	 */
	public void appendNullCharToAlignStream()
	{
		int mod = stream.size() % 4;
		int pad = 4 - mod;
		for (int i = 0; i < pad; i++) stream.write(0);
	}

	/**
	 * Convert the contents of the output stream to a byte array.
	 * @return the byte array containing the byte stream
	 */
	public byte[] toByteArray()
	{
		return stream.toByteArray();
	}

	/**
	 * Write bytes into the byte stream.
	 * @param bytes byte[]
	 */
	public void write(byte[] bytes)
	{
		writeUnderHandler(bytes);
	}

	/**
	 * Write an int into the byte stream.
	 * @param i int
	 */
	public void write(int i)
	{
		writeInteger32ToByteArray(i);
	}

	/**
	 * Write a float into the byte stream.
	 * @param f java.lang.Float
	 */
	public void write(Float f)
	{
		writeInteger32ToByteArray(Float.floatToIntBits(f.floatValue()));
	}

	/**
	 * @param i java.lang.Integer
	 */
	public void write(Integer i)
	{
		writeInteger32ToByteArray(i.intValue());
	}
	
	/**
	 * @param i java.lang.Integer
	 */
	public void write(BigInteger i)
	{
		writeInteger64ToByteArray(i.longValue());
	}	

	/**
	 * Write a string into the byte stream.
	 * @param string java.lang.String
	 */
	public void write(String string)
	{
		int stringLength = string.length();
		string.getChars(0, stringLength, stringChars, 0);
		int mod = stringLength % 4;
		int pad = 4 - mod;
		for (int i = 0; i < pad; i++) stringChars[stringLength++] = 0;
		for (int i = 0; i < stringLength; i++) stringBytes[i] = (byte) (stringChars[i] & 0x00FF);
		stream.write(stringBytes, 0, stringLength);		
	}

	/**
	 * Write a char into the byte stream.
	 * @param c char
	 */
	public void write(char c)
	{
		stream.write(c);
	}

	/**
	 * Write an object into the byte stream.
	 * @param object one of Float, String, Integer, BigInteger, or array of these.
	 */
	public void write(Object object)
	{
		if (object != null)
		{
			if (object instanceof Object[])
			{
				Object[] theArray = (Object[]) object;
				for (int i = 0; i < theArray.length; ++i) write(theArray[i]);
			}
			else if (object instanceof Float) write((Float) object);
			else if (object instanceof String) write((String) object);
			else if (object instanceof Integer) write((Integer) object); 
			else if (object instanceof BigInteger) write((BigInteger) object);
		}
	}

	/**
	 * Write the type tag for the type represented by the class
	 * @param c Class of a Java object in the arguments
	 */
	public void writeType(Class<? extends Object> c)
	{
		if (Integer.class.equals(c)) stream.write('i');
		else if (java.math.BigInteger.class.equals(c)) stream.write('h');
		else if (Float.class.equals(c)) stream.write('f');
		else if (Double.class.equals(c)) stream.write('d');
		else if (String.class.equals(c)) stream.write('s');
		else if (Character.class.equals(c)) stream.write('c');
	}

	/**
	 * Write the types for an array element in the arguments.
	 * @param array java.lang.Object[]
	 */
	public void writeTypesArray(Object[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (null == array[i]) continue;
			else if (Boolean.TRUE.equals(array[i])) stream.write('T');
			else if (Boolean.FALSE.equals(array[i])) stream.write('F');
			else writeType(array[i].getClass());
		}
	}
	
	/**
	 * Write types for the arguments (use a vector for jdk1.1 compatibility, rather than an ArrayList).
	 * @param objects the arguments to an OSCMessage
	 */
	public void writeTypes(List<Object> objects)
	{
		for (Object object : objects)
		{
			if (null == object) continue;
			else if (object.getClass().isArray())
			{
				stream.write('[');
				writeTypesArray((Object[]) object);
				stream.write(']');
			}
			else if (Boolean.TRUE.equals(object)) stream.write('T');
			else if (Boolean.FALSE.equals(object)) stream.write('F');
			else writeType(object.getClass());
		}
		// align the stream with padded bytes
		appendNullCharToAlignStream();
	}

	/**
	 * Write bytes to the stream, catching IOExceptions and converting them to RuntimeExceptions.
	 * @param bytes byte[]
	 */
	private void writeUnderHandler(byte[] bytes)
	{
		try
		{
			stream.write(alignBigEndToFourByteBoundry(bytes));
		}
		catch (IOException e)
		{
			throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
		}
	}

	/**
	 * Write a 32 bit integer to the byte array without allocating memory.
	 * @param value a 32 bit int.
	 */
	private void writeInteger32ToByteArray(int value)
	{
		intBytes[3] = (byte)value; value>>>=8;
		intBytes[2] = (byte)value; value>>>=8;
		intBytes[1] = (byte)value; value>>>=8;
		intBytes[0] = (byte)value;

		try
		{
			stream.write(intBytes);
		}
		catch (IOException e)
		{
			throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
		}
	}

	/**
	 * Write a 64 bit integer to the byte array without allocating memory.
	 * @param value a 64 bit int.
	 */
	private void writeInteger64ToByteArray(long value)
	{
		longintBytes[7] = (byte)value; value>>>=8;
		longintBytes[6] = (byte)value; value>>>=8;
		longintBytes[5] = (byte)value; value>>>=8;
		longintBytes[4] = (byte)value; value>>>=8;
		longintBytes[3] = (byte)value; value>>>=8;
		longintBytes[2] = (byte)value; value>>>=8;
		longintBytes[1] = (byte)value; value>>>=8;
		longintBytes[0] = (byte)value;

		try
		{
			stream.write(longintBytes);
		}
		catch (IOException e)
		{
			throw new RuntimeException("You're screwed: IOException writing to a ByteArrayOutputStream");
		}
	}
}

