package cn.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {
	
	public static void main(String[] args) {
		int port = 8080;
		MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
		new Thread(timeServer,"nio-time-001").start();
	}

	private Selector selector;

	private ServerSocketChannel servChannel;

	private volatile boolean stop;

	public MultiplexerTimeServer(int port) {
		try {
			// 打开ServerSocketChannel，用于监听客户端连接，是所有客户端连接的父管道
			servChannel = ServerSocketChannel.open();
			// 设置为非阻塞模式
			servChannel.configureBlocking(false);
			// 绑定监听端口
			servChannel.socket().bind(new InetSocketAddress(port), 1024);
			// 创建多路复用器
			selector = Selector.open();
			// channel注册到多路复用器，监听指定事件
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("the time server is start in port:" + port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		this.stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				selector.select(1000);// 休眠1s
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					handleInput(key);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			// 接受连接
			if (key.isAcceptable()) {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			// read the data
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "utf-8");
					System.out.println("server receive body:" + body);
					doWrite(sc, "send client msg");
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				} else {
					;
				}
			}
		}
	}

	private void doWrite(SocketChannel channel, String response) throws IOException {
		if (response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);

		}
	}

}
