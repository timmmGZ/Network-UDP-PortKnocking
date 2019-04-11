import java.net.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.*;

public class UdpClient extends DatagramSocket {
	static BufferedReader send = new BufferedReader(new InputStreamReader(System.in));
	int tcpPort;

	public UdpClient() throws IOException {
		super();
		String serverIP;
		System.out.println("Please enter the Server IP£º");
		serverIP = send.readLine();
		System.out.println("Please enter the ports in correct sequence, split by \",\"");
		String portsInLine = send.readLine();

		List<Integer> ports = Pattern.compile(",").splitAsStream(portsInLine).map(s -> Integer.parseInt(s))
				.collect(Collectors.toList());

		this.setSoTimeout(2000);
		for (Integer i : ports)
			if (ports.get(ports.size() - 1) != i) {
				knock(i, serverIP, "");
			} else {
				knock(i, serverIP, "last");
			}
		close();
		int port;
		System.out.println("Please enter the port for TCP connection£º");
		port = Integer.parseInt(send.readLine());
		if (port != tcpPort)
			System.out.println("java.net.ConnectException: Connection refused: connect");
		else {
			TcpClient client = new TcpClient(serverIP, port);
			client.start();
			client.close();
		}
	}

	byte[] bufor;
	DatagramPacket packet;

	public static void main(String[] args) throws IOException {
		UdpClient u = null;
		try {
			u = new UdpClient();
		} catch (SocketException e) {
			System.err.println("1 " + e);
			System.exit(1);
		} finally {
			u.close();
		}

	}

	public void knock(int port, String IP, String send) {
		bufor = send.getBytes();
		InetAddress address = null;
		try {
			address = InetAddress.getByName(IP);
		} catch (UnknownHostException e1) {
			System.err.println("2 " + IP);
		}
		packet = new DatagramPacket(bufor, bufor.length, address, port);
		try {
			send(packet);
		} catch (IOException e) {
			System.err.println("3 " + e);
			System.exit(1);
		}
		bufor = new byte[256];
		packet = new DatagramPacket(bufor, bufor.length);
		try {
			receive(packet);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		String received = new String(packet.getData(), 0, packet.getLength());
		address = packet.getAddress();
		port = packet.getPort();
		if (send.equals("last"))
			if (!received.equals("N")) {
				System.out.println("[" + address.getHostAddress() + ":" + port + "] " + received + " (UDP message)");
				tcpPort = Integer.parseInt(received.substring(64, 68));
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				System.err.println("java.net.SocketTimeoutException: Receive timed out");
				// it is a faked timed out, just lying to knockers for not even letting them
				// know they find out some correct ports by their guessing( i.e. if they found
				// out all the correct ports, then the only thing they have to do is figure out
				// the permutation, which only cost few minutes if the sequence is only make by
				// few ports)
				System.exit(1);
			}
	}
}
