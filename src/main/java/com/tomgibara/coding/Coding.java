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

import java.math.BigInteger;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitStreamException;
import com.tomgibara.bits.BitWriter;

/**
 * Implementations of this interface can encode non-negative numbers into a bit
 * sequences that can then be decoded into the original values.
 *
 * Some implementations may place further restrictions on the values that can be
 * encoded. Those that do not extend {@link UniversalCoding}.
 *
 * To encode and decode negative values an {@link ExtendedCoding} may be used.
 *
 * @author Tom Gibara
 *
 */

public interface Coding {

	/**
	 * Writes a non-negative integer.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            an integer greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	int encodePositiveInt(BitWriter writer, int value);

	/**
	 * Writes a non-negative long.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            an integer greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	int encodePositiveLong(BitWriter writer, long value);

	/**
	 * Writes a non-negative BigInteger.
	 *
	 * @param writer
	 *            the writer that will store the encoded value
	 * @param value
	 *            an integer greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	int encodePositiveBigInt(BitWriter writer, BigInteger value);

	/**
	 * Reads a non-negative integer.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return an integer greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	int decodePositiveInt(BitReader reader);

	/**
	 * Reads a non-negative long.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a long greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	long decodePositiveLong(BitReader reader);

	/**
	 * Reads a non-negative BigInteger.
	 *
	 * @param reader
	 *            the reader that will supply the bit encoding
	 * @return a BigInteger greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	BigInteger decodePositiveBigInt(BitReader reader);

}
