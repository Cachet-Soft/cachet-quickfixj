package jp.co.cachet.quickfix.entity;

public class Engine {
	private int capacity;
	private short numCylinders;
	private int maxRpm;
	private String manufactureCode;
	private String fuel;

	/**
	 * コンストラクタ。
	 * 
	 * @param capacity
	 * @param numCylinders
	 * @param maxRpm
	 * @param manufactureCode
	 * @param fuel
	 */
	public Engine(int capacity, short numCylinders, int maxRpm, String manufactureCode, String fuel) {
		this.capacity = capacity;
		this.numCylinders = numCylinders;
		this.maxRpm = maxRpm;
		this.manufactureCode = manufactureCode;
		this.fuel = fuel;
	}

	public int getCapacity() {
		return capacity;
	}

	public short getNumCylinders() {
		return numCylinders;
	}

	public int getMaxRpm() {
		return maxRpm;
	}

	public String getManufactureCode() {
		return manufactureCode;
	}

	public String getFuel() {
		return fuel;
	}

}
