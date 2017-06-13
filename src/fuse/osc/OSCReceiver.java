package fuse.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import fuse.osc.utils.OSCByteArrayToJavaConverter;

public class OSCReceiver extends OSCPort implements Runnable {
	private boolean isListening;
	private OSCByteArrayToJavaConverter converter;
	private OSCPacketDispatcher dispatcher;

	public OSCReceiver(int port) throws SocketException {
		this.port = port;
		socket = new DatagramSocket(port);
		converter = new OSCByteArrayToJavaConverter();
		dispatcher = new OSCPacketDispatcher();
	}

	public void startListening() {
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}

	public void stopListening() {
		isListening = false;
	}

	public boolean isListening() {
		return isListening;
	}

	public void addListener(OSCListener listener) {
		dispatcher.addListener(listener);
	}

	public void removeListener(OSCListener listener) {
		dispatcher.removeListener(listener);
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1536];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (isListening) {
			try {
				socket.receive(packet);
				OSCPacket oscPacket = converter.convert(buffer, packet.getLength());
				dispatcher.dispatchPacket(oscPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
