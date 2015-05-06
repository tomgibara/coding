/*
 * Copyright 2011 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.coding;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class records the frequencies of byte and integer data from which it
 * calculates the 'zero-order' information entropy. The implementation places no
 * constraints on the values that may appear in the value arrays it analyzes,
 * though OutOfMemoryErrors may occur in cases where an excessively large number
 * of different values occur.
 *
 * The class has been designed to operate as efficiently as possible within the
 * constraints of robustness. The implementation should be fast enough for most
 * purposes. This class is immutable and is safe for concurrent use by multiple
 * threads.
 *
 * @author Tom Gibara
 *
 */

//TODO Add static methods that return entropy values for standard distributions.
//TODO Provide operations for combining multiple frequency distributions.
//TODO Add a constructor that can operate over an integer iterator.

public class CodingFrequencies implements Iterable<Integer> {

	// static inner classes

	/**
	 * An enumeration of the possible ways in which values are mapped to the
	 * indices of an array of frequencies.
	 */

	private enum Indexing {

		/**
		 * Indicates that value indices are obtained from indexFromValue().
		 */

		INTERLEAVED,

		/**
		 * Indicates that value indices are assigned contiguously from zero.
		 */

		LINEAR,

		/**
		 * Indicates that value indices are assigned contiguously from -128.
		 */

		BYTE;

		int index(int value) {
			switch(this) {
			case INTERLEAVED: return indexFromValue(value);
			case LINEAR:      return value;
			case BYTE:        return value != (byte) value ? 256 : (byte)value & 0xff;
			default: throw new IllegalStateException("Unsupported indexing: " + this);
			}
		}

	};

	// static fields

	private static final Integer ZERO = Integer.valueOf(0);

	/**
	 * The initial (minimum) size of a frequency array that is derived from
	 * integer data.
	 */

	private static final int INIT_FREQ_SIZE = 256;

	/**
	 * The maximum size to which a frequency array may grow before overflowing
	 * values are recorded in an 'overflow' map.
	 */

	private static final int MAX_FREQ_SIZE = 16384;

	// static utility methods

	/**
	 * Converts an integer number into a natural number by interleaving
	 * positive and negative values as even and odd numbers respectively.
	 */

	private static int indexFromValue(int value) {
		return value < 0 ? -(value<<1) - 1 : value<<1;
	}

	/**
	 * Reverses the interleaving performed by the indexFromValue method.
	 *
	 * @param index an index that was derived from the indexFromValue method
	 * @return the value associated with the specified index
	 */

	private static int valueFromIndex(int index) {
		return (index & 1) == 0 ? index>>1 : -((index + 1)>>1);
	}

	/**
	 * A convenience method for checking the parameters supplied for methods
	 * that take array arguments.
	 *
	 * @param array the array
	 * @param offset the data offset
	 * @param length the data length
	 */

	private static void checkParams(Object array, int offset, int length) {
		if (array == null) throw new IllegalArgumentException("null array");
		if (offset < 0) throw new IllegalArgumentException("strictly negative offset");
		if (length < 0) throw new IllegalArgumentException("strictly negative length");
		int arrlen;
		if (array instanceof int[]) {
			arrlen = ((int[])array).length;
		} else if (array instanceof byte[]) {
			arrlen = ((byte[])array).length;
		} else {
			throw new IllegalStateException("Unexpected array type: " + array.getClass());
		}

		if (offset+length > arrlen) throw new IllegalArgumentException("sum of offset and length exceeds array length");
	}

	// factory methods

	/**
	 * Constructs an empty set of frequencies.
	 *
	 * @return an object that contains no frequencies
	 */


	public static CodingFrequencies fromEmpty() {
		return new CodingFrequencies(new int[0], 0);
	}

	/**
	 * Identifies the frequencies of values in a byte array range. The customary
	 * constraints on offset and length parameters apply.
	 *
	 * This method does not modify the supplied array and does not keep a
	 * reference to it after this method returns.
	 *
	 * @param values an array of byte data
	 * @param offset the first byte of the data
	 * @param length the number of bytes in the data.
	 * @return the frequencies of the byte values
	 */

