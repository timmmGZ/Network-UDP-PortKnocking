import java.util.*;
import java.net.*;
import java.io.*;

public class UdpServer extends DatagramSocket implements Runnable {
	public static final String TCP_SERVER_NOTIFICATION = "###TCP SERVER NOTIFICATION### ";
	static final List<Integer> UDP_SEQUENCE_1 = Arrays.asList(2568, 3745, 7512);
	static final List<Integer> UDP_SEQUENCE_2 = Arrays.asList(1324, 5432, 1324);
	static final List<Integer> UDP_SEQUENCE_3 = Arrays.asList(4132, 3152, 2243, 6021);
	static final List<List<Integer>> UDP_SEQUENCES = Arrays.asList(UDP_SEQUENCE_1, UDP_SEQUENCE_2, UDP_SEQUENCE_3);
	static final int ALL_PORTS[] = { 2568, 3745, 7512, 1324, 5432, 4132, 3152, 2243, 6021 };
	static Map<String, List<Integer>> m = new TreeMap<>();
	DatagramPacket packet;
	byte[] bufor;

	public static void main(String[] args) {
		int i = 0;
		while (i < ALL_PORTS.length)
			try {
				new Thread(new UdpServer(ALL_PORTS[i++])).start();
			} catch (SocketException e) {
				System.err.println("Address already in use: Cannot bind");
				System.exit(1);
			}
	}

	public UdpServer(int port) throws SocketException {
		super(port);
		setBroadcast(true);
	}

	@Override
	public void run() {
		try {
			while (true) {
				bufor = new byte[256];
				packet = new DatagramPacket(bufor, bufor.length);
				receive(packet);
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				String received = new String(packet.getData(), 0, packet.getLength());
				String name = address.getHostAddress() + ":" + port;
				System.out.println("[" + name + "] " + received + " (UDP message)");
				List<Integer> l = m.get(name) == null ? new ArrayList<>() : m.get(name);
				l.add(this.getLocalPort());
				m.put(name, l);
				if (received.equals("last")) {
					check(name);
				}
				packet = new DatagramPacket(bufor, bufor.length, address, port);
				send(packet);

			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (this != null)
				close();
			System.exit(1);
		}

	}

	public void check(String name) {
		List<Integer> l = m.get(name);
		int tcpPort = 8888 + (int) (Math.random() * 2);
		boolean check = UDP_SEQUENCES.contains(l);
		bufor = check ? (TCP_SERVER_NOTIFICATION + "correct ports sequence, TCP Port: " + tcpPort
				+ " is opening now, for security, you have 10 seconds to connect to it, or it will be closed and you have to Port-Knocking again!")
						.getBytes()
				: "N".getBytes();
		if (check)
			new Thread(() -> {
				try {
					TcpServer t = new TcpServer(tcpPort);
					t.start();
					t.close();
				} catch (IOException e1) {
				}
			}).start();

	}
}