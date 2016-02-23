package org.bh.tools.time;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bh.tools.time.PerfectTime.TimePrecision.*;

// @formatter:off
/**
 * Copyright BHStudios ©2016 BH-1-PS.
 *
 * Represents a very precise instant in time, accurate down to the attosecond.
 *
 * This does the following things as of version 1.0.0:
 * <ul>
 *     <li><strong>Always use strings to store time</strong>, to guarantee no loss in precision. This makes it slightly
 *         less performant on low-speed systems like digital watches and flip phones, but all modern systems should
 *         handle this fine.</li>
 *     <li><strong>Use <a href="https://en.wikipedia.org/wiki/Unix_time">Unix time</a></strong> to represent time. That
 *         is, treat <code>0</code> as the very beginning of 1970, and each number represents the linear progression of
 *         time in seconds.
 *     <ul>
 *         <li><strong>Store it in base 10.</strong> That is, using only the digits (U+0030 to U+0039
 *             <code>0123456789</code>), an optional dash (U+002D <code>-</code>) to represent dates prior to 1970,
 *             and a singular, mandatory full stop for the radix point (U+002E <code>.</code>). Other characters are
 *             not to be used. A Perfect Time string must match the regex <code>/-[0-9]+\.[0-9]{18}/</code></li>.
 *         <li><strong>Use 18 fractional digits</strong> to represent parts of a second as small as an attosecond. For
 *             example, <code>1456020377.0123456789112345678</code>. This is precise enough to
 *             <a href="http://www.wolframalpha.com/input/?i=distance+light+travels+in+1+attosecond">track light
 *             traveling across atoms</a>.
 *         <ul>
 *             <li>When the data comes from systems that cannot track time that precisely, <strong>fill untrackable
 *             digits with zeroes.</strong> For instance, if a system can only track time as precise as milliseconds:
 *             <code>1456021196.012000000000000000</code>.</li>
 *         </ul>
 *         </li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * We want this to do the following things in the future:
 * <ul>
 *     <li><strong>Only store in GMT</strong> (offset +00.00).</li>
 *     <li>
 *     <ul>
 *         <li><strong><a href="https://en.wikipedia.org/wiki/Leap_second">Leap Seconds</a> are
 *             <a href="https://en.wikipedia.org/wiki/Leap_second#Workarounds_for_leap_second_issues">smeared</a> across
 *             a whole day.</strong> Although this may cause it to be offset by ½ second at noon, it also guarantees
 *             that a given timestamp always references the same exact point in time.</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * @author Kyli Rouge
 * @version 1.0.0
 * @see <a href="https://github.com/Supuhstar/Perfect-Time">Github Repo</a>
 * @since 2016-02-20
 */
// @formatter:on
public class PerfectTime extends Number implements Cloneable, Serializable {
	/**
	 * The character used to delimit the integer part from the fractional part of the timestamp
	 */
	public static final char RADIX_POINT = '.';

	/**
	 * All 18 zeroes needed to pad a number that's precise to the second
	 */
	private static final String      PADDING_FOR_IMPRECISION_SECONDS      = "000000000000000000";
	/**
	 * All 12 zeroes needed to pad a number that's precise to the millisecond
	 */
	private static final String      PADDING_FOR_IMPRECISION_MILLISECONDS = "000000000000";
	/**
	 * All 9 zeroes needed to pad a number that's precise to the microsecond
	 */
	private static final String      PADDING_FOR_IMPRECISION_MICROSECONDS = "000000000";
	/**
	 * All 6 zeroes needed to pad a number that's precise to the nanosecond
	 */
	private static final String      PADDING_FOR_IMPRECISION_NANOSECONDS  = "000000";
	/**
	 * All 3 zeroes needed to pad a number that's precise to the second
	 */
	private static final String      PADDING_FOR_IMPRECISION_FEMTOSECONDS = "000";
	/**
	 * The number of fractional digits when doing extremely accurate calculations
	 */
	private static final byte        EXTREME_PRECISION                    = 50;
	/**
	 * The math context used when doing extremely accurate calculations
	 */
	private static final MathContext EXTREME_PRECISION_MATH_CONTEXT       = new MathContext(EXTREME_PRECISION,
			RoundingMode.HALF_UP);
	/**
	 * The number of fractional digits when doing normal calculations
	 */
	private static final byte        DEFAULT_PRECISION                    = 18;
	/**
	 * The math context when doing normal calculations
	 */
	private static final MathContext DEFAULT_MATH_CONTEXT                 = new MathContext(DEFAULT_PRECISION,
			RoundingMode.HALF_UP);

	/**
	 * The value of this PerfectTime. This has <i>n</i> integer digits and {@value #DEFAULT_PRECISION} fractional ones
	 */
	protected String _value;

