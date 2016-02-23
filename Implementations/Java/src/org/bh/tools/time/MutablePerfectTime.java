package org.bh.tools.time;

import com.sun.istack.internal.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bh.tools.time.PerfectTime.TimePrecision.*;

/**
 * Copyright BHStudios ©2016 BH-1-PS.
 *
 * Represents a changeable very precise instant in time, accurate down to the femtosecond.
 *
 * @author Kyli Rouge
 * @since 2016-02-21
 */
public class MutablePerfectTime extends PerfectTime {

	/**
	 * Creates a mutable version of the given time
	 *
	 * @param basis the instant off of which to base this
	 */
	public MutablePerfectTime(PerfectTime basis) {
		super(basis, SECONDS);
	}

	/**
	 * Creates a new MutablePerfectTime with the given number time and the precision it represents.
	 *
	 * @param time      The initial time that this will represent.
	 * @param precision The unit that {@code time} is provided in.
	 */
	public MutablePerfectTime(@NotNull Number time, @NotNull TimePrecision precision) {
		super(time, precision);
	}

	/**
	 * Creates a new MutablePerfectTime with the given number time and the precision it represents.
	 *
	 * @param time      The initial time that this will represent.
	 * @param precision The unit that {@code time} is provided in.
	 */
	public MutablePerfectTime(@NotNull String time, @NotNull TimePrecision precision) {
		super(time, precision);
	}

	/**
	 * Returns a new {@link MutablePerfectTime} representing the same instant as this one
	 *
	 * @return a clone of this {@link MutablePerfectTime}
	 *
	 * @throws CloneNotSupportedException never :D
	 */
	@Override protected MutablePerfectTime clone() throws CloneNotSupportedException {
		try {
			super.clone();
		} catch (Throwable t) {
			Logger.getGlobal().log(Level.SEVERE, "Could not clone parent", t);
		}
		return new org.bh.tools.time.MutablePerfectTime(this);
	}

	/**
	 * Changes the value of this {@link MutablePerfectTime}, and then returns this.
	 *
	 * @param newValue     The new value
	 * @param newPrecision The precision of the new value
	 *
	 * @return this
	 */
	public MutablePerfectTime setValue(@NotNull Number newValue, @NotNull TimePrecision newPrecision) {
		return this.setValue(newValue.toString(), newPrecision);
	}

	/**
	 * Changes the value of this {@link MutablePerfectTime}, and then returns this.
	 *
	 * @param newValue     The new value
	 * @param newPrecision The precision of the new value
	 *
	 * @return this
	 */
	public MutablePerfectTime setValue(@NotNull String newValue, @NotNull TimePrecision newPrecision) {
		_value = new PerfectTime(newValue, newPrecision)._value;
		return this;
	}
}
