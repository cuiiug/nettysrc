package cn.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient {

	private Selector selector;

	public void initClient(String ip, int port) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		this.selector = Selector.open();
		channel.connect(new InetSocketAddress(ip, port));
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void listen() throws IOException {
		while (true) {
			this.selector.select();
			Iterator ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				ite.remove();
				if (key.isConnectable()) {
					SocketChannel channel = (SocketChannel) key.channel();
					if (channel.isConnectionPending()) {
						channel.finishConnect();
					}
					channel.configureBlocking(false);
					channel.write(ByteBuffer.wrap(new String("send to server").getBytes()));
					channel.register(this.selector, SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}

	public void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buff = ByteBuffer.allocate(1024);
		channel.read(buff);
		byte[] data = buff.array();
		String msg = new String(data).trim();
		System.out.println("服务端接收到的消息：" + msg);
		ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
		channel.write(outBuffer);
	}
	
	public static void main(String[] args) throws IOException {
		NIOClient client = new NIOClient();
		client.initClient("127.0.0.1", 8081);
		client.listen();
	}
}
