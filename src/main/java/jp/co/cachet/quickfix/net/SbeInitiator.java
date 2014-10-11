package jp.co.cachet.quickfix.net;

import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Response;

public class SbeInitiator extends SbeAcceptor {
	private final ConcurrentHashMap<Long, Response<Object>> responses = new ConcurrentHashMap<Long, Response<Object>>();

	public SbeInitiator(SocketChannel socket) throws SocketException {
		super(socket);
	}

	public void send(Car car, Response<Object> response) {
		responses.put(car.getSerialNumber(), response);
		send(car);
	}

	@Override
	public void onCar(Car car, SbeSession session) {
		final long key = car.getSerialNumber();
		if (responses.containsKey(key)) {
			responses.get(key).onResponse(car);
		}
	}

}