	/**
	 * Creates a new PerfectTime with the given number time and the precision it represents.
	 *
	 * @param time      The initial time that this will represent.
	 * @param precision The unit that {@code time} is provided in.
	 */
	public PerfectTime(@NotNull Number time, @NotNull TimePrecision precision) {
		this(time.toString(), precision);
	}

	/**
	 * Creates a new PerfectTime with the given string time and the precision it represents.
	 *
	 * @param time      The initial time that this will represent.
	 * @param precision The unit that {@code time} is provided in.
	 */
	public PerfectTime(@NotNull String time, @NotNull TimePrecision precision) {
		BigDecimal timeBigDecimal = new BigDecimal(time)
				.setScale(EXTREME_PRECISION, RoundingMode.HALF_UP);
		BigDecimal precisionPerSecond = new BigDecimal(precision.PER_SECOND)
				.setScale(EXTREME_PRECISION, RoundingMode.HALF_UP);
		BigDecimal timePerPrecision = timeBigDecimal.divide(precisionPerSecond, RoundingMode.HALF_UP)
				.setScale(DEFAULT_PRECISION, BigDecimal.ROUND_HALF_UP);

		_value = timePerPrecision.toPlainString();

		boolean hasRadix = _value.contains(Character.toString(RADIX_POINT));
		if (hasRadix) {
			String  fPart                   = _value.substring(_value.indexOf(RADIX_POINT) + 1);
			boolean fPartIsPerfectlyPrecise = fPart.length() == DEFAULT_PRECISION;
			if (!fPartIsPerfectlyPrecise) {
				// we already took care of the case where it's too precise in the above BigDecimal math, so the only
				// reason we would be here is if it's not precise enough. So, let's pad it with zeroes!

				String iPart = _value.substring(0, _value.indexOf(RADIX_POINT));

				fPart = fPart + PADDING_FOR_IMPRECISION_SECONDS.substring(fPart.length());
				_value = iPart + RADIX_POINT + fPart;
			}
			// else we're good :D
		} else {
			_value = _value + RADIX_POINT + PADDING_FOR_IMPRECISION_SECONDS;
		}
	}

