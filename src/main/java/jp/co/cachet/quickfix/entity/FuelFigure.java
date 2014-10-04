package jp.co.cachet.quickfix.entity;

public class FuelFigure {
	private int speed;
	private float mpg;

	public FuelFigure(int speed, float mpg) {
		this.speed = speed;
		this.mpg = mpg;
	}

	public int getSpeed() {
		return speed;
	}

	public float getMpg() {
		return mpg;
	}

}
