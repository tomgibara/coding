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
import com.tomgibara.bits.BitWriter;

/**
 * Implements Golomb coding. The coding is parameterized by a divisor which
 * determines the size of the quotients generated output by the coding. For
 * divisors which are a power of two, better performance can be expected by
 * using a {@link RiceCoding}.
 *
 * @author Tom Gibara
 */

public final class GolombCoding extends UniversalCoding {

	private final int divisor;
	private final BigInteger bigDivisor;
	private final TruncatedBinaryCoding truncated;

	public GolombCoding(int divisor) {
		if (divisor < 1) throw new IllegalArgumentException("non-positive divisor");
		this.divisor = divisor;
		this.bigDivisor = BigInteger.valueOf(divisor);
		truncated = new TruncatedBinaryCoding(bigDivisor);
	}

	public int getDivisor() {
		return divisor;
	}

	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		int q = value / divisor;
		int r = value - q * divisor;
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveInt(writer, q)
		+ truncated.encodeInt(writer, r);
	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		long q = value / divisor;
		long r = value - q * divisor;
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveLong(writer, q)
		+ truncated.encodeLong(writer, r);
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		BigInteger[] qr = value.divideAndRemainder(bigDivisor);
		return
		UnaryCoding.zeroTerminated.unsafeEncodePositiveBigInt(writer, qr[0])
		+ truncated.encodeBigInt(writer, qr[1]);
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveInt(reader) * divisor
		+ truncated.decodeInt(reader);
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveLong(reader) * divisor
		+ truncated.decodeLong(reader);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		return
		UnaryCoding.zeroTerminated.decodePositiveBigInt(reader)
		.multiply(bigDivisor)
		.add(truncated.decodeBigInt(reader));
	}

}
