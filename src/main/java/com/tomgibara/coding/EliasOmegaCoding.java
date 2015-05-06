/*
 * Copyright 2007 Tom Gibara
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
import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.BitWriter;

/**
 * Implements Elias omega coding. Note that in contrast to most presentations of
 * Elias omega coding, the mapping from integers to code words begins at zero so
 * that 0 -> "0", 1 -> "100" and so on.
 *
 * The singleton instance of this class is available from
 * {@link EliasOmegaCoding#instance}.
 *
 * @author Tom Gibara
 * @see http://en.wikipedia.org/wiki/Elias_omega_coding
 */

final public class EliasOmegaCoding extends UniversalCoding {

	// statics

	/**
	 * The sole instance of this class.
	 */

	public static final EliasOmegaCoding instance = new EliasOmegaCoding();

	/**
	 * An extended coding of this class.
	 */

	public static final ExtendedCoding extended = new ExtendedCoding(instance);

	private static int encodeInt0(BitWriter writer, int value) {
		if (value == 1) return 0;
		int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
		return encodeInt0(writer, size-1) + writer.write(value, size);
	}

	private static int encodeLong0(BitWriter writer, long value) {
		if (value == 1L) return 0;
		int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
		return encodeInt0(writer, size-1) + writer.write(value, size);
	}

	private static int encodeBigInt0(BitWriter writer, BigInteger value) {
		if (value.equals(BigInteger.ONE)) return 0;
		int size = value.bitLength();
		return encodeInt0(writer, size - 1) + writer.write(value, size);
	}

	// constructors

	private EliasOmegaCoding() {}

	// coding methods

	@Override
	public int decodePositiveInt(BitReader reader) {
		//conceptually simple version
//		int value = 1;
//		while (reader.readBoolean()) {
//			value = (1 << value) | reader.read(value);
//		}
//		return value;

		// optimized version
		if (!reader.readBoolean()) return 0;
		int value = 2 | reader.read(1);
		if (!reader.readBoolean()) return value - 1;
		value = (1 << value) | reader.read(value);
		if (!reader.readBoolean()) return value - 1;
		value = (1 << value) | reader.read(value);
		if (!reader.readBoolean()) return value - 1;
		//TODO could check value < 32 to catch call decoding errors
		value = (1 << value) | reader.read(value);
		if (reader.readBoolean()) throw new BitStreamException("value too large for int");
		return value - 1;
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		//conceptually simple version
//		long value = 1;
//		while (reader.readBoolean()) {
//			value = (1L << (int)value) | reader.readLong((int)value);
//		}
//		return value;

		// optimized version
		if (!reader.readBoolean()) return 0L;
		int value = 2 | reader.read(1);
		if (!reader.readBoolean()) return value - 1L;
		value = (1 << value) | reader.read(value);
		if (!reader.readBoolean()) return value - 1L;
		value = (1 << value) | reader.read(value);
		if (!reader.readBoolean()) return value - 1L;
		//TODO could check value < 64 to catch call decoding errors
		long lvalue = (1L << value) | reader.readLong(value);
		if (reader.readBoolean()) throw new BitStreamException("value too large for long");
		return lvalue - 1L;
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		int value = 1;
		while (reader.readBoolean()) {
			if (value < 32) {
				value = (1 << value) | reader.read(value);
			} else {
				BitVector vector = new BitVector(value + 1);
				vector.setBit(value, true);
				vector.rangeView(0, value).readFrom(reader);
				if (reader.readBoolean()) throw new BitStreamException("value too large for BigInteger");
				//TODO yuk, decrement is very inefficient here
				return vector.toBigInteger().subtract(BigInteger.ONE);
			}
		}
		return BigInteger.valueOf((value & 0xffffffffL) - 1L);
	}

	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		return encodeInt0(writer, value + 1) + writer.writeBit(0);
	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		return encodeLong0(writer, value + 1) + writer.writeBit(0);
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		//TODO again yuk at incremement
		return encodeBigInt0(writer, value.add(BigInteger.ONE)) + writer.writeBit(0);
	}

}
