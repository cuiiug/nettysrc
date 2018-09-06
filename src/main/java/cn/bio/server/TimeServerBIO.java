package cn.bio.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServerBIO {

	public static void main(String[] args) throws IOException {
		int port = 8080;
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start in port:" + port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				new Thread(new TimeServerHandllerBIO(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("The time server close");
			if (server != null) {
				server.close();
				server = null;
			}
		}
	}
}
