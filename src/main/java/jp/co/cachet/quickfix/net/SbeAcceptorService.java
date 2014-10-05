package jp.co.cachet.quickfix.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import jp.co.cachet.quickfix.util.Factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SbeAcceptorService implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(SbeAcceptorService.class);

	private final int port;
	private final ExecutorService executorService;
	private final Factory<SbeAcceptor, SocketChannel> factory;
	private ServerSocketChannel serverSocket = null;

	public SbeAcceptorService(int port, ExecutorService executorService, Factory<SbeAcceptor, SocketChannel> factory) {
		this.port = port;
		this.executorService = executorService;
		this.factory = factory;
	}

	public void start() {
		executorService.submit(this);
	}

	public void stop() {
		if (serverSocket != null && serverSocket.isOpen()) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void run() {
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.socket().bind(new InetSocketAddress(port));
			while (serverSocket.isOpen()) {
				try {
					SocketChannel socket = serverSocket.accept();
					SbeAcceptor acceptor = factory.getInstance(socket);
					executorService.submit(acceptor);
				} catch (AsynchronousCloseException ignored) {
				} catch (IOException e) {
					log.error("", e);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (serverSocket != null && serverSocket.isOpen()) {
				try {
					serverSocket.close();
					serverSocket = null;
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}

}