	public static CodingFrequencies fromValues(final byte[] values, final int offset, final int length) {
		checkParams(values, offset, length);
		int[] freqs = new int[256];
		for (int i = offset; i < offset + length; i++) {
			freqs[values[i] & 0xff]++;
		}
		return new CodingFrequencies(freqs, null, length, -128, 128, Indexing.BYTE);
	}

	/**
	 * Identifies the frequencies of values in a byte array.
	 *
	 * This method does not modify the supplied array and does not keep a
	 * reference to it after this method returns.
	 *
	 * @param values an array of byte data
	 * @return the frequencies of the byte values
	 */

	public static CodingFrequencies fromValues(final byte[] values) {
		return CodingFrequencies.fromValues(values, 0, values.length);
	}

	/**
	 * Identifies the frequencies of values in an int array range. The customary
	 * constraints on offset and length parameters apply.
	 *
	 * This method does not modify the supplied array and does not keep a
	 * reference to it after this method returns.
	 *
	 * @param values an array of int data
	 * @param offset the first int of the data
	 * @param length the number of ints in the data.
	 * @return the frequencies of the int values
	 */

	public static CodingFrequencies fromValues(final int[] values, final int offset, final int length) {
		checkParams(values, offset, length);
		int[] freq = new int[INIT_FREQ_SIZE];
		HashMap<Number, Integer> overflow = null;
		Integer minimumOverflow = null;
		Integer maximumOverflow = null;
		for (int i = offset; i < offset+length; i++) {
			int value = values[i];
			int index = indexFromValue(value);
			if (index >= freq.length) {
				if (overflow == null) {
					int newlength = Integer.highestOneBit(index) << 1;
					if (newlength > MAX_FREQ_SIZE) {
						overflow = new HashMap<Number, Integer>();
						Integer o = overflow.get(value);
						Integer n = o == null ? 1 : o+1;
						overflow.put(value, n);
					} else {
						freq = Arrays.copyOf(freq, newlength);
						freq[index]++;
					}
				} else {
					Integer v = value;
					Integer o = overflow.get(v);
					Integer n = o == null ? 1 : o+1;
					overflow.put(v, n);
					if (minimumOverflow == null || value < minimumOverflow) minimumOverflow = v;
					if (maximumOverflow == null || maximumOverflow < value) maximumOverflow = v;
				}
			} else {
				freq[index]++;
			}
		}

		int minimumValue =
			length == 0 ? 0 :
			minimumOverflow != null ? minimumOverflow :
			valueFromIndex(freq.length - 1);
		int maximumValue =
			length == 0 ? 0 :
			maximumOverflow != null ? maximumOverflow :
			valueFromIndex(freq.length - 2);
		return new CodingFrequencies(freq, overflow, length, minimumValue, maximumValue, Indexing.INTERLEAVED);
	}

	/**
	 * Identifies the frequencies of values in an int array.
	 *
	 * This method does not modify the supplied array and does not keep a
	 * reference to it after this method returns.
	 *
	 * @param values an array of int data
	 * @return the frequencies of the int values
	 */

	public static CodingFrequencies fromValues(final int[] values) {
		return CodingFrequencies.fromValues(values, 0, values.length);
	}

	/**
	 * Copies a set of frequencies from a range of the supplied array.
	 * The values associated with the frequencies in the array are assumed to
	 * range contiguously starting from zero.
	 *
	 * The frequency array supplied to this method is copied, and may thus be
	 * modified after this method returns.
	 *
	 * Note that, for reasons of performance, this method admits a
	 * frequencyTotal argument that may be supplied if already known. This allows
	 * the implementation of this method to avoid summing the frequencies
	 * unecessarily. Naturally this value cannot be verified without rendering
	 * it useless. If an incorrect value is supplied, all of the methods on the
	 * returned object will function, but incorrect values will be returned for
	 * the computed entropy values.
	 *
	 * @param frequencies an array containing a set of value frequencies
	 * @param offset the index at which the first frequency is recorded
	 * @param length the number of frequencies in the array
	 * @param frequencyTotal the sum of the frequencies (if known) or any
	 * negative value otherwise
	 * @return the frequencies enapsulated as an object
	 */

