package jp.co.cachet.quickfix.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SbeInitiatorService {
	private static final Logger log = LoggerFactory.getLogger(SbeInitiatorService.class);

	private final InetSocketAddress socketAddress;
	private final ExecutorService executorService;
	private final Factory<SbeAcceptor, SocketChannel> factory;
	private final SbeAcceptor[] initiators;

	private final AtomicInteger counter = new AtomicInteger(0);

	public SbeInitiatorService(String address, int port, ExecutorService executorService, int capacity,
			Factory<SbeAcceptor, SocketChannel> factory) {
		this.socketAddress = new InetSocketAddress(address, port);
		this.executorService = executorService;
		this.factory = factory;
		this.initiators = new SbeAcceptor[capacity];
	}

	public void start() {
		try {
			for (int i = 0; i < initiators.length; i++) {
				SocketChannel socket = SocketChannel.open(socketAddress);
				SbeAcceptor initiator = factory.getInstance(socket);
				executorService.submit(initiator);
				initiators[i] = initiator;
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void stop() {
		for (SbeAcceptor initiator : initiators) {
			try {
				initiator.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	public void send(Object message) {
		SbeAcceptor initiator = initiators[counter.getAndIncrement() % initiators.length];
		if (message instanceof Car) {
			Car car = (Car) message;
			initiator.send(car);
		}
	}

}
