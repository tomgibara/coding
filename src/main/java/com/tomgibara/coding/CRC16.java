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

//TODO this could be modelled as a Hash.
public final class CRC16 {

	// statics

	public static final short DEFAULT_POLY = (short) 0x8005;

	// fields

	private final short polynomial;
	private short checksum;

	// constructors

	public CRC16() {
		this(DEFAULT_POLY);
	}

	public CRC16(short polynomial) {
		this.polynomial = polynomial;
		reset();
	}

	// accessors

	short getPolynomial() {
		return polynomial;
	}

	// methods

	public void addBits(int value, int length) {
		int mask = 1 << (length - 1);
		do {
			checksum <<= 1;
			if (((checksum & 0x8000) == 0) ^ ((value & mask) == 0)) checksum ^= polynomial;
		} while ((mask >>>= 1) != 0);
	}

	public void addInt(int value) {
		addBits(value, 32);
	}

	public void addShort(short value) {
		addBits(value, 16);
	}

	public void addByte(byte value) {
		addBits(value, 8);
	}

	public short checksum() {
		return checksum;
	}

	public void reset() {
		checksum = (short) 0xFFFF;
	}
}