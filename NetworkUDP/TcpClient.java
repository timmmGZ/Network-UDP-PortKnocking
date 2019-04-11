import java.io.*;
import java.net.*;

public class TcpClient extends Socket {
	PrintWriter out = null;
	static BufferedReader send = new BufferedReader(new InputStreamReader(System.in));

	public TcpClient(String ip, int port) throws UnknownHostException, IOException {
		super(ip, port);
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		TcpClient client = new TcpClient(args[0], Integer.parseInt(args[1]));
		client.start();
		client.close();
	}

	public void start() throws IOException {
		try {
			out = new PrintWriter(new OutputStreamWriter(getOutputStream(), "UTF-8"), true);
			new Thread(new ServerListener()).start();
			String msg;
			while ((msg = send.readLine()) != null) {
				out.println(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.println("DISCONNECT");
			out.close();
			send.close();
			close();
		}
	}

	class ServerListener implements Runnable {
		BufferedReader inn = null;

		@Override
		public void run() {
			try {
				inn = new BufferedReader(new InputStreamReader(getInputStream(), "UTF-8"));
				String msg;
				while ((msg = inn.readLine()) != null) {
					System.out.println(msg + " (TCP message)");
				}
			} catch (IOException e) {
			} finally {
				if (inn != null)
					try {
						inn.close();
					} catch (IOException e) {
					}
			}
		}
	}

}
