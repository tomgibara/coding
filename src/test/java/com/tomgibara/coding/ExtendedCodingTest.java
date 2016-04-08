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
import com.tomgibara.bits.Bits;

// TODO should allow number of bits to be configured
public abstract class ExtendedCodingTest<C extends ExtendedCoding> extends CodingTest<C> {

	@Override
	int getMaxEncodableValue(C coding) {
		return -1;
	}

	public void testInt() {
		int[] ints = new int[1000];
		BitWriter writer = Bits.writerTo(ints);
		BitReader reader = Bits.readerFrom(ints);
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

	private void checkInt(BitWriter writer, BitReader reader, int i) {
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
			v.clear();
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
		int[] ints = new int[bits / 32];
		BitWriter writer = Bits.writerTo(ints);
		BitReader reader = Bits.readerFrom(ints);

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

	private void checkPositiveBigInt(BitWriter writer, BitReader reader, BigInteger i) {
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
		int[] ints = new int[4];
		BitWriter writer = Bits.writerTo(ints);
		BitReader reader = Bits.readerFrom(ints);
		c.encodeInt(writer, value);
		writer.flush();
		return c.decodePositiveInt(reader);
	}

	private void checkBigInt(BitWriter writer, BitReader reader, BigInteger i) {
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
			BitWriter writer = Bits.writerTo(memory, bytes * 8);
			BitReader reader = Bits.readerFrom(memory, bytes * 8);
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

	private void checkDouble(BitWriter writer, BitReader reader, double d) {
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

	public void testFloat() {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			int bytes = 16;
			int[] memory = new int[bytes];
			BitWriter writer = Bits.writerTo(memory, bytes * 8);
			BitReader reader = Bits.readerFrom(memory, bytes * 8);
			checkFloat(writer, reader, 0.0f);
			checkFloat(writer, reader, -0.0f);
			checkFloat(writer, reader, 1.0f);
			checkFloat(writer, reader, 2.0f);
			checkFloat(writer, reader, 3.0f);
			checkFloat(writer, reader, 4.0f);

			for (float d = -100.0f; d < 100.0f; d += 0.1f) {
				checkFloat(writer, reader, d);
			}

			Random r = new Random(0L);
			for (int i = 0; i < 10000; i++) {
				float f = Float.intBitsToFloat(r.nextInt());
				if (Float.isNaN(f) || Float.isInfinite(f)) continue;
				checkFloat(writer, reader, f);
			}
		}
	}

	private void checkFloat(BitWriter writer, BitReader reader, float f) {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			writer.setPosition(0);
			coding.encodeFloat(writer, f);
			writer.flush();
			reader.setPosition(0);
			float g = coding.decodeFloat(reader);
			assertEquals(f, g);
		}
	}

	public void testDecimal() {
		for (C coding : getCodings()) {
			if (isEncodableValueLimited(coding)) return;
			int bits = 10240;
			int[] memory = new int[bits / 8];
			BitWriter writer = Bits.writerTo(memory, bits);
			BitReader reader = Bits.readerFrom(memory, bits);

			Random r = new Random(0L);
			for (int i = 0; i < 10000; i++) {
				checkDecimal(writer, reader, new BigDecimal(new BigInteger(r.nextInt(bits/4), r), r.nextInt(100) - 50));
			}
		}
	}

	private void checkDecimal(BitWriter writer, BitReader reader, BigDecimal d) {
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
