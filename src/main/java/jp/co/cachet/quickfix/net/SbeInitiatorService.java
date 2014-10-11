package jp.co.cachet.quickfix.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Factory;
import jp.co.cachet.quickfix.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SbeInitiatorService {
	private static final Logger log = LoggerFactory.getLogger(SbeInitiatorService.class);

	private final InetSocketAddress socketAddress;
	private final ExecutorService executorService;
	private final Factory<SbeInitiator, SocketChannel> factory;
	private final SbeInitiator[] initiators;

	private final AtomicInteger counter = new AtomicInteger(0);

	public SbeInitiatorService(String address, int port, ExecutorService executorService, int capacity,
			Factory<SbeInitiator, SocketChannel> factory) {
		this.socketAddress = new InetSocketAddress(address, port);
		this.executorService = executorService;
		this.factory = factory;
		this.initiators = new SbeInitiator[capacity];
	}

	public void start() {
		try {
			for (int i = 0; i < initiators.length; i++) {
				SocketChannel socket = SocketChannel.open(socketAddress);
				SbeInitiator initiator = factory.getInstance(socket);
				executorService.submit(initiator);
				initiators[i] = initiator;
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void stop() {
		for (SbeInitiator initiator : initiators) {
			try {
				initiator.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	public void send(Object message, Response<Object> response) {
		SbeInitiator initiator = initiators[counter.getAndIncrement() % initiators.length];
		if (message instanceof Car) {
			Car car = (Car) message;
			initiator.send(car, response);
		}
	}

}
