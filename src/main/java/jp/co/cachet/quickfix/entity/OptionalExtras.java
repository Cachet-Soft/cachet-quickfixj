package jp.co.cachet.quickfix.entity;

public class OptionalExtras {
	private boolean sunRoof;
	private boolean sportsPack;
	private boolean cruiseControl;

	public OptionalExtras(boolean sunRoof, boolean sportsPack, boolean cruiseControl) {
		this.sunRoof = sunRoof;
		this.sportsPack = sportsPack;
		this.cruiseControl = cruiseControl;
	}

	public boolean isSunRoof() {
		return sunRoof;
	}

	public boolean isSportsPack() {
		return sportsPack;
	}

	public boolean isCruiseControl() {
		return cruiseControl;
	}

}
