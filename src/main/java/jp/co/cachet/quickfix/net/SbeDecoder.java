package jp.co.cachet.quickfix.net;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.cachet.quickfix.entity.Acceleration;
import jp.co.cachet.quickfix.entity.FuelFigure;
import jp.co.cachet.quickfix.entity.PerformanceFigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.examples.car.BooleanType;
import uk.co.real_logic.sbe.examples.car.Car;
import uk.co.real_logic.sbe.examples.car.Engine;
import uk.co.real_logic.sbe.examples.car.MessageHeader;
import uk.co.real_logic.sbe.examples.car.Model;
import uk.co.real_logic.sbe.examples.car.OptionalExtras;

public class SbeDecoder {
	private static final Logger log = LoggerFactory.getLogger(SbeDecoder.class);
	private static final int ACTING_VERSION = 0;

	private final MessageHeader header = new MessageHeader();
	private final Car bodyCar = new Car();
	private final byte[] tempBuffer = new byte[128];

	public boolean canDecode(DirectBuffer buffer, int newPosition) {
		return (newPosition > buffer.byteBuffer().limit() || newPosition < 0) ? false : true;
	}

	public Object decode(DirectBuffer buffer) throws UnsupportedEncodingException {
		return decode(buffer, false);
	}

	public Object decode(DirectBuffer buffer, boolean rescue) {
		Object decoded = decode(buffer, buffer.byteBuffer().position(), rescue);
		log.info("position = {}", buffer.byteBuffer().position());
		return decoded;
	}

	public Object decode(DirectBuffer buffer, int bufferIndex) {
		return decode(buffer, bufferIndex, false);
	}

	public Object decode(DirectBuffer buffer, int bufferIndex, boolean rescue) {
		final int newPosition = bufferIndex + header.size();
		if (!canDecode(buffer, newPosition)) {
			return null;
		}

		header.wrap(buffer, bufferIndex, ACTING_VERSION);
		final int blockLength = header.blockLength();
		final int templateId = header.templateId();
		final int schemaId = header.schemaId();
		final int version = header.version();

		try {
			switch (templateId) {
			case Car.TEMPLATE_ID:
				if (blockLength == Car.BLOCK_LENGTH && schemaId == Car.SCHEMA_ID && version == Car.SCHEMA_VERSION) {
					return decode(buffer, bufferIndex + header.size(), bodyCar);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.warn("bufferIndex:{} templatedId:{} excetion:{}", bufferIndex, templateId, e);
		}

		if (rescue) {
			return decode(buffer, ++bufferIndex, rescue);
		}

		log.warn("Unknown templatedId:{}", templateId);
		return null;
	}

	public jp.co.cachet.quickfix.entity.Car decode(DirectBuffer buffer, int bufferIndex, Car body)
			throws UnsupportedEncodingException {
		final int newPosition = bufferIndex + Car.BLOCK_LENGTH;
		if (!canDecode(buffer, newPosition)) {
			return null;
		}

		bodyCar.wrapForDecode(buffer, bufferIndex, header.blockLength(), header.version());

		long serialNumber = bodyCar.serialNumber();
		int modelYear = bodyCar.modelYear();
		BooleanType available = bodyCar.available();
		Model code = bodyCar.code();

		int size = 0;

		size = Car.someNumbersLength();
		int[] someNumbers = new int[size];
		for (int i = 0; i < size; i++)
		{
			someNumbers[i] = bodyCar.someNumbers(i);
		}

		String vehicleCode = new String(tempBuffer, 0, bodyCar.getVehicleCode(tempBuffer, 0), "UTF-8");

		final OptionalExtras sbeExtras = bodyCar.extras();
		jp.co.cachet.quickfix.entity.OptionalExtras extras = new jp.co.cachet.quickfix.entity.OptionalExtras(
				sbeExtras.cruiseControl(),
				sbeExtras.sportsPack(),
				sbeExtras.sunRoof()
				);

		final Engine sbeEngine = bodyCar.engine();
		jp.co.cachet.quickfix.entity.Engine engine = new jp.co.cachet.quickfix.entity.Engine(
				sbeEngine.capacity(),
				sbeEngine.numCylinders(),
				sbeEngine.maxRpm(),
				new String(tempBuffer, 0, sbeEngine.getManufacturerCode(tempBuffer, 0), "UTF-8"),
				new String(tempBuffer, 0, sbeEngine.getFuel(tempBuffer, 0, tempBuffer.length), "UTF-8")
				);


		List<FuelFigure> fuelFigures = new ArrayList<FuelFigure>();
		for (final Car.FuelFigures fuelFigure : bodyCar.fuelFigures()) {
			fuelFigures.add(new FuelFigure(
					fuelFigure.speed(),
					fuelFigure.mpg()
					));
		}

		List<PerformanceFigure> performanceFigures = new ArrayList<PerformanceFigure>();
		for (final Car.PerformanceFigures performanceFigure : bodyCar.performanceFigures()) {
			List<Acceleration> accelerations = new ArrayList<Acceleration>();
			for (final Car.PerformanceFigures.Acceleration acceleration : performanceFigure.acceleration()) {
				accelerations.add(new Acceleration(
						acceleration.mph(),
						acceleration.seconds()
						));
			}
			performanceFigures.add(new PerformanceFigure(
					performanceFigure.octaneRating(),
					accelerations
					));

		}

		String make = new String(tempBuffer, 0, bodyCar.getMake(tempBuffer, 0, tempBuffer.length), "UTF-8");
		String model = new String(tempBuffer, 0, bodyCar.getModel(tempBuffer, 0, tempBuffer.length), "UTF-8");

		jp.co.cachet.quickfix.entity.Car decoded = new jp.co.cachet.quickfix.entity.Car(
				serialNumber, modelYear, available, code, someNumbers, vehicleCode, extras, engine, fuelFigures,
				performanceFigures, make, model);
		buffer.byteBuffer().position(bodyCar.limit());
		return decoded;
	}
}
