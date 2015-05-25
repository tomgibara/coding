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
 * Implements unary coding. Note that unary coding is extremely inefficient for
 * even moderately sized numbers; writing large numbers could result in huge
 * resource usage.
 *
 * This implementation limits the number of bits it will write to
 * {@link UnaryCoding#MAX_ENCODABLE_INT}. This is in violation of the
 * {@link UniversalCoding} contract, but a necessary practical limitation.
 *
 * Instances of this class are available via the static fields
 * {@link UnaryCoding#oneTerminated} and {@link UnaryCoding#zeroTerminated}.
 *
 * @author Tom Gibara
 * @see <a href="http://en.wikipedia.org/wiki/Unary_coding">Unary coding</a>
 */

public final class UnaryCoding extends UniversalCoding {

	// statics

	/**
	 * The greatest value that may be written by this coding.
	 */

	public static final int MAX_ENCODABLE_INT = Integer.MAX_VALUE - 1;
	private static final BigInteger MAX_ENCODABLE_BIG_INT = BigInteger.valueOf(Integer.MAX_VALUE - 1);

	//TODO find better names

	/**
	 * An unary coding that will generate codes which consist of one bits,
	 * terminated by a zero.
	 */

	public static final UnaryCoding zeroTerminated = new UnaryCoding(false);

	/**
	 * An unary coding that will generate codes which consist of zero bits,
	 * terminated by a one.
	 */

	public static final UnaryCoding oneTerminated = new UnaryCoding(true);

	/**
	 * An extended coding based on {@link #zeroTerminated}.
	 */

	public static final ExtendedCoding zeroExtended = new ExtendedCoding(zeroTerminated);

	/**
	 * An extended coding based on {@link #oneTerminated}.
	 */

	public static final ExtendedCoding oneExtended = new ExtendedCoding(oneTerminated);

	private final boolean terminalBit;

	private UnaryCoding(boolean terminalBit) {
		this.terminalBit = terminalBit;
	}

	/**
	 * Whether the unary coding is terminated with a one
	 *
	 * @return the terminating bit
	 */

	public boolean isTerminatedByOne() {
		return terminalBit;
	}

	@Override
	int unsafeEncodePositiveInt(BitWriter writer, int value) {
		int count = (int) writer.writeBooleans(!terminalBit, value);
		count += writer.writeBoolean(terminalBit);
		return count;
	}

	@Override
	int unsafeEncodePositiveLong(BitWriter writer, long value) {
		// we can't support returning this many bits in count
		// and with unary encoding, it's hard to imagine a scenario for this
		if (value > MAX_ENCODABLE_INT) throw new IllegalArgumentException("value exceeds maximum encodable value");
		return unsafeEncodePositiveInt(writer, (int) value);
	}

	@Override
	int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value) {
		// see comments above
		if (value.compareTo(MAX_ENCODABLE_BIG_INT) > 0) throw new IllegalArgumentException("value exceeds maximum encodable value");
		return unsafeEncodePositiveInt(writer, value.intValue());
	}

	@Override
	public int decodePositiveInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		long count = reader.readUntil(terminalBit);
		//TODO should have a separate DecodingException?
		if (count > Integer.MAX_VALUE) throw new BitStreamException("value too large for int");
		return (int) count;
	}

	@Override
	public long decodePositiveLong(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		return reader.readUntil(terminalBit);
	}

	@Override
	public BigInteger decodePositiveBigInt(BitReader reader) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		return BigInteger.valueOf(reader.readUntil(terminalBit));
	}

}
