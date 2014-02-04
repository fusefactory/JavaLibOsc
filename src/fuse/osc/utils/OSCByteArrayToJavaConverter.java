package fuse.osc.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import fuse.osc.OSCMessage;

/**
 * Utility class to convert a byte array conforming to the OSC byte stream format
 * into Java objects.
 * <p>
 * Copyright (C) 2004-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 * <p>
 * See license.txt (or license.rtf) for license information.
 *
 * @author Chandrasekhar Ramakrishnan
 * @version 1.0
 */
public class OSCByteArrayToJavaConverter
{
	private byte[] bytes;
	private int streamPosition;

	/**
	 * Convert a byte array into an OSCMessage.
	 * @return an OSCMessage
	 */
	public OSCMessage convert(byte[] byteArray)
	{
		bytes = byteArray;
		streamPosition = 0;
		return convertMessage(); 
	}

	/**
	 * Convert the byte array a simple message. Assumes that the byte array is a message.
	 * @return a message containing the data specified in the byte stream
	 */
	private OSCMessage convertMessage()
	{
		String address = readString();
		List<Object> arguments = new ArrayList<Object>();

		char[] types = readTypes();
		if (types != null)
		{
			moveToFourByteBoundry();
			for (int i = 0; i < types.length; ++i)
			{
				if ('[' == types[i])
				{
					// we're looking at an array -- read it in
					arguments.addAll(readArray(types, ++i));
					// then increment i to the end of the array
					while (']' != types[i]) i++;
				}
				else arguments.add(readArgument(types[i]));
			}
		}
		
		return new OSCMessage(address, arguments.toArray());
	}

	/**
	 * Read a string from the byte stream.
	 * @return the next string in the byte stream
	 */
	private String readString()
	{
		int strLen = lengthOfCurrentString();
		char[] stringChars = new char[strLen];
		for (int i = 0; i < strLen; i++) stringChars[i] = (char) bytes[streamPosition++];
		moveToFourByteBoundry();
		return new String(stringChars);
	}

	/**
	 * Read the types of the arguments from the byte stream.
	 * @return a char array with the types of the arguments
	 */
	private char[] readTypes()
	{
		// the next byte should be a ","
		if (bytes[streamPosition] != 0x2C) return null;
		streamPosition++;
		// find out how long the list of types is
		int typesLen = lengthOfCurrentString();
		if (0 == typesLen) return null;

		// read in the types
		char[] typesChars = new char[typesLen];
		for (int i = 0; i < typesLen; i++) typesChars[i] = (char) bytes[streamPosition++];
		return typesChars;
	}

	/**
	 * Read an object of the type specified by the type char.
	 * @param c type of argument to read
	 * @return a Java representation of the argument
	 */
	private Object readArgument(char c)
	{
		switch (c)
		{
			case 'i' :
				return readInteger();
			case 'h' :
				return readBigInteger();
			case 'f' :
				return readFloat();
			case 'd' :
				return readDouble();
			case 's' :
				return readString();
			case 'c' :
				return readChar();
			case 'T' :
				return Boolean.TRUE;
			case 'F' :
				return Boolean.FALSE;
		}
		return null;
	}

	/**
	 * Read a char from the byte stream.
	 * @return a Character
	 */
	private Object readChar()
	{
		return new Character((char) bytes[streamPosition++]);
	}

	/**
	 * Read a double &mdash; this just read a float.
	 * @return a Double
	 */
	private Object readDouble()
	{
		return readFloat();
	}

	/**
	 * Read a float from the byte stream.
	 * @return a Float
	 */
	private Object readFloat()
	{
		byte[] floatBytes = new byte[4];
		floatBytes[0] = bytes[streamPosition++];
		floatBytes[1] = bytes[streamPosition++];
		floatBytes[2] = bytes[streamPosition++];
		floatBytes[3] = bytes[streamPosition++];
		BigInteger floatBits = new BigInteger(floatBytes);
		return new Float(Float.intBitsToFloat(floatBits.intValue()));
	}

	/**
	 * Read a Big Integer (64 bit int) from the byte stream.
	 * @return a BigInteger
	 */
	private Object readBigInteger()
	{
		byte[] longintBytes = new byte[8];
		longintBytes[0] = bytes[streamPosition++];
		longintBytes[1] = bytes[streamPosition++];
		longintBytes[2] = bytes[streamPosition++];
		longintBytes[3] = bytes[streamPosition++];
		longintBytes[4] = bytes[streamPosition++];
		longintBytes[5] = bytes[streamPosition++];
		longintBytes[6] = bytes[streamPosition++];
		longintBytes[7] = bytes[streamPosition++];
		return new BigInteger(longintBytes);
	}

	/**
	 * Read an Integer (32 bit int) from the byte stream.
	 * @return an Integer
	 */
	private Object readInteger()
	{
		byte[] intBytes = new byte[4];
		intBytes[0] = bytes[streamPosition++];
		intBytes[1] = bytes[streamPosition++];
		intBytes[2] = bytes[streamPosition++];
		intBytes[3] = bytes[streamPosition++];
		BigInteger intBits = new BigInteger(intBytes);
		return new Integer(intBits.intValue());
	}
	
	/**
	 * Read an array from the byte stream.
	 * @param types
	 * @param i
	 * @return an Array
	 */
	private List<Object> readArray(char[] types, int i)
	{
		int arrayLen = 0;
		while (types[i + arrayLen] != ']') arrayLen++;
		List<Object> array = new ArrayList<Object>();
		for (int j = 0; j < arrayLen; j++) array.add(readArgument(types[i + j]));
		return array;
	}

	/**
	 * Get the length of the string currently in the byte stream.
	 */
	private int lengthOfCurrentString()
	{
		int i = 0;
		while (bytes[streamPosition + i] != 0) i++;
		return i;
	}

	/**
	 * Move to the next byte with an index in the byte array divisable by four.
	 */	
	private void moveToFourByteBoundry()
	{
		// If i'm already at a 4 byte boundry, I need to move to the next one
		int mod = streamPosition % 4;
		streamPosition += (4 - mod);
	}
}
