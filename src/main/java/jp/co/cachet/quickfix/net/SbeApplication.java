package jp.co.cachet.quickfix.net;

import jp.co.cachet.quickfix.entity.Car;

public interface SbeApplication {
	void onCar(Car car, SbeSession session);
}