	public static CodingFrequencies fromFrequencies(int[] frequencies, final int offset, final int length, int frequencyTotal) {
		checkParams(frequencies, offset, length);
		frequencies = offset != 0 || length != frequencies.length ? Arrays.copyOfRange(frequencies, offset, offset+length) : frequencies.clone();
		if (frequencyTotal < 0) {
			frequencyTotal = 0;
			for (int i = 0; i < frequencies.length; i++) {
				frequencyTotal += frequencies[i];
			}
		}
		return new CodingFrequencies(frequencies, null, frequencyTotal, 0, frequencies.length, Indexing.LINEAR);
	}

	/**
	 * Copies a set of frequencies from the supplied array.
	 * The values associated with the frequencies in the array are assumed to
	 * range contiguously starting from zero.
	 *
	 * The frequency array supplied to this method is copied, and may thus be
	 * modified after this method returns.
	 *
	 * Note that, for reasons of performance, this method admits a
	 * frequencyTotal argument that may be supplied if already known. This allows
	 * the implementation of this method to avoid summing the frequencies
	 * unecessarily. Naturally this value cannot be verified without rendering
	 * it useless. If an incorrect value is supplied, all of the methods on the
	 * returned object will function, but incorrect values will be returned for
	 * the computed entropy values.
	 *
	 * @param frequencies an array containing a set of value frequencies
	 * @param frequencyTotal the sum of the frequencies (if known) or any
	 * negative value otherwise
	 * @return the frequencies enapsulated as an object
	 */

	public static CodingFrequencies fromFrequencies(final int[] frequencies, final int frequencyTotal) {
		return fromFrequencies(frequencies, 0, frequencies.length, frequencyTotal);
	}

	/**
	 * Copies a set of frequencies from the supplied array.
	 * The values associated with the frequencies in the array are assumed to
	 * range contiguously starting from zero.
	 *
	 * The frequency array supplied to this method is copied, and may thus be
	 * modified after this method returns.
	 *
	 * @param frequencies an array containing a set of value frequencies
	 * @return the frequencies enapsulated as an object
	 */

	public static CodingFrequencies fromFrequencies(final int[] frequencies) {
		return fromFrequencies(frequencies, -1);
	}

	// static helper method

	/**
	 * Convenience method for adapting an array of longs into an array of
	 * integers. This method may be helpful if frequencies that have been
	 * accumulated as longs are to be used with the methods on this class.
	 *
	 * @param longs
	 *            an array of longs, not null
	 * @return an array of integers containing the same values
	 * @throws IllegalArgumentException
	 *             if any value in the supplied array cannot be represented as
	 *             an int
	 */

	//TODO try to support longs directly in API
	public static int[] intsFromLongs(long[] longs) {
		if (longs == null) throw new IllegalArgumentException("null longs");
		int[] ints = new int[longs.length];
		for (int i = 0; i < longs.length; i++) {
			long value = longs[i];
			if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) throw new IllegalArgumentException();
			ints[i] = (int) value;
		}
		return ints;
	}

	//fields

	/**
	 * Records the frequencies of data values. Values, which may be negative,
	 * are mapped onto indices via the indexFromValue method.
	 */

	private final int[] frequencies;

	/**
	 * Maps large values (those whose indices exceed the length of the
	 * frequencies array) onto their frequencies, may be null.
	 */

	private final HashMap<Number, Integer> overflow;

	/**
	 * The sum of the frequencies. This value may be supplied by client code
	 * and MAY BE INCORRECT.
	 */

	private final int frequencyTotal;

	/**
	 * An inclusive lower bound on the values whose frequencies are recorded by
	 * this object.
	 */

	private final int minimumValue;

	/**
	 * An exclusive upper bound on the values whose frequencies are recorded by
	 * this object.
	 */

	private final int maximumValue;

	/**
	 * Records the method by which values are mapped to array indicies.
	 */

	private final Indexing indexing;

	/**
	 * The entropy value computed from the frequencies ('unbased').
	 */

	private final double rawEntropy;

	/**
	 * Whether the frequency representation is compact (no oveflow and no
	 * zero frequencies recorded).
	 */

	private final boolean compact;

	// constructors

	/**
	 * Constructor used by factory methods that generate frequencies from user
	 * supplied data/frequencies.
	 */

	private CodingFrequencies(final int[] frequencies, final HashMap<Number, Integer> overflow, final int frequencyTotal, final int minimumValue, final int maximumValue, final Indexing indexing) {
		this.frequencies = frequencies;
		this.overflow = overflow;
		this.frequencyTotal = frequencyTotal;
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
		this.indexing = indexing;
		rawEntropy = computeEntropy();
		compact = false;
	}

	/**
	 * Constructor used to create compact instances.
	 */

	private CodingFrequencies(final int[] frequencies, final int frequencyTotal) {
		this.frequencies = frequencies;
		this.frequencyTotal = frequencyTotal;
		overflow = null;
		minimumValue = 0;
		maximumValue = frequencies.length;
		indexing = Indexing.LINEAR;
		rawEntropy = computeEntropy();
		compact = true;
	}

	// accessors

	/**
	 * An inclusive lower bound on the values whose frequencies are recorded by
	 * this object. This bound is not strict, i.e. no guarantees are made about
	 * this value beyond: x < getMinimumValue() implies getFrequency(x) == 0.
	 *
	 * @return the minimum value whose frequency is recorded by this object
	 */

	public int getMinimumValue() {
		return minimumValue;
	}

	/**
	 * An exclusive upper bound on the values whose frequencies are recorded by
	 * this object. This bound is not strict, i.e. no guarantees are made about
	 * this value beyond: x >= getMaximumValue() implies getFrequency(x) == 0.
	 *
	 * @return the maximum value whose frequency is recorded by this object
	 */

	public int getMaximumValue() {
		return maximumValue;
	}

	/**
	 * The frequency with which the supplied value occurred in the data. The
	 * value specified is free to exceed the bounds of the data from which the
	 * frequencies were generated.
	 *
	 * @param value any integer value
	 * @return the frequency with which the supplied value occurred in the data
	 */

	public int getFrequency(int value) {
		if (minimumValue == maximumValue) return 0;
		int index = indexing.index(value);
		if (index < frequencies.length) {
			return frequencies[index];
		} else {
			Integer f = overflow == null ? ZERO : overflow.get(value);
			return f == null ? 0 : f.intValue();
		}
	}

	/**
	 * The sum of all frequencies. This value <em>may</em> have been supplied by
	 * client code and may be inconsistent with the actual frequency values.
	 * In this case the entropy values computed from the frequencies will be
	 * incorrect.
	 *
	 * @return the sum of all frequencies
	 */

	public int getFrequencyTotal() {
		return frequencyTotal;
	}


	/**
	 * A compact frequency set is one in which has discarded the original
	 * mapping between values and their frequencies. Instead, frequencies are
	 * associated with a zero indexed interval of positive integers in an
	 * unspecified order.
	 *
	 * @see #compact()
	 * @return true if the frequencies are compact and false otherwise.
	 */

	public boolean isCompact() {
		return compact;
	}

	/**
	 * An array containing all of the non-zero frequencies in an unspecified
	 * order. This method may be expected to be significantly more efficient
	 * if called on a compact instance.
	 *
	 * @see #isCompact()
	 * @see #compact()
	 * @return an array of non-zero frequencies, never null.
	 */

	public int[] getFrequencies() {
		return compact ? frequencies.clone() : compact().getFrequencies();

	}

	// iterable

	/**
	 * An iterator over all of the non-zero frequencies in an unspecified
	 * order. This method may be expected to be significantly more efficient
	 * if called on a compact instance.
	 *
	 * @see #isCompact()
	 * @see #compact()
	 * @return an iterator over non-zero frequencies, never null.
	 */

	@Override
	public Iterator<Integer> iterator() {
		if (!compact) return compact().iterator();
		if (minimumValue == maximumValue) return Collections.<Integer>emptyList().iterator();
		return new Iterator<Integer>() {

			/**
			 * The index of the next frequency to be returned from next().
			 */

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < frequencies.length;
			}

			@Override
			public Integer next() {
				return frequencies[index++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// methods

	/**
	 * Computes the information entropy of the frequencies in the specified base.
	 * This implementation computes the information entropy on creation,
	 * consequently, calling this method for different bases is extremely quick.
	 *
	 * @param base the log base for entropy calculation
	 * @return the entropy of the frequencies in the specified base
	 */

	public double entropy(double base) {
		if (base <= 1.0) throw new IllegalArgumentException("base must be strictly greater than 1.0");
		return rawEntropy/Math.log(base);
	}

	/**
	 * The computed binary information entropy of the frequencies.
	 *
	 * @see #entropy(double)
	 * @return the binary entropy of the frequencies
	 */

	public double binaryEntropy() {
		return entropy(2.0);
	}

	/**
	 * The idealized number of bits necessary to encode a sequence of the values with these frequencies.
	 *
	 * @return a number of bits
	 */

	public double bits() {
		return binaryEntropy() * getFrequencyTotal();
	}

	/**
	 * Computes the information entropy associated with the given value.
	 * If the value has zero frequency, positive infinity is returned
	 *
	 * @param base the log base for entropy calculation
	 * @param value some value
	 * @return the entropy, in the specified base, associated with the value
	 */

	public double getEntropy(double base, int value) {
		if (base <= 1.0) throw new IllegalArgumentException("base must be strictly greater than 1.0");
		int f = getFrequency(value);
		if (f == 0) return Double.POSITIVE_INFINITY;
		return -Math.log((double) f / frequencyTotal) / Math.log(base);
	}

	/**
	 * The computed binary information entropy associated with the given value.
	 *
	 * @see #getEntropy(double, int)
	 * @return the binary entropy associated with the value
	 */

	public double getBinaryEntropy(int value) {
		return getEntropy(2.0, value);
	}

	/**
	 * The idealized number of bits necessary to transmit the value.
	 *
	 * @return a number of bits
	 */

	public double getBits(int value) {
		int f = getFrequency(value);
		if (f == 0) return Double.POSITIVE_INFINITY;
		return -f * Math.log((double) f / frequencyTotal) / Math.log(2.0);
	}

	/**
	 * Returns a compact set of frequencies - if called on an already compact
	 * instance, the original object is returned. Compaction discards the
	 * original mapping between values and their frequencies. Instead,
	 * frequencies become associated with a zero indexed interval of positive
	 * integers in an unspecified order.
	 *
	 * Compact representations typically require less memory and have accurate
	 * bounds ({@link #getMinimumValue()} {@link #getMaximumValue()}). Some
	 * methods ({@link #iterator()} and {@link #getFrequencies()}) are
	 * significantly more efficient on compacted frequencies.
	 *
	 * @return a compact set of frequencies
	 */

	public CodingFrequencies compact() {
		if (compact) return this;
		final int bound = overflow == null ? frequencies.length : frequencies.length + overflow.size();
		final int[] freqs = new int[bound];
		int j = 0;
		for (int i = 0; i < frequencies.length; i++) {
			int value = frequencies[i];
			if (value != 0) freqs[j++] = value;
		}
		if (overflow != null) {
			for (int value : overflow.values()) {
				freqs[j++] = value;
			}
		}
		int[] trimmed = j == bound ? freqs : Arrays.copyOf(freqs, j);
		return new CodingFrequencies(trimmed, frequencyTotal);
	}

	// private utility methods

	/**
	 * Computes the entropy of the frequencies in the natural base.
	 */

	private double computeEntropy() {
		final double t = frequencyTotal;
		double sum = 0.0;
		for (int f : frequencies) {
			if (f == 0) continue;
			double p = f/t;
			sum -= p * Math.log(p);
		}
		if (overflow != null) {
			for (int f : overflow.values()) {
				double p = f/t;
				sum -= p * Math.log(p);
			}
		}
		return sum;
	}

}
