package jp.co.cachet.net;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.entity.Engine;
import jp.co.cachet.quickfix.entity.FuelFigure;
import jp.co.cachet.quickfix.entity.OptionalExtras;
import jp.co.cachet.quickfix.entity.PerformanceFigure;
import jp.co.cachet.quickfix.net.SbeAcceptor;
import jp.co.cachet.quickfix.net.SbeAcceptorService;
import jp.co.cachet.quickfix.net.SbeApplication;
import jp.co.cachet.quickfix.net.SbeEncoder;
import jp.co.cachet.quickfix.util.Factory;
import jp.co.cachet.quickfix.util.Response;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.examples.car.BooleanType;
import uk.co.real_logic.sbe.examples.car.Model;

public class SbeAcceptorServiceTest {
	private static final int PORT = 9999;
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(100);
	private static final int MAX = 1000;

	private SbeAcceptorService acceptorService;
	private AtomicInteger counter = new AtomicInteger(0);

	@Before
	public void setUp() {
		DOMConfigurator.configure("src/test/resources/log4j_warn.xml");
		
		acceptorService = new SbeAcceptorService(PORT, EXECUTOR_SERVICE,
				new Factory<SbeAcceptor, SocketChannel>() {

					@Override
					public SbeAcceptor getInstance(SocketChannel arg) {
						try {
							return new SbeAcceptorImpl(arg);
						} catch (SocketException e) {
							throw new IllegalStateException(e);
						}
					}

		});
		acceptorService.start();
	}

	@After
	public void tearDown() {
		acceptorService.stop();
	}

	@Test
	public void test() throws Exception {
		SbeEncoder sbeEncoder = new SbeEncoder();
		DirectBuffer buffer = new DirectBuffer(ByteBuffer.allocate(130).order(ByteOrder.nativeOrder()));
		SocketChannel socket = SocketChannel.open(new InetSocketAddress("localhost", PORT));

		final long start = System.currentTimeMillis();
		for (int i = 0; i < MAX; i++) {
			Car car = getInstance(i);
			buffer.byteBuffer().clear();
			sbeEncoder.encode(car, buffer);
			buffer.byteBuffer().flip();

			while (buffer.byteBuffer().hasRemaining()) {
				socket.write(buffer.byteBuffer());
			}
		}

		final long end = System.currentTimeMillis();
		final long elapsed = end - start;
		while (MAX > counter.get() && (System.currentTimeMillis() - end) < 1000) {
			LockSupport.parkNanos(1);
		}

		System.out.printf("count=%d, elapsed=%d ms, throuput=%.1f /s, latency=%.1f us%n",
				counter.get(), elapsed, counter.get() * 1000D / elapsed, elapsed * 1000D / counter.get());
		assertEquals(MAX, counter.get());
	}

	private Car getInstance(int i) {
		List<FuelFigure> fuelFigures = new ArrayList<FuelFigure>();
		List<PerformanceFigure> performanceFigures = new ArrayList<PerformanceFigure>();
		return new Car(i, 2014, BooleanType.YES, Model.A,
				new int[] { 1, 2, 3, 4, 5 }, "abcdef",
				new OptionalExtras(true, true, false),
				new Engine(25, (short) 6, 9000, "BMW", "Petrol"),
				fuelFigures, performanceFigures,
				"BMW", "323i");
	}

	class SbeAcceptorImpl extends SbeAcceptor {

		public SbeAcceptorImpl(SocketChannel socket) throws SocketException {
			this(socket, new SbeApplication() {

				@Override
				public void onCar(Car car, Response<Object> response) {
					counter.incrementAndGet();
				}

			});
		}

		public SbeAcceptorImpl(SocketChannel socket, SbeApplication application) throws SocketException {
			super(socket, application);
		}

	}
}
