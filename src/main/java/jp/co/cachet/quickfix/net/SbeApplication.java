package jp.co.cachet.quickfix.net;

import jp.co.cachet.quickfix.entity.Car;
import jp.co.cachet.quickfix.util.Response;

public interface SbeApplication {
	void onCar(Car car, Response<Object> response);
}
