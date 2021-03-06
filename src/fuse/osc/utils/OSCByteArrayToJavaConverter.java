package fuse.osc.utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fuse.osc.OSCBundle;
import fuse.osc.OSCMessage;
import fuse.osc.OSCPacket;

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
	private final String BUNDLE_START = "#bundle";
	private final char BUNDLE_IDENTIFIER = BUNDLE_START.charAt(0);
	
	private byte[] bytes;
	private Charset charset;
	private int bytesLength;
	private int streamPosition;
	
	/**
	 * Sets the character set used to decode message addresses
	 * and string parameters.
	 * @param charset the desired character-encoding-set to be used by this converter
	 */
	public void setCharset(Charset charset)
	{
		this.charset = charset;
	}

	/**
	 * Convert a byte array into an OSCMessage.
	 * @return an OSCMessage
	 */
	public OSCPacket convert(byte[] bytes, int bytesLength)
	{
		this.bytes = bytes;
		this.bytesLength = bytesLength;
		this.streamPosition = 0;
		
		if (bytes[0] == BUNDLE_IDENTIFIER) return convertBundle();
		else return convertMessage(); 
	}
	
	/**
	 * Converts the byte array to a bundle.
	 * Assumes that the byte array is a bundle.
	 * @return a bundle containing the data specified in the byte stream
	 */
	private OSCBundle convertBundle()
	{
		// skip the "#bundle " stuff
		streamPosition = BUNDLE_START.length() + 1;
		Date timestamp = readTimeTag();
		OSCBundle bundle = new OSCBundle(timestamp);
		OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
		converter.setCharset(charset);
		while (streamPosition < bytesLength)
		{
			// recursively read through the stream and convert packets you find
			Integer packetLength = readInteger();
			
			if (packetLength == 0) throw new IllegalArgumentException("Packet length may not be 0");
			else if ((packetLength % 4) != 0) throw new IllegalArgumentException("Packet length has to be a multiple of 4, is:" + packetLength);
			
			byte[] packetBytes = new byte[packetLength];
			System.arraycopy(bytes, streamPosition, packetBytes, 0, packetLength);
			streamPosition += packetLength;
			OSCPacket packet = converter.convert(packetBytes, packetLength);
			bundle.addPacket(packet);
		}
		return bundle;
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
	private Character readChar()
	{
		return new Character((char) bytes[streamPosition++]);
	}

	/**
	 * Read a double &mdash; this just read a float.
	 * @return a Double
	 */
	private Double readDouble()
	{
		return Double.valueOf(readFloat());
	}

	/**
	 * Read a float from the byte stream.
	 * @return a Float
	 */
	private Float readFloat()
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
	private BigInteger readBigInteger()
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
	private Integer readInteger()
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
	 * Reads the time tag and convert it to a Java Date object.
	 * A timestamp is a 64 bit number representing the time in NTP format.
	 * The first 32 bits are seconds since 1900, the second 32 bits are
	 * fractions of a second.
	 * @return a {@link Date}
	 */
	private Date readTimeTag()
	{
		byte[] secondBytes = new byte[8];
		byte[] fractionBytes = new byte[8];
		for (int i = 0; i < 4; i++)
		{
			// clear the higher order 4 bytes
			secondBytes[i] = 0;
			fractionBytes[i] = 0;
		}
		// while reading in the seconds & fraction, check if
		// this timetag has immediate semantics
		boolean isImmediate = true;
		for (int i = 4; i < 8; i++)
		{
			secondBytes[i] = bytes[streamPosition++];
			if (secondBytes[i] > 0) isImmediate = false;
		}
		for (int i = 4; i < 8; i++) {
			fractionBytes[i] = bytes[streamPosition++];
			if (i < 7)
			{
				if (fractionBytes[i] > 0) isImmediate = false;
			}
			else
			{
				if (fractionBytes[i] > 1) isImmediate = false;
			}
		}

		if (isImmediate) return OSCBundle.TIMESTAMP_IMMEDIATE;

		long secsSince1900 = new BigInteger(secondBytes).longValue();
		long secsSince1970 = secsSince1900 - OSCBundle.SECONDS_FROM_1900_TO_1970;

		// no point maintaining times in the distant past
		if (secsSince1970 < 0) secsSince1970 = 0;
		long fraction = (new BigInteger(fractionBytes).longValue());

		// this line was cribbed from jakarta commons-net's NTP TimeStamp code
		fraction = (fraction * 1000) / 0x100000000L;

		// I do not know where, but I'm losing 1ms somewhere...
		fraction = (fraction > 0) ? fraction + 1 : 0;
		long millisecs = (secsSince1970 * 1000) + fraction;
		return new Date(millisecs);
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
