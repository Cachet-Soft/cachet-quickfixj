package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;

import org.junit.Test;

public class SafeDecimalFormatTest {

	@Test
	public void test() {
		SafeDecimalFormat sdf = SafeDecimalFormat.getInstance("0.000000");
		DecimalFormat df = new DecimalFormat("0.000000");
		for (double d : new double[] { -0.000001, 0.000009, -0.000010, 0.100000, 1.100000, 10.100000 }) {
			assertEquals(df.format(d), sdf.format(d));
		}
	}

}
