package jp.co.cachet.quickfix.util;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class SafeDateFormatTest {

	@Test
	public void test() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SafeDateFormat safe = new SafeDateFormat();

		String lastYear = "1970";
		long ms = 0;
		for (int i = 0; i < 100000; i++) {
			ms += 86400000;
			String date = sdf.format(new Date(ms));
			long msDate = sdf.parse(date).getTime();
			// parse確認
			assertEquals(msDate, safe.parse(date));
			// format確認
			assertEquals(date, safe.format(msDate));

			String year = date.substring(0, 4);
			if (!year.equals(lastYear)) {
				System.out.printf("passed all days of %s year\n", lastYear);
				lastYear = year;
			}
		}
	}

}
