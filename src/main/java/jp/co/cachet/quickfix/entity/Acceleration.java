package jp.co.cachet.quickfix.entity;

public class Acceleration {
	private int mph;
	private float seconds;

	public Acceleration(int mph, float seconds) {
		this.mph = mph;
		this.seconds = seconds;
	}

	public int getMph() {
		return mph;
	}

	public float getSeconds() {
		return seconds;
	}

}
