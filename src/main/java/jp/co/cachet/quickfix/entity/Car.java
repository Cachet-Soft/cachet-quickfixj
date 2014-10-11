package jp.co.cachet.quickfix.entity;

import java.util.List;

import uk.co.real_logic.sbe.examples.car.BooleanType;
import uk.co.real_logic.sbe.examples.car.Model;

/**
 * Carエンティティクラス。
 * 
 * 
 * @author masaaki
 * 
 *         <pre>
 * {@code
 * <message name="Car" id="1" description="Description of a basic Car">
 * 		 <field name="serialNumber" id="1" type="uint32"/>
 * 		 <field name="modelYear" id="2" type="ModelYear"/>
 * 		 <field name="available" id="3" type="BooleanType"/>
 * 		 <field name="code" id="4" type="Model"/>
 * 		 <field name="someNumbers" id="5" type="someNumbers"/>
 * 		 <field name="vehicleCode" id="6" type="VehicleCode"/>
 * 		 <field name="extras" id="7" type="OptionalExtras"/>
 * 		 <field name="engine" id="8" type="Engine"/>
 * 		 <group name="fuelFigures" id="9" dimensionType="groupSizeEncoding">
 * 			 <field name="speed" id="10" type="uint16"/>
 * 			 <field name="mpg" id="11" type="float"/>
 * 		 </group>
 * 		 <group name="performanceFigures" id="12"
 * 			 dimensionType="groupSizeEncoding">
 * 			 <field name="octaneRating" id="13" type="uint8"/>
 * 			 <group name="acceleration" id="14" dimensionType="groupSizeEncoding">
 * 				 <field name="mph" id="15" type="uint16"/>
 * 				 <field name="seconds" id="16" type="float"/>
 * 			 </group>
 * 		 </group>
 * 		 <data name="make" id="17" type="varDataEncoding"/>
 * 		 <data name="model" id="18" type="varDataEncoding"/>
 * </message>
 * }
 * </pre>
 */
public class Car {

	private long serialNumber;
	private int modelYear;
	private BooleanType available;
	private Model code;
	private int[] someNumbers;
	private String vehicleCode;
	private OptionalExtras extras;
	private Engine engine;
	private List<FuelFigure> fuelFigures;
	private List<PerformanceFigure> performanceFigures;
	private String make;
	private String model;

	public Car(long serialNumber, int modelYear, BooleanType available, Model code, int[] someNumbers,
			String vehicleCode, OptionalExtras extras, Engine engine, List<FuelFigure> fuelFigures,
			List<PerformanceFigure> performanceFigures, String make, String model) {
		this.serialNumber = serialNumber;
		this.modelYear = modelYear;
		this.available = available;
		this.code = code;
		this.someNumbers = someNumbers;
		this.vehicleCode = vehicleCode;
		this.extras = extras;
		this.engine = engine;
		this.fuelFigures = fuelFigures;
		this.performanceFigures = performanceFigures;
		this.make = make;
		this.model = model;
	}

	public long getSerialNumber() {
		return serialNumber;
	}

	public int getModelYear() {
		return modelYear;
	}

	public BooleanType getAvailable() {
		return available;
	}

	public Model getCode() {
		return code;
	}

	public int[] getSomeNumbers() {
		return someNumbers;
	}

	public String getVehicleCode() {
		return vehicleCode;
	}

	public OptionalExtras getExtras() {
		return extras;
	}

	public Engine getEngine() {
		return engine;
	}

	public List<FuelFigure> getFuelFigures() {
		return fuelFigures;
	}

	public List<PerformanceFigure> getPerformanceFigures() {
		return performanceFigures;
	}

	public String getMake() {
		return make;
	}

	public String getModel() {
		return model;
	}

}
