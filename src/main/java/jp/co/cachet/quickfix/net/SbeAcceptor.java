package jp.co.cachet.quickfix.net;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;

public class SbeAcceptor implements Runnable, Response<Object> {
	private static final Logger log = LoggerFactory.getLogger(SbeAcceptor.class);

	private final SocketChannel socket;
	private final SbeApplication application;

	private final DirectBuffer decodeBuffer;
	private final DirectBuffer badBuffer;
	private final DirectBuffer encodeBuffer = new DirectBuffer(ByteBuffer.allocate(128).order(ByteOrder.nativeOrder()));
	private final SbeDecoder sbeDecoder = new SbeDecoder();
	private final SbeEncoder sbeEncoder = new SbeEncoder();

	public SbeAcceptor(SocketChannel socket, SbeApplication application) throws SocketException {
		this.socket = socket;
		this.application = application;
		decodeBuffer = new DirectBuffer(ByteBuffer.allocate(socket.socket().getReceiveBufferSize() * 10)
				.order(ByteOrder.nativeOrder()));
		badBuffer = new DirectBuffer(ByteBuffer.allocate(decodeBuffer.capacity()).order(ByteOrder.nativeOrder()));
	}

	@Override
	public void run() {
		int remaining = 0;
		decodeBuffer.byteBuffer().clear();
		try {
			while (socket.read(decodeBuffer.byteBuffer()) > 0) {
				decodeBuffer.byteBuffer().flip();
				final int limit = decodeBuffer.byteBuffer().limit();

				Object decoded = null;
				if (remaining > 0) {
					decodeBuffer.getBytes(0, badBuffer, remaining, limit);
					while ((decoded = sbeDecoder.decode(badBuffer, true)) != null) {
						application.onCar((Car) decoded, this);
					}
				} else {
					while ((decoded = sbeDecoder.decode(decodeBuffer)) != null) {
						application.onCar((Car) decoded, this);
					}
				}

				final int position = decodeBuffer.byteBuffer().position();
				remaining = limit - position;
				if (remaining > 0) {
					log.warn("remain={}, limit={}, position={}", remaining, limit, position);
					decodeBuffer.getBytes(position, badBuffer, 0, remaining);
				}
				decodeBuffer.byteBuffer().clear();
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void onResponse(Object arg) {
		try {
			if (arg instanceof Car) {
				Car car = (Car) arg;
				encodeBuffer.byteBuffer().clear();
				sbeEncoder.encode(car, encodeBuffer);
				encodeBuffer.byteBuffer().flip();
				while (encodeBuffer.byteBuffer().hasRemaining()) {
					socket.write(encodeBuffer.byteBuffer());
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}

	}

}
