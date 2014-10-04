package jp.co.cachet.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.entity.Engine;
import jp.co.cachet.quickfix.entity.FuelFigure;
import jp.co.cachet.quickfix.entity.OptionalExtras;
import jp.co.cachet.quickfix.entity.PerformanceFigure;
import jp.co.cachet.quickfix.net.SbeDecoder;
import jp.co.cachet.quickfix.net.SbeEncoder;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.examples.car.BooleanType;
import uk.co.real_logic.sbe.examples.car.Model;

public class SbeDecoderEncoderTest {
	private SbeDecoder sbeDecoder = new SbeDecoder();
	private SbeEncoder sbeEncoder = new SbeEncoder();

	@Before
	public void setUp() {
		DOMConfigurator.configure("src/test/resources/log4j.xml");
	}

	@Test
	public void test() throws UnsupportedEncodingException {
		List<FuelFigure> fuelFigures = new ArrayList<FuelFigure>();
		List<PerformanceFigure> performanceFigures = new ArrayList<PerformanceFigure>();
		Car car = new Car(123456789, 2014, BooleanType.YES, Model.A,
				new int[] { 1, 2, 3, 4, 5 }, "abcdef",
				new OptionalExtras(true, true, false),
				new Engine(25, (short) 6, 9000, "BMW", "Petrol"),
				fuelFigures, performanceFigures,
				"BMW", "323i");

		DirectBuffer buffer = new DirectBuffer(ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder()));

		sbeEncoder.encode(car, buffer, buffer.byteBuffer().position());
		Car decoded = (Car) sbeDecoder.decode(buffer, buffer.byteBuffer().position());

		doAssertEquals(car, decoded);
	}

	@Test
	public void testMulti() throws UnsupportedEncodingException {
		List<FuelFigure> fuelFigures = new ArrayList<FuelFigure>();
		List<PerformanceFigure> performanceFigures = new ArrayList<PerformanceFigure>();
		Car car0 = new Car(123456789, 2014, BooleanType.YES, Model.A,
				new int[] { 1, 2, 3, 4, 5 }, "abcdef",
				new OptionalExtras(true, true, false),
				new Engine(25, (short) 6, 9000, "BMW", "Petrol"),
				fuelFigures, performanceFigures,
				"BMW", "323i");
		Car car1 = new Car(123456790, 2015, BooleanType.NO, Model.B,
				new int[] { 6, 7, 8, 9, 10 }, "ghijkl",
				new OptionalExtras(true, true, true),
				new Engine(20, (short) 4, 8000, "ADI", "Petrol"),
				fuelFigures, performanceFigures,
				"Audi", "A4");

		DirectBuffer buffer = new DirectBuffer(ByteBuffer.allocate(130).order(ByteOrder.nativeOrder()));

		sbeEncoder.encode(car0, buffer);
		sbeEncoder.encode(car1, buffer);
		try {
			sbeEncoder.encode(car1, buffer);
			fail();
		} catch (IllegalArgumentException okay) {
		} catch (IndexOutOfBoundsException okay) {
		}

		buffer.byteBuffer().clear();
		Car decoded0 = (Car) sbeDecoder.decode(buffer);
		Car decoded1 = (Car) sbeDecoder.decode(buffer);
		Car decoded2 = (Car) sbeDecoder.decode(buffer);

		doAssertEquals(car0, decoded0);
		doAssertEquals(car1, decoded1);
		assertNull(decoded2);
	}

	@Test
	public void testRescue() throws UnsupportedEncodingException {
		List<FuelFigure> fuelFigures = new ArrayList<FuelFigure>();
		List<PerformanceFigure> performanceFigures = new ArrayList<PerformanceFigure>();
		Car car0 = new Car(123456789, 2014, BooleanType.YES, Model.A,
				new int[] { 1, 2, 3, 4, 5 }, "abcdef",
				new OptionalExtras(true, true, false),
				new Engine(25, (short) 6, 9000, "BMW", "Petrol"),
				fuelFigures, performanceFigures,
				"BMW", "323i");
		Car car1 = new Car(123456790, 2015, BooleanType.NO, Model.B,
				new int[] { 6, 7, 8, 9, 10 }, "ghijkl",
				new OptionalExtras(true, true, true),
				new Engine(20, (short) 4, 8000, "ADI", "Petrol"),
				fuelFigures, performanceFigures,
				"Audi", "A4");

		DirectBuffer buffer = new DirectBuffer(ByteBuffer.allocate(130).order(ByteOrder.nativeOrder()));
		DirectBuffer badBuffer = new DirectBuffer(ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder()));

		sbeEncoder.encode(car0, buffer);
		sbeEncoder.encode(car1, buffer);
		try {
			sbeEncoder.encode(car1, buffer);
			fail();
		} catch (IllegalArgumentException okay) {
		} catch (IndexOutOfBoundsException okay) {
		}

		buffer.getBytes(0, badBuffer, 7, buffer.capacity());
		badBuffer.byteBuffer().clear();
		Car decoded0 = (Car) sbeDecoder.decode(badBuffer, true);
		Car decoded1 = (Car) sbeDecoder.decode(badBuffer, true);
		Car decoded2 = (Car) sbeDecoder.decode(badBuffer, true);

		doAssertEquals(car0, decoded0);
		doAssertEquals(car1, decoded1);
		assertNull(decoded2);
	}

	private void doAssertEquals(Car expected, Car actual) {
		assertEquals(expected.getSerialNumber(), actual.getSerialNumber());
		assertEquals(expected.getMake(), actual.getMake());
	}

}
