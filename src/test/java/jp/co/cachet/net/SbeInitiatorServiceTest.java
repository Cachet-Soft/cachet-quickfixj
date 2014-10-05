package jp.co.cachet.net;

import static org.junit.Assert.assertEquals;

import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.entity.Engine;
import jp.co.cachet.quickfix.entity.FuelFigure;
import jp.co.cachet.quickfix.entity.OptionalExtras;
import jp.co.cachet.quickfix.entity.PerformanceFigure;
import jp.co.cachet.quickfix.net.SbeAcceptor;
import jp.co.cachet.quickfix.net.SbeAcceptorService;
import jp.co.cachet.quickfix.net.SbeInitiatorService;
import jp.co.cachet.quickfix.util.Factory;
import jp.co.cachet.quickfix.util.Response;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.real_logic.sbe.examples.car.BooleanType;
import uk.co.real_logic.sbe.examples.car.Model;

public class SbeInitiatorServiceTest {
	private static final int PORT = 9999;
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(100);
	private static final int MAX = 1000000;
	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

	private SbeAcceptorService acceptorService;
	private SbeInitiatorService initiatorService;
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

		initiatorService = new SbeInitiatorService("localhost", PORT, EXECUTOR_SERVICE, 1,
				new Factory<SbeAcceptor, SocketChannel>() {

					@Override
					public SbeAcceptor getInstance(SocketChannel arg) {
						try {
							return new SbeAcceptor(arg) {
								@Override
								public void onCar(Car car, Response<Object> response) {
									counter.incrementAndGet();
								}
							};
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}

				});
		initiatorService.start();
	}

	@After
	public void tearDown() {
		initiatorService.stop();
		acceptorService.stop();
	}

	@Test
	public void test() throws Exception {
		final long start = System.currentTimeMillis();
		for (int i = 0; i < MAX; i++) {
			Car car = getInstance(i);
			initiatorService.send(car);
		}

		final long end = System.currentTimeMillis();
		final long elapsed = end - start;
		while (MAX > counter.get() && (System.currentTimeMillis() - end) < 10000) {
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
			super(socket);
		}

		@Override
		public void onCar(Car car, Response<Object> response) {
			SCHEDULER.execute(new SbeActor(car, response));
		}
	}
	
	class SbeActor implements Runnable {
		private final Car car;
		private final Response<Object> response;
		
		public SbeActor(Car car, Response<Object> response) {
			this.car = car;
			this.response = response;
		}

		@Override
		public void run() {
				response.onResponse(car);
		}
	}
		
}
