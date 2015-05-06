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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitStreamException;
import com.tomgibara.bits.BitWriter;

/**
 * An extended coding adds convenient support for encoding arbitrary numeric
 * values (negative, floating point, decimal) with a {@link UniversalCoding}.
 *
 * Negative values are coded using an interleaving scheme: 0, 1, -1, 2,...
 * ie. 2n-1 if n is greater than zero, -2n otherwise.
 *
 * @author Tom Gibara
 */

//TODO investigate adding support for a fluent Chain
public class ExtendedCoding implements Coding {

	private static final BigInteger MINUS_ONE = BigInteger.ONE.negate();

	// fields

	private final UniversalCoding coding;

	// constructor

	/**
	 * Wraps a universal coding to provide encoding/decoding methods for
	 * arbitrary numeric values.
	 *
	 * @param coding
	 *            the coding to be extended
	 */

	public ExtendedCoding(UniversalCoding coding) {
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.coding = coding;
	}

	// accessors

	/**
	 * The coding that has been extended.
	 *
	 * @return the underlying coding
	 */

	public UniversalCoding getUniversalCoding() {
		return coding;
	}

    // delegated coding methods

	@Override
	public int encodePositiveInt(BitWriter writer, int value) {
		return coding.encodePositiveInt(writer, value);
	}

	@Override
	public int encodePositiveLong(BitWriter writer, long value) {
		return coding.encodePositiveLong(writer, value);
	}

	@Override
	public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
		return coding.encodePositiveBigInt(writer, value);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		return coding.decodePositiveInt(reader);
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		return coding.decodePositiveLong(reader);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		return coding.decodePositiveBigInt(reader);
	}

	// extra methods

	/**
	 * Writes an integer.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            any integer
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int encodeInt(BitWriter writer, int value) {
		value = value <= 0 ? (-value) << 1 : (value << 1) - 1;
		return coding.unsafeEncodePositiveInt(writer, value);
	}

	/**
	 * Reads an integer.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return an integer
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public int decodeInt(BitReader reader) {
		int value = decodePositiveInt(reader);
		// the term ... | (value & (1 << 31) serves to restore sign bit
		// in the special case where decoding overflows
		// but we have enough info to reconstruct the correct value
		//return (value & 1) == 1 ? ((-1 - value) >> 1) | (value & (1 << 31)) : value >>> 1;
		return (value & 1) == 0 ? -(value >> 1) | (value & (1 << 31)) : (value + 1) >>> 1;
	}

	/**
	 * Writes a long.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            any long
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int encodeLong(BitWriter writer, long value) {
		value = value <= 0L ? (-value) << 1 : (value << 1) - 1L;
		return coding.unsafeEncodePositiveLong(writer, value);
	}

	/**
	 * Reads a long.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a long
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public long decodeLong(BitReader reader) {
		long value = decodePositiveLong(reader);
		// see comments in decodeInt
		//return (value & 1L) == 0 ? -(value >> 1) | (value & (1 << 63)) : (value + 1L) >>> 1;
		return (value & 1L) == 0 ? -(value >> 1) | (value & (1L << 63)) : (value + 1L) >>> 1;
	}

	/**
	 * Writes a BigInteger.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            any BigInteger
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int encodeBigInt(BitWriter writer, BigInteger value) {
		value = value.signum() <= 0 ? value.shiftLeft(1).negate() : value.shiftLeft(1).subtract(BigInteger.ONE);
		return coding.unsafeEncodePositiveBigInt(writer, value);
	}

	/**
	 * Reads a BigInteger.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a BigInteger
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public BigInteger decodeBigInt(BitReader reader) {
		BigInteger value = decodePositiveBigInt(reader);
		return value.testBit(0) ? value.add(BigInteger.ONE).shiftRight(1): value.shiftRight(1).negate();
	}

	/**
	 * Writes a double. NaN and infinite values are not supported.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            a double, not NaN or +/- infinity
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 * @throws IllegalArgumentException
	 *             if the supplied value is infinite or NaN.
	 */

	public int encodeDouble(BitWriter writer, double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException();
		long bits = Double.doubleToLongBits(value);
		long sign = bits & 0x8000000000000000L;
		if (sign == bits) return coding.unsafeEncodePositiveInt(writer, sign == 0L ? 0 : 1);

		long mantissa = bits & 0x000fffffffffffffL;
		if (sign == 0) {
			mantissa = (mantissa << 1) + 2L;
		} else {
			mantissa = (mantissa << 1) + 3L;
		}
		int exponent = (int) ((bits & 0x7ff0000000000000L) >> 52) - 1023;
		return coding.unsafeEncodePositiveLong(writer, mantissa) + encodeInt(writer, exponent);
	}

	/**
	 * Reads a double.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a double, never NaN or infinite
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public double decodeDouble(BitReader reader) {
		long mantissa = decodePositiveLong(reader);
		if (mantissa == 0L) return 0.0;
		if (mantissa == 1L) return -0.0;
		int exponent = decodeInt(reader);
		long bits = (exponent + 1023L) << 52;
		if ((mantissa & 1L) == 0) {
			mantissa = (mantissa - 2L) >> 1;
		} else {
			bits |= 0x8000000000000000L;
			mantissa = (mantissa - 3L) >> 1;
		}
		bits |= mantissa;
		return Double.longBitsToDouble(bits);
	}

	/**
	 * Writes a BigDecimal.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            any BigDecimal
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int encodeDecimal(BitWriter writer, BigDecimal value) {
		return encodeInt(writer, value.scale()) + encodeBigInt(writer, value.unscaledValue());
	}

	/**
	 * Reads a BigDecimal.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a BigDecimal
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public BigDecimal decodeDecimal(BitReader reader) {
		int scale = decodeInt(reader);
		BigInteger bigInt = decodeBigInt(reader);
		return new BigDecimal(bigInt, scale);
	}

}
