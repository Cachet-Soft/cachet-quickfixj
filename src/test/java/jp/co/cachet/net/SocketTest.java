package jp.co.cachet.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketTest {
	private static final Logger log = LoggerFactory.getLogger(SocketTest.class);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private ServerSocketChannel serverChannel = null;

	@Before
	public void setUp() {
		DOMConfigurator.configure("src/test/resources/log4j.xml");

		executorService.submit(new Runnable() {

			public void run() {
				ByteBuffer buf = ByteBuffer.allocate(256).order(ByteOrder.nativeOrder());
				try {
					serverChannel = ServerSocketChannel.open();
					serverChannel.socket().bind(new InetSocketAddress(9999));
					while (serverChannel.isOpen()) {
						SocketChannel channel = serverChannel.accept();
						while (true) {
							buf.clear();
							if (channel.read(buf) < 0) {
								break;
							}
							buf.flip();
							channel.write(buf);
						}
					}
				} catch (Exception e) {
					log.error("", e);
				} finally {
					if (serverChannel != null && serverChannel.isOpen()) {
						try {
							serverChannel.close();
						} catch (IOException e) {
							log.error("", e);
						}
					}
				}
			}

		});
	}

	@After
	public void tearDown() {
		if (serverChannel != null && serverChannel.isOpen()) {
			try {
				serverChannel.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Test
	public void test() throws Exception {
		while (serverChannel == null || !serverChannel.isOpen()) {
			LockSupport.parkNanos(1);
		}

		char[] result = null;
		ByteBuffer buf = ByteBuffer.allocate(256).order(ByteOrder.nativeOrder());
		SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9999));
		for (int i = 0; i < 10000; i++) {
			buf.clear();
			buf.put(("test" + i).getBytes());
			buf.flip();
			channel.write(buf);

			buf.clear();
			if (channel.read(buf) < 0) {
				break;
			}
			buf.flip();
			result = new char[buf.limit()];
			for (int j = 0; j < buf.limit(); j++) {
				result[j] = (char) buf.get();
			}
		}
		channel.close();
		log.info(new StringBuilder("result = ").append(result).toString());
	}

}
