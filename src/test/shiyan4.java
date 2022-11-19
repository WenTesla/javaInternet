package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class shiyan4 {
	private int port = 9999;
	private ServerSocketChannel severSocketChannel = ServerSocketChannel.open();
	private Selector selector;
	ByteBuffer buffer;
	public int totalLength;

	public shiyan4() throws IOException {
		this.severSocketChannel.socket().setReuseAddress(true);
		this.severSocketChannel.bind(new InetSocketAddress(this.port));
		this.selector = Selector.open();
		this.severSocketChannel.configureBlocking(false);
		this.severSocketChannel.register(this.selector, 16);
		System.out.println("服务器启动成功，服务端口为：" + this.port);
	}

	public void service() {
		label40 : while (true) {
			try {
				if (this.selector.select() > 0) {
					Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
					Iterator it = selectedKeys.iterator();

					while (true) {
						while (true) {
							if (!it.hasNext()) {
								continue label40;
							}

							SelectionKey key = (SelectionKey) it.next();
							it.remove();
							if (key.isAcceptable()) {
								ServerSocketChannel severSocketChannel = (ServerSocketChannel) key.channel();
								SocketChannel socketChannel = severSocketChannel.accept();
								socketChannel.configureBlocking(false);
								Path filePath = FileSystems.getDefault().getPath("fdsdata.txt");
								byte[] data = Files.readAllBytes(filePath);
								this.totalLength = data.length;
								this.buffer = ByteBuffer.wrap(data);
								socketChannel.register(this.selector, 4, this.buffer);
								System.out.println("客户端连接成功" + socketChannel.getRemoteAddress() + ":"
										+ socketChannel.socket().getPort());
								System.out.println("等待接收长度" + this.totalLength);
							} else if (key.isWritable()) {
								SocketChannel socketChannel = (SocketChannel) key.channel();
								ByteBuffer buffer = (ByteBuffer) key.attachment();
								ByteBuffer buffer1=ByteBuffer.wrap(("待发送长度为："+totalLength).getBytes());
								socketChannel.write(buffer1);

								while (buffer.hasRemaining()) {
									socketChannel.write(buffer);
								}

								buffer.flip();
								key.cancel();
								socketChannel.close();
							}
						}
					}
				}
			} catch (IOException var9) {
				var9.printStackTrace();
			}

			return;
		}
	}

	public static void main(String[] args) throws IOException {
		(new shiyan4()).service();
	}
}