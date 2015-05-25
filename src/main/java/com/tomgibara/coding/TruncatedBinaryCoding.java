/*
 * Copyright 2012 Tom Gibara
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

import java.math.BigInteger;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitStreamException;
import com.tomgibara.bits.BitWriter;

/**
 * Implements truncated binary coding.
 *
 * @author Tom Gibara
 * @see <a href="http://en.wikipedia.org/wiki/Truncated_binary_encoding">Truncated binary encoding</a>
 */

public final class TruncatedBinaryCoding implements Coding {

	//TODO could change comparisons to bit length checks?
	private static final BigInteger INT_MAX_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);
	private static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

	private final BigInteger size;
	private final int bits;
	private final int intSize;
	private final long longSize;
	private final BigInteger bigIntCutoff;
	private final int intCutoff;
	private final long longCutoff;

	/**
	 * Constructs a truncated binary coding for the first <em>size</em> natural
	 * numbers.
	 *
	 * @param size
	 *            the number of values, must be greater than zero
	 */

	public TruncatedBinaryCoding(int size) {
		this(BigInteger.valueOf(size));
	}

	/**
	 * Constructs a truncated binary coding for the first <em>size</em> natural
	 * numbers.
	 *
	 * @param size
	 *            the number of values, must be greater than zero
	 */

	public TruncatedBinaryCoding(long size) {
		this(BigInteger.valueOf(size));
	}

	/**
	 * Constructs a truncated binary coding for the first <em>size</em> natural
	 * numbers.
	 *
	 * @param size
	 *            the number of values, must be greater than zero
	 */

	public TruncatedBinaryCoding(BigInteger size) {
		if (size == null) throw new IllegalArgumentException("null size");
		if (size.signum() < 1) throw new IllegalArgumentException("non-positive size");

		this.size = size;
		bits = size.bitLength() - 1;
		intSize = size.compareTo(INT_MAX_VALUE) <= 0 ? size.intValue() : 0;
		longSize = size.compareTo(LONG_MAX_VALUE) <= 0 ? size.longValue() : 0L;

		bigIntCutoff = BigInteger.ONE.shiftLeft(size.bitLength()).subtract(size);
		intCutoff = intSize > 0 ? bigIntCutoff.intValue() : 0;
		longCutoff = longSize > 0L ? bigIntCutoff.longValue() : 0L;
	}

	public BigInteger getSize() {
		return size;
	}

	@Override
	public int encodePositiveInt(BitWriter writer, int value) {
		if (writer == null) throw new IllegalArgumentException("null writer");
		if (value < 0) throw new IllegalArgumentException("negative value");
		if (intSize != 0) {
			if (value >= intSize) throw new IllegalArgumentException("invalid value");
			return encodeInt(writer, value);
		}
		return longSize == 0L ? encodeBigInt(writer, BigInteger.valueOf(value)) : encodeLong(writer, value);
	}

	@Override
	public int encodePositiveLong(BitWriter writer, long value) {
		if (writer == null) throw new IllegalArgumentException("null writer");
		if (value < 0L) throw new IllegalArgumentException("negative value");
		if (intSize != 0) {
			if (value >= intSize) throw new IllegalArgumentException("invalid value");
			return encodeInt(writer, (int) value);
		}
		if (longSize != 0) {
			if (value >= longSize) throw new IllegalArgumentException("invalid value");
			return encodeLong(writer, value);
		}
		return encodeBigInt(writer, BigInteger.valueOf(value));
	}

	@Override
	public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
		if (writer == null) throw new IllegalArgumentException("null writer");
		if (value.signum() < 0) throw new IllegalArgumentException("negative value");
		return encodeBigInt(writer, value);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		if (intSize != 0) {
			return decodeInt(reader);
		}
		if (longSize != 0) {
			long value = decodeLong(reader);
			if (value > Integer.MAX_VALUE) throw new BitStreamException("value too large for int");
			return (int) value;
		}
		BigInteger value = decodeBigInt(reader);
		if (value.compareTo(INT_MAX_VALUE) > 0) throw new BitStreamException("value too large for int");
		return value.intValue();
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		if (longSize != 0L) {
			return decodeLong(reader);
		}
		BigInteger value = decodeBigInt(reader);
		if (value.compareTo(LONG_MAX_VALUE) > 0) throw new BitStreamException("value too large for long");
		return value.intValue();
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		return decodeBigInt(reader);
	}

	int encodeInt(BitWriter writer, int value) {
		return value < intCutoff ? writer.write(value, bits) : writer.write(value + intCutoff, bits + 1);
	}

	int decodeInt(BitReader reader) {
		int value = reader.read(bits);
		return value < intCutoff ? value : ((value << 1) | reader.readBit()) - intCutoff;
	}

	int encodeLong(BitWriter writer, long value) {
		return value < longCutoff ? writer.write(value, bits) : writer.write(value + longCutoff, bits + 1);
	}

	long decodeLong(BitReader reader) {
		long value = reader.readLong(bits);
		return value < longCutoff ? value : ((value << 1) | reader.readBit()) - longCutoff;
	}

	int encodeBigInt(BitWriter writer, BigInteger value) {
		return value.compareTo(bigIntCutoff) < 0 ? writer.write(value, bits) : writer.write(value.add(bigIntCutoff), bits + 1);
	}

	BigInteger decodeBigInt(BitReader reader) {
		BigInteger value = reader.readBigInt(bits);
		if (value.compareTo(bigIntCutoff) < 0) return value;
		value = value.shiftLeft(1);
		if (reader.readBoolean()) value = value.add(BigInteger.ONE);
		return value.subtract(bigIntCutoff);
	}

}