	public static void main(String[] args) {
		long        start, endS, endPT, endZ, endO, endNO, endMD, endMS;
		String      s;
		PerfectTime pt, z, o, nO, mD, mS;

		start = System.nanoTime();
		s = currentTimeToString();
		endS = System.nanoTime();
		pt = currentTime();
		endPT = System.nanoTime();
		z = new PerfectTime(0, SECONDS);
		endZ = System.nanoTime();
		o = new PerfectTime(1, SECONDS);
		endO = System.nanoTime();
		nO = new PerfectTime(-1, SECONDS);
		endNO = System.nanoTime();
		mD = new PerfectTime(-123456789.01234567, SECONDS);
		endMD = System.nanoTime();
		mS = new PerfectTime("-012345678911234567892123456789.012345678911234567892123456789", SECONDS);
		endMS = System.nanoTime();

		// @formatter:off
		System.out.println("Current time to string: "          + s  + " (took " + new BigDecimal(endS - start)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("          Current time: "          + pt + " (took " + new BigDecimal(endPT - endS)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("             Zero time:          " + z  + " (took " + new BigDecimal(endZ - endPT)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("              One time:          " + o  + " (took " + new BigDecimal(endO - endZ)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("     Negative One time:         "  +nO  + " (took " + new BigDecimal(endNO - endO)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("  Manual time (double): "          + mD + " (took " + new BigDecimal(endMD - endNO)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		System.out.println("  Manual time (string): "          + mS + " (took " + new BigDecimal(endMS - endMD)
				.divide(new BigDecimal(1_000_000_000), EXTREME_PRECISION_MATH_CONTEXT) + "s)");
		// @formatter:on
	}

	/**
	 * Returns the system's most precise read of the current time as a UNIX timestamp. At the moment, this is
	 * milliseconds.
	 *
	 * @return the current time as a UNIX timestamp
	 */
	public static @NotNull String currentTimeToString() {
		return currentTime().toString();
	}

	/**
	 * Returns the system's most precise read of the current time as PerfectTime. As of this version, this is in
	 * millisecond precision.
	 *
	 * @return the current time as a PerfectTime
	 */
	public static @NotNull PerfectTime currentTime() {
		final long milliseconds = System.currentTimeMillis();
		return new PerfectTime(milliseconds, MILLISECONDS);
	}

	/**
	 * Returns the string value of this {@link PerfectTime}.
	 *
	 * @return the string value of this {@link PerfectTime}.
	 */
	public @NotNull String getValue() {
		return _value;
	}

	/**
	 * Returns an imprecise integer value of this {@link PerfectTime}, discarding entirely the fractional part.
	 *
	 * @return an imprecise representation of the seconds in this {@link PerfectTime} as an {@code int}.
	 */
	@Override public int intValue() {
		return new BigDecimal(_value).intValue();
	}

	/**
	 * Returns an imprecise integer value of this {@link PerfectTime}, discarding entirely the fractional part.
	 *
	 * @return an imprecise representation of the seconds in this {@link PerfectTime} as a {@code long}.
	 */
	@Override public long longValue() {
		return new BigDecimal(_value).longValue();
	}

	/**
	 * Returns an imprecise floating-point value version of this {@link PerfectTime}, discarding most of the fractional
	 * part.
	 *
	 * @return an imprecise representation of the seconds in this {@link PerfectTime} as a {@code float}.
	 */
	@Override public float floatValue() {
		return new BigDecimal(_value).floatValue();
	}

	/**
	 * Returns an imprecise floating-point value version of this {@link PerfectTime}, discarding some of the fractional
	 * part.
	 *
	 * @return an imprecise representation of the seconds in this {@link PerfectTime} as a {@code double}.
	 */
	@Override public double doubleValue() {
		return new BigDecimal(_value).doubleValue();
	}

	/**
	 * Returns a new {@link PerfectTime} representing the same instant as this one
	 *
	 * @return a clone of this {@link PerfectTime}
	 *
	 * @throws CloneNotSupportedException never :D
	 */
	@Override protected PerfectTime clone() throws CloneNotSupportedException {
		try {
			super.clone();
		} catch (Throwable t) {
			Logger.getGlobal().log(Level.SEVERE, "Could not clone parent", t);
		}
		return new PerfectTime(_value, SECONDS);
	}

	/**
	 * Returns the string value of this {@link PerfectTime}.
	 *
	 * @return the string value of this {@link PerfectTime}.
	 */
	@Override public String toString() {
		return _value;
	}

	/**
	 * Returns an imprecise {@link Date} version of this {@link PerfectTime}, discarding most of the fractional part.
	 *
	 * @return an imprecise representation of the milliseconds in this {@link PerfectTime} as a {@link Date}.
	 */
	public Date toDate() {
		return new Date(new BigDecimal(as(MILLISECONDS)).longValue());
	}

	/**
	 * Returns a string version of this {@link PerfectTime}, translated into the given time unit.
	 *
	 * @param precision The unit in which the returned value will be represented
	 *
	 * @return a string version of this {@link PerfectTime}, translated into the given time unit.
	 */
	public String as(TimePrecision precision) {
		return new BigDecimal(_value, EXTREME_PRECISION_MATH_CONTEXT)
				.multiply(new BigDecimal(precision.PER_SECOND), DEFAULT_MATH_CONTEXT)
				.toString();
	}

	/**
	 * Represents many different tiers of time precision, how many of them there are per second, and how many seconds
	 * there are per them. These are precise to the 50th decimal place.
	 */
	enum TimePrecision {
		/** Represents 1,000 average Gregorian years. */
		MILLENNIA(".000000000031688765366335366091139568738996430758545636762947", "31556925252.2016"),
		/** Represents 100 average Gregorian years. */
		CENTURIES(".00000000031688765366335366091139568738996430758545636762947", "3155692525.22016"),
		/** Represents 1 average Gregorian year. */
		YEARS(".000000031688765366335366091139568738996430758545636762947", "31556925.2522016"),
		/** Represents 1 day */
		DAYS("0.000011574074074074074074074074074074074074074074074074", "86400"),
		/** Represents 1 second */
		SECONDS("1", "1"),
		/** Represents 1/1,000 of a second */
		MILLISECONDS("1000", "0.001"),
		/** Represents 1/1,000,000 of a second */
		MICROOSECONDS("1000000", "0.000001"),
		/** Represents 1/1,000,000,000 of a second */
		NANOSECONDS("1000000000", "0.000000001"),
		/** Represents 1/1,000,000,000,000 of a second */
		FEMTOSECONDS("1000000000000000", "0.000000000000001"),
		/** Represents 1/1,000,000,000,000,000 of a second */
		ATTOSECONDS("1000000000000000000", "0.000000000000000001");

		/**
		 * The number of this per second
		 */
		public final String PER_SECOND;
		/**
		 * The number of seconds per this
		 */
		public final String SECONDS_PER_THIS;

		TimePrecision(String perSecond, String secondsPerThis) {
			PER_SECOND = perSecond;
			SECONDS_PER_THIS = secondsPerThis;
		}
	}
}

