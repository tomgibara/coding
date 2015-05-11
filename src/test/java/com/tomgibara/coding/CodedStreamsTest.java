/*
 * Copyright 2015 Tom Gibara
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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Random;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.BitVectorWriter;

import junit.framework.TestCase;

public class CodedStreamsTest extends TestCase {

	private static final ExtendedCoding[] CODES = {
		EliasDeltaCoding.extended,
		EliasOmegaCoding.extended,
		FibonacciCoding.extended,
		};
	private static final Class<?>[] TYPES = { boolean.class, byte.class, short.class, char.class, int.class, float.class, long.class, double.class };
	private static final int[] SIZES = { 1, 1, 2, 2, 4, 4, 8, 8 };
	private static final int MAXLEN = 1000;
	private static final int TRIALS = 1000;
	
	public void testArrays() {
		
	
		Random r = new Random(0);
		for (int t = 0; t < TRIALS; t++) {
			int type = r.nextInt(TYPES.length);
			if (type == 7) continue; // TODO bytes generate invalid doubles
			int length = r.nextInt(MAXLEN);
			for (int i = 0; i < CODES.length; i++) {
				ExtendedCoding coding = CODES[i];
				testWritePrimitiveArray(coding, type, length, r);
			}
		}
	}
	
	private void testWritePrimitiveArray(ExtendedCoding coding, int type, int length, Random random) {
		int size = SIZES[type];
		byte[] bytes = new byte[length * size];
		random.nextBytes(bytes);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final Object array;
	
		switch (type) {
		case 0:
			boolean[] bools = new boolean[length];
			for (int i = 0; i < length; i++) bools[i] = bytes[i] >= 0;
			array = bools;
			break;
		case 1: array = bytes; break;
		case 2: array = ShortBuffer.allocate(length).put(buffer.asShortBuffer()).array(); break;
		case 3: array = CharBuffer.allocate(length).put(buffer.asCharBuffer()).array(); break;
		case 4: array = IntBuffer.allocate(length).put(buffer.asIntBuffer()).array(); break;
		case 5:
			float[] floats = FloatBuffer.allocate(length).put(buffer.asFloatBuffer()).array();
			for (int i = 0; i < length; i++) {
				if (Float.isInfinite(floats[i]) || Float.isNaN(floats[i])) floats[i] = 0;
			}
			array = floats;
			break;
		case 6: array = LongBuffer.allocate(length).put(buffer.asLongBuffer()).array(); break;
		case 7:
			double[] doubles = DoubleBuffer.allocate(length).put(buffer.asDoubleBuffer()).array();
			for (int i = 0; i < length; i++) {
				if (Double.isInfinite(doubles[i]) || Double.isNaN(doubles[i])) doubles[i] = 0;
			}
			array = doubles;
			break;
		default: throw new IllegalStateException();
		}
		
		BitVectorWriter writer = new BitVectorWriter();
		CodedWriter w = new CodedWriter(writer, coding);
		int bits = CodedStreams.writePrimitiveArray(w, array);
		BitVector vector = writer.toBitVector();
		assertEquals(bits, vector.size());
		
		BitReader reader = vector.openReader();
		CodedReader r = new CodedReader(reader, coding);
		
		switch (type) {
		case 0 : assertTrue(equalAsArrays(array, CodedStreams.readBooleanArray(r))); break;
		case 1 : assertTrue(equalAsArrays(array, CodedStreams.readByteArray(r))); break;
		case 2 : assertTrue(equalAsArrays(array, CodedStreams.readShortArray(r))); break;
		case 3 : assertTrue(equalAsArrays(array, CodedStreams.readCharArray(r))); break;
		case 4 : assertTrue(equalAsArrays(array, CodedStreams.readIntArray(r))); break;
		case 5 : assertTrue(equalAsArrays(array, CodedStreams.readFloatArray(r))); break;
		case 6 : assertTrue(equalAsArrays(array, CodedStreams.readLongArray(r))); break;
		case 7 : assertTrue(equalAsArrays(array, CodedStreams.readDoubleArray(r))); break;
		default: throw new IllegalStateException();
		}
		
	}
	
	public static boolean equalAsArrays(Object as, Object bs) {
		if (as == bs) return true;
		if (as == null && bs == null) return true;
		if (as == null || bs == null) return false;
		final Class<? extends Object> classA = as.getClass();
		final Class<? extends Object> classB = bs.getClass();
		if (classA.isArray() != classB.isArray()) return false;
		if (!classA.isArray()) return as.equals(bs);
		if (classA != classB) return false;
		final Class<?> type = classA.getComponentType();
		if (type == boolean.class) return Arrays.equals((boolean[]) as, (boolean[]) bs);
		else if (type == byte.class) return Arrays.equals((byte[]) as, (byte[]) bs);
		else if (type == char.class) return Arrays.equals((char[]) as, (char[]) bs);
		else if (type == double.class) return Arrays.equals((double[]) as, (double[]) bs);
		else if (type == float.class) return Arrays.equals((float[]) as, (float[]) bs);
		else if (type == int.class) return Arrays.equals((int[]) as, (int[]) bs);
		else if (type == long.class) return Arrays.equals((long[]) as, (long[]) bs);
		else if (type == short.class) return Arrays.equals((short[]) as, (short[]) bs);
		else return Arrays.equals((Object[]) as, (Object[]) bs);
	}

}
