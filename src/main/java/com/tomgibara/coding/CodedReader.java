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

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitStreamException;

/**
 * Pairs a {@link Reader} with an {@link ExtendedCoding} to provide a convenient
 * way of reading coded data.
 *
 * @author Tom Gibara
 *
 */

public class CodedReader {

	// fields

	private final BitReader reader;
	private final ExtendedCoding coding;

	// constructors

	/**
	 * Creates a coded reader.
	 *
	 * @param reader
	 *            the reader from which bits will be read
	 * @param coding
	 *            used to decode the bits into values
	 */

	public CodedReader(BitReader reader, ExtendedCoding coding) {
		if (reader == null) throw new IllegalArgumentException("null reader");
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.reader = reader;
		this.coding = coding;
	}

	// accessors

	/**
	 * The reader that supplies the bits for the coding.
	 * 
	 * @return the bit reader
	 */

	public BitReader getReader() {
		return reader;
	}

	/**
	 * The coding that decodes the bits.
	 * 
	 * @return the coding
	 */

	public ExtendedCoding getCoding() {
		return coding;
	}

	// methods

	/**
	 * Decodes a positive integer from the reader.
	 *
	 * @return an integer greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public int readPositiveInt() {
		return coding.decodePositiveInt(reader);
	}

	/**
	 * Decodes a positive long from the reader.
	 *
	 * @return a long greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public long readPositiveLong() {
		return coding.decodePositiveLong(reader);
	}

	/**
	 * Decodes a positive BigInteger from the reader.
	 *
	 * @return a BigInteger greater than or equal to zero
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public BigInteger readPositiveBigInt() {
		return coding.decodePositiveBigInt(reader);
	}

	/**
	 * Decodes an integer from the reader.
	 *
	 * @return an integer
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public int readInt() {
		return coding.decodeInt(reader);
	}

	/**
	 * Decodes a long from the reader.
	 *
	 * @return a long
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public long readLong() {
		return coding.decodeLong(reader);
	}

	/**
	 * Decodes a BigInteger from the reader.
	 *
	 * @return a BigInteger
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public BigInteger readBigInt() {
		return coding.decodeBigInt(reader);
	}

	/**
	 * Decodes a float from the reader.
	 *
	 * @return a float
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public float readFloat() {
		return coding.decodeFloat(reader);
	}

	/**
	 * Decodes a double from the reader.
	 *
	 * @return a double
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public double readDouble() {
		return coding.decodeDouble(reader);
	}

	/**
	 * Decodes a BigDecimal from the reader.
	 *
	 * @return a BigDecimal
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public BigDecimal readDecimal() {
		return coding.decodeDecimal(reader);
	}

}
