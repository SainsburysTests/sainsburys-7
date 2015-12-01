package hybristest.scrape.util;



import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for converting bytes to more readable format.
 * There was going to other methods in here but I didn't get that far.
 * 
 * @author Mike
 *
 */
public class ScrapeUtils {

	// Constants for calculations
	private static final BigDecimal KILO = BigDecimal.valueOf(1024);
	private static final BigDecimal MEGA = BigDecimal.valueOf(1048576);
	private static final BigDecimal GIGA = BigDecimal.valueOf(1073741824);

	// constants for units
	private static final String GB = "Gb";
	private static final String MB = "Mb";
	private static final String KB = "Kb";

	private static final int SCALE = 2;

	// Private ctor to prevent instantiation
	private ScrapeUtils(String[] args) {
	}

	/**
	 * Formats the passed bytes into human readable format
	 * 
	 * @param size
	 * @return
	 */
	public static String formatPageSize(long noOfBytes) {
		StringBuilder hrSize = new StringBuilder(StringUtils.EMPTY);

		BigDecimal k = BigDecimal.valueOf(noOfBytes).setScale(SCALE)
				.divide(KILO, BigDecimal.ROUND_CEILING);
		BigDecimal m = BigDecimal.valueOf(noOfBytes).setScale(SCALE)
				.divide(MEGA, BigDecimal.ROUND_CEILING);
		BigDecimal g = BigDecimal.valueOf(noOfBytes).setScale(SCALE)
				.divide(GIGA, BigDecimal.ROUND_CEILING);

		if (BigDecimal.ONE.compareTo(g) <= 0) {

			hrSize.append(g.toString()).append(GB);
		} else if (BigDecimal.ONE.compareTo(m) <= 0) {

			hrSize.append(m.toString()).append(MB);

		} else {

			hrSize.append(k.toString()).append(KB);
		}

		return hrSize.toString();
	}
}
