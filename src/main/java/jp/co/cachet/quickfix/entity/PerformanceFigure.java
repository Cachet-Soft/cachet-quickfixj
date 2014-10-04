package jp.co.cachet.quickfix.entity;

import java.util.List;

public class PerformanceFigure {
	private short octaneRating;
	private List<Acceleration> accelerations;

	public PerformanceFigure(short octaneRating, List<Acceleration> accelerations) {
		this.octaneRating = octaneRating;
		this.accelerations = accelerations;
	}

	public short getOctaneRating() {
		return octaneRating;
	}

	public List<Acceleration> getAccelerations() {
		return accelerations;
	}

}
