package jp.co.cachet.quickfix.net;

import java.io.UnsupportedEncodingException;

import jp.co.cachet.quickfix.entity.Acceleration;
import jp.co.cachet.quickfix.entity.FuelFigure;
import jp.co.cachet.quickfix.entity.PerformanceFigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.examples.car.Car;
import uk.co.real_logic.sbe.examples.car.MessageHeader;

public class SbeEncoder {
	public static final int ACTING_VERSION = 3;

	private static final Logger log = LoggerFactory.getLogger(SbeEncoder.class);

	private final MessageHeader header = new MessageHeader();
	private final Car bodyCar = new Car();

	public void encode(jp.co.cachet.quickfix.entity.Car car, DirectBuffer buffer)
			throws UnsupportedEncodingException {
		final int position = buffer.byteBuffer().position();
		encode(car, buffer, position);
	}

	public void encode(jp.co.cachet.quickfix.entity.Car car, DirectBuffer buffer, int bufferIndex)
			throws UnsupportedEncodingException {
		header.wrap(buffer, bufferIndex, ACTING_VERSION)
				.blockLength(Car.BLOCK_LENGTH)
				.templateId(Car.TEMPLATE_ID)
				.schemaId(Car.SCHEMA_ID)
				.version(Car.SCHEMA_VERSION);

		bodyCar.wrapForEncode(buffer, bufferIndex + header.size())
				.code(car.getCode())
				.modelYear(car.getModelYear())
				.serialNumber(car.getSerialNumber())
				.available(car.getAvailable())
				.putVehicleCode(car.getVehicleCode().getBytes("UTF-8"), 0);

		for (int i = 0, size = Car.someNumbersLength(); i < size; i++) {
			bodyCar.someNumbers(i, car.getSomeNumbers()[i]);
		}

		bodyCar.extras().clear()
				.sportsPack(car.getExtras().isSportsPack())
				.sunRoof(car.getExtras().isSunRoof());

		bodyCar.engine().capacity(car.getEngine().getCapacity())
				.numCylinders(car.getEngine().getNumCylinders())
				.putManufacturerCode(car.getEngine().getManufactureCode().getBytes("UTF-8"), 0);

		final Car.FuelFigures fuelFigures = bodyCar.fuelFiguresCount(car.getFuelFigures().size());
		for (FuelFigure fuelFigure : car.getFuelFigures()) {
			fuelFigures.next().speed(fuelFigure.getSpeed()).mpg(fuelFigure.getMpg());
		}

		final Car.PerformanceFigures perfFigures = bodyCar.performanceFiguresCount(car.getPerformanceFigures().size());
		for (PerformanceFigure perfFigure : car.getPerformanceFigures()) {
			final Car.PerformanceFigures.Acceleration accelerations = perfFigures.next()
					.octaneRating(perfFigure.getOctaneRating()).accelerationCount(perfFigure.getAccelerations().size());
			for (Acceleration acceleration : perfFigure.getAccelerations()) {
				accelerations.next().mph(acceleration.getMph()).seconds(acceleration.getSeconds());
			}
		}

		byte[] make = car.getMake().getBytes("UTF-8");
		bodyCar.putMake(make, 0, make.length);

		byte[] model = car.getModel().getBytes("UTF-8");
		bodyCar.putModel(model, 0, model.length);

		buffer.byteBuffer().position(bodyCar.limit());
		log.info("position = {}", buffer.byteBuffer().position());
	}
}
