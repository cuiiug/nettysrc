package cn.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {

	// 通道管理器
	private Selector selecctor;

	public void initServer(int port) throws IOException {
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置为非阻塞模式
		serverChannel.configureBlocking(false);
		// 将通道对应的ServerSocket绑定到port端口
		serverChannel.bind(new InetSocketAddress(port));
		// 获得一个管理器
		this.selecctor = Selector.open();
		// 将通道注册到通道管理器上，注册事件SelectionKey.OP_ACCEPT事件，当事件到达时，selector.select()会返回，如果没有事件到达，会一直阻塞
		serverChannel.register(selecctor, SelectionKey.OP_ACCEPT);
	}
	
	public void listen() throws IOException{
		System.out.println("服务端启动成功");
		while(true) {
			//当注册事件到达时，方法返回；否则改方法一直阻塞
			this.selecctor.select();
			//获得selector中选中的迭代器，选中的项为注册事件
			Iterator ite = this.selecctor.selectedKeys().iterator();
			while(ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				//删除已选的key，防止重复
				ite.remove();
				//客户端连接请求事件
				if(key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					//客户端连接的channel
					SocketChannel channel = server.accept();
					channel.configureBlocking(false);
					//对客户端发送信息
					channel.write(ByteBuffer.wrap(new String("hello client").getBytes()));
					//设置只读权限
					channel.register(this.selecctor, SelectionKey.OP_READ);
				}else if(key.isReadable()) {
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
		System.out.println("服务端接收到的消息："+msg);
		ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
		channel.write(outBuffer);
	}
	public static void main(String[] args) throws IOException {
		NIOServer server = new NIOServer();
		server.initServer(8081);
		server.listen();
	}

}
