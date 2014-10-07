package jp.co.cachet.quickfix.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;

public abstract class SbeAcceptor implements Runnable, Closeable, Response<Object>, SbeApplication {
	private static final Logger log = LoggerFactory.getLogger(SbeAcceptor.class);

	private final SocketChannel socket;

	private final DirectBuffer decodeBuffer;
	private final DirectBuffer encodeBuffer = new DirectBuffer(ByteBuffer.allocate(128).order(ByteOrder.nativeOrder()));
	private final SbeDecoder sbeDecoder = new SbeDecoder();
	private final SbeEncoder sbeEncoder = new SbeEncoder();

	public SbeAcceptor(SocketChannel socket) throws SocketException {
		this.socket = socket;
		decodeBuffer = new DirectBuffer(ByteBuffer.allocate(socket.socket().getReceiveBufferSize() * 10)
				.order(ByteOrder.nativeOrder()));
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
				while ((decoded = sbeDecoder.decode(decodeBuffer, true)) != null) {
					onCar((Car) decoded, this);
				}

				final int position = decodeBuffer.byteBuffer().position();
				remaining = limit - position;
				decodeBuffer.byteBuffer().clear();
				if (remaining > 0) {
					log.warn("remain={}, limit={}, position={}", remaining, limit, position);
					decodeBuffer.byteBuffer().position(remaining);
				}
			}
		} catch (AsynchronousCloseException ignored) {
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void send(Object message) {
		try {
			if (message instanceof Car) {
				Car car = (Car) message;
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

	@Override
	public void onResponse(Object message) {
		send(message);
	}

	@Override
	public void close() throws IOException {
		if (socket != null && socket.isOpen()) {
			socket.close();
		}
	}

}
