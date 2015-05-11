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

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.tomgibara.bits.BitStreamException;
import com.tomgibara.bits.BitWriter;

/**
 * Pairs a {@link Writer} with an {@link ExtendedCoding} to provide a convenient
 * way of writing coded data.
 *
 * @author Tom Gibara
 *
 */

public class CodedWriter {

	// fields

	private final BitWriter writer;
	private final ExtendedCoding coding;

	// constructors

	/**
	 * Creates a coded writer.
	 *
	 * @param writer
	 *            the writer to which bits will be written
	 * @param coding
	 *            used to encode the values into bits
	 */

	public CodedWriter(BitWriter writer, ExtendedCoding coding) {
		if (writer == null) throw new IllegalArgumentException("null writer");
		if (coding == null) throw new IllegalArgumentException("null coding");
		this.writer = writer;
		this.coding = coding;
	}

	// accessors

	/**
	 * The writer that receives the bits of the encoding.
	 */

	public BitWriter getWriter() {
		return writer;
	}

	/**
	 * The coding that encodes the values.
	 */

	public ExtendedCoding getCoding() {
		return coding;
	}

	// methods

	/**
	 * Writes a positive integer to the writer.
	 *
	 * @param value
	 *            an integer greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writePositiveInt(int value) {
		return coding.encodePositiveInt(writer, value);
	}

	/**
	 * Writes a positive long to the writer.
	 *
	 * @param value
	 *            a long greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writePositiveLong(long value) {
		return coding.encodePositiveLong(writer, value);
	}

	/**
	 * Writes a positive BigInteger to the writer.
	 *
	 * @param value
	 *            a BigInteger greater than or equal to zero
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writePositiveBigInt(BigInteger value) {
		return coding.encodePositiveBigInt(writer, value);
	}

	/**
	 * Writes an integer to the writer.
	 *
	 * @param value
	 *            an integer
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writeInt(int value) {
		return coding.encodeInt(writer, value);
	}

	/**
	 * Writes a long to the writer.
	 *
	 * @param value
	 *            a long
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public int writeLong(long value) {
		return coding.encodeLong(writer, value);
	}

	/**
	 * Writes a BigInteger to the writer.
	 *
	 * @param value
	 *            a BigInteger
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writeBigInt(BigInteger value) {
		return coding.encodeBigInt(writer, value);
	}

	/**
	 * Writes a float to the writer.
	 *
	 * @param value
	 *            a float
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writeFloat(float value) {
		return coding.encodeFloat(writer, value);
	}

	/**
	 * Writes a double to the writer.
	 *
	 * @param value
	 *            a double
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem writing bits to the stream
	 */

	public int writeDouble(double value) {
		return coding.encodeDouble(writer, value);
	}

	/**
	 * Writes a BigDecimal to the writer.
	 *
	 * @param value
	 *            a BigDecimal
	 * @return the number of bits written
	 * @throws BitStreamException
	 *             if there was a problem reading bits from the stream
	 */

	public int writeDecimal(BigDecimal value) {
		return coding.encodeDecimal(writer, value);
	}

}
