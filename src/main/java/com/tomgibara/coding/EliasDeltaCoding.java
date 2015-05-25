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
import com.tomgibara.bits.BitWriter;


/**
 * Implements Elias delta coding. Note that in contrast to most presentations of
 * Elias delta coding, the mapping from integers to code words begins at zero so that
 * 0 -&gt; "1", 1 -&gt; "0100" and so on.
 *
 * The singleton instance of this class is available from
 * {@link EliasDeltaCoding#instance}.
 *
 * @author Tom Gibara
 * @see <a href="http://en.wikipedia.org/wiki/Elias_delta_coding">Elias delta coding</a>
 */

//TODO use readUntil method for implementation?
public final class EliasDeltaCoding extends UniversalCoding {

	// statics

	/**
	 * The sole instance of this class.
	 */

	public static final EliasDeltaCoding instance = new EliasDeltaCoding();

	/**
	 * An extended coding of this class.
	 */

	public static final ExtendedCoding extended = new ExtendedCoding(instance);

	// constructors

	private EliasDeltaCoding() { }

	// abstract methods

	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		value++;
		int size = 32 - Integer.numberOfLeadingZeros(value); //position of leading 1
		int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
		int count = 0;
		count += writer.writeBooleans(false, sizeLength -1);
		count += writer.write(size, sizeLength);
		count += writer.write(value, size - 1);
		return count;

	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		value++;
		int size = 64 - Long.numberOfLeadingZeros(value); //position of leading 1
		int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
		int count = 0;
		count += writer.writeBooleans(false, sizeLength -1);
		count += writer.write(size, sizeLength);
		count += writer.write(value, size - 1);
		return count;
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		//TODO can we avoid this?
		value = value.add(BigInteger.ONE);
		int size = value.bitLength();
		int sizeLength = 32 - Integer.numberOfLeadingZeros(size);
		int count = 0;
		count += writer.writeBooleans(false, sizeLength -1);
		count += writer.write(size, sizeLength);
		count += writer.write(value, size - 1);
		return count;
	}

	// coding methods

	@Override
	public int decodePositiveInt(BitReader reader) {
		int sizeLength = 0;
		while (!reader.readBoolean()) sizeLength++;
		if (sizeLength == 0) return 0;
		int size = (1 << sizeLength) | reader.read(sizeLength);
		int x = reader.read(size - 1);
		return ((1 << (size-1)) | x) - 1;
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		int sizeLength = 0;
		while (!reader.readBoolean()) sizeLength++;
		if (sizeLength == 0) return 0L;
		int size = (1 << sizeLength) | reader.read(sizeLength);
		long x = reader.readLong(size - 1);
		return ((1L << (size-1)) | x) - 1L;
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		int sizeLength = 0;
		while (!reader.readBoolean()) sizeLength++;
		if (sizeLength == 0) return BigInteger.ZERO;
		int size = (1 << sizeLength) | reader.read(sizeLength);
		BigInteger x = reader.readBigInt(size - 1);
		return x.or(BigInteger.ONE.shiftLeft(size - 1)).subtract(BigInteger.ONE);
	}

}
