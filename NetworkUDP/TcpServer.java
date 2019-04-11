import java.io.*;
import java.net.*;

public class TcpServer extends ServerSocket {
	public static final String TCP_SERVER_NOTIFICATION = "###TCP SERVER NOTIFICATION### ";
	int count = 0;
	int port;

	public TcpServer(int p) throws IOException {
		super(p);
		port = p;
		System.out.println(TCP_SERVER_NOTIFICATION + "TCP Port: " + port + " has been opened.");

	}

	public void start() throws SocketException {
		new Thread(() -> {
			try {
				Thread.sleep(10000);
				if (count == 0) {// count is number of current connected clients
					this.close();
					System.out.println(
							"None one connect to this tcp port: " + port + " in 10 seconds, so the port closes");
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {
				System.err.println(e);
			}
		}).start();// in case some client opens TCP port and don't connect to it but just leave, so
					// TcpServer won't know if the client is going to connect, so we give it 10
					// seconds limit, then even if client leave, the port will eventually close too
		try {
			while (true) {
				new Thread(new ListenrClient(accept(), this)).start();
				count++;
				System.out.println(
						TCP_SERVER_NOTIFICATION + "TCP Port: " + port + " has " + count + " clients connecting now.");
			}
		} catch (Exception e) {
		}

	}

	class ListenrClient implements Runnable {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
		int cPort;
		String IP;
		TcpServer tcpServer;

		public ListenrClient(Socket s, TcpServer t) {
			socket = s;
			cPort = s.getPort();
			IP = s.getInetAddress().getHostAddress();
			tcpServer = t;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
				String msg = null;
				out.println(TCP_SERVER_NOTIFICATION + " You Connect to TcpServer");
				while ((msg = in.readLine()) != null) {
					if (msg.equals("DISCONNECT")) {
						System.out.println(
								TCP_SERVER_NOTIFICATION + "Client " + this + " disconnects from port: " + port);
						if (--count == 0) {
							tcpServer.close();
							System.out.println(TCP_SERVER_NOTIFICATION + "TCP Port: " + port
									+ " has been closed since no one connecting now.");
						}
					} else {
						System.out.println(this + ": " + msg + "  (TCP message)");
						out.println("Server: get your message");
					}
				}
			} catch (IOException e) {
			} finally {
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e) {
				}
			}
		}

		public String toString() {
			return "[" + IP + ":" + cPort + "]";
		}

	}

}
