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
import java.util.Random;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.IntArrayBitReader;
import com.tomgibara.bits.IntArrayBitWriter;

// TODO should allow number of bits to be configured
public abstract class ExtendedCodingTest<C extends ExtendedCoding> extends CodingTest<C> {

	@Override
	int getMaxEncodableValue(C coding) {
		return -1;
	}

	public void testInt() {
		IntArrayBitWriter writer = new IntArrayBitWriter(30000);
		IntArrayBitReader reader = new IntArrayBitReader(writer.getInts(), 30000);
		for (int i = -10000; i < 10000; i++) {
			checkInt(writer, reader, i);
		}

		// tests that fit into int manipulation
		checkInt(writer, reader, 1-(1 << 30));
		checkInt(writer, reader, -(1 << 30));
		checkInt(writer, reader, 1 - (1 << 30));
		checkInt(writer, reader, (1 << 30));
		checkInt(writer, reader, 1 + (1 << 30));

		//tests that exceed int manipulation
		checkInt(writer, reader, -(1 << 30) - 1);

		Random r = new Random(0L);
		for (int i = 0; i < 1000000; i++) {
			checkInt(writer, reader, r.nextInt());
		}

	}

	private void checkInt(IntArrayBitWriter writer, IntArrayBitReader reader, int i) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding) && Math.abs(i) > getMaxEncodableValue(coding)) return;
			writer.setPosition(0);
			coding.encodeInt(writer, i);
			writer.flush();
			reader.setPosition(0);
			int j = coding.decodeInt(reader);
			assertEquals(i, j);
			reader.setPosition(0);
		}
	}

	public void testLong() {
		//30000 to accommodate unary coding
		BitVector v = new BitVector(30000);
		for (long i = -10000; i < 10000; i++) {
			checkLong(v, i);
		}

		// tests that fit into long manipulation
		checkLong(v, 1L-(1L << 62));
		checkLong(v, -(1L << 62));
		checkLong(v, 1L - (1L << 62));
		//TODO identify why fib enc fails this test
		//checkLong(v, (1L << 62));
		checkLong(v, 1L + (1L << 62));

		//tests that exceed int manipulation
		checkLong(v, -(1L << 62) - 1L);

		Random r = new Random(0L);
		for (int i = 0; i < 1000000; i++) {
			checkLong(v, r.nextLong());
		}

	}

	private void checkLong(BitVector v, long i) {
		for (C coding : getCodings()) {
			v.set(false);
			BitWriter writer = v.openWriter();
			if (isEncodableValueLimited(coding) && Math.abs(i) > getMaxEncodableValue(coding)) return;
			coding.encodeLong(writer, i);
			writer.flush();
			BitReader reader = v.openReader();
			long j = coding.decodeLong(reader);
			assertEquals(i, j);
		}
	}

	public void testBigInt() {
		int bits = 4096;
		IntArrayBitWriter writer = new IntArrayBitWriter(bits);
		IntArrayBitReader reader = new IntArrayBitReader(writer.getInts(), writer.getSize());

		for (long i = 0; i < 100L; i++) {
			checkPositiveBigInt(writer, reader, BigInteger.valueOf(i));
		}

		for (long i = 0; i < 10000000000L; i+=1000000L) {
   			checkPositiveBigInt(writer, reader, BigInteger.valueOf(i));
		}

		Random r = new Random(0L);
		for (int i = 0; i < 10000; i++) {
			BigInteger value = new BigInteger(r.nextInt(bits/4), r);
			checkBigInt(writer, reader, value);
		}

	}

	private void checkPositiveBigInt(IntArrayBitWriter writer, IntArrayBitReader reader, BigInteger i) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding) && i.compareTo(BigInteger.valueOf(getMaxEncodableValue(coding))) > 0) return;
			writer.setPosition(0);
			coding.encodePositiveBigInt(writer, i);
			writer.flush();
			reader.setPosition(0);
			BigInteger j = coding.decodePositiveBigInt(reader);
			assertEquals(i, j);
			reader.setPosition(0);
		}
	}

	public void testIntInterleave() {
		for (C coding : getCodings()) {
			assertEquals(0, readUnsignedInt(coding, 0));
			assertEquals(1, readUnsignedInt(coding, 1));
			assertEquals(2, readUnsignedInt(coding, -1));
			assertEquals(3, readUnsignedInt(coding, 2));
			assertEquals(4, readUnsignedInt(coding, -2));
		}
	}

	private int readUnsignedInt(C c, int value) {
		IntArrayBitWriter writer = new IntArrayBitWriter(100);
		IntArrayBitReader reader = new IntArrayBitReader(writer.getInts(), writer.getSize());
		c.encodeInt(writer, value);
		writer.flush();
		return c.decodePositiveInt(reader);
	}

	private void checkBigInt(IntArrayBitWriter writer, IntArrayBitReader reader, BigInteger i) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding) && i.abs().compareTo(BigInteger.valueOf(getMaxEncodableValue(coding))) > 0) return;
			writer.setPosition(0);
			coding.encodeBigInt(writer, i);
			writer.flush();
			reader.setPosition(0);
			BigInteger j = coding.decodeBigInt(reader);
			assertEquals(i, j);
			reader.setPosition(0);
		}
	}

	public void testDouble() {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			int bytes = 16;
			int[] memory = new int[bytes];
			IntArrayBitWriter writer = new IntArrayBitWriter(memory, bytes * 8);
			IntArrayBitReader reader = new IntArrayBitReader(memory, bytes * 8);
			checkDouble(writer, reader, 0.0);
			checkDouble(writer, reader, -0.0);
			checkDouble(writer, reader, 1.0);
			checkDouble(writer, reader, 2.0);
			checkDouble(writer, reader, 3.0);
			checkDouble(writer, reader, 4.0);

			for (double d = -100.0; d < 100.0; d += 0.1) {
				checkDouble(writer, reader, d);
			}

			Random r = new Random(0L);
			for (int i = 0; i < 10000; i++) {
				double d = Double.longBitsToDouble(r.nextLong());
				if (Double.isNaN(d) || Double.isInfinite(d)) continue;
				checkDouble(writer, reader, d);
			}
		}
	}

	private void checkDouble(IntArrayBitWriter writer, IntArrayBitReader reader, double d) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			writer.setPosition(0);
			coding.encodeDouble(writer, d);
			writer.flush();
			reader.setPosition(0);
			double e = coding.decodeDouble(reader);
			assertEquals(d, e);
		}
	}

	public void testDecimal() {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			int bits = 10240;
			int[] memory = new int[bits / 8];
			IntArrayBitWriter writer = new IntArrayBitWriter(memory, bits);
			IntArrayBitReader reader = new IntArrayBitReader(memory, bits);

			Random r = new Random(0L);
			for (int i = 0; i < 10000; i++) {
				checkDecimal(writer, reader, new BigDecimal(new BigInteger(r.nextInt(bits/4), r), r.nextInt(100) - 50));
			}
		}
	}

	private void checkDecimal(IntArrayBitWriter writer, IntArrayBitReader reader, BigDecimal d) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			writer.setPosition(0);
			coding.encodeDecimal(writer, d);
			writer.flush();
			reader.setPosition(0);
			BigDecimal e = coding.decodeDecimal(reader);
			assertEquals(d, e);
			reader.setPosition(0);
		}
	}

}
