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

import java.util.Collections;

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.Bits;

public class EliasDeltaCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Collections.singleton(EliasDeltaCoding.extended);
	}

	public void testCorrectness() {
		for (ExtendedCoding coding : getCodings()) {
			int[] memory = new int[1];
//			IntArrayBitWriter writer = new IntArrayBitWriter(memory, 32);
//			IntArrayBitReader reader = new IntArrayBitReader(memory, 32);
			BitWriter writer = Bits.writerTo(memory, 32);
			BitReader reader = Bits.readerFrom(memory, 32);
			for (int i = 1; i <= 10; i++) {
				writer.setPosition(0);
				coding.encodePositiveInt(writer, i);
				writer.flush();
				reader.setPosition(0);
				int j = coding.decodePositiveInt(reader);
				assertEquals(i, j);
			}
		}
	}

	public void testSpeed() {
		testSpeed(10000000, 100);
	}

	private void testSpeed(int size, int bound) {
		for (ExtendedCoding coding : getCodings()) {
			int[] memory = new int[size];
//			IntArrayBitWriter writer = new IntArrayBitWriter(memory, size * 32);
			BitWriter writer = Bits.writerTo(memory, size * 32);
			int count = size;
			long start = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				coding.encodePositiveInt(writer, (i % bound) + 1);
				//encode(i + 1, writer);
				//memory[i] = (i % bound) + 1;
				//memory[i] = i;
			}
			writer.flush();
			long finish = System.currentTimeMillis();
			System.out.println(finish-start + " ms to write first " + count + " integers");

//			IntArrayBitReader reader = new IntArrayBitReader(memory, writer.getSize());
			BitReader reader = Bits.readerFrom(memory, size * 32);
			start = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				int v = coding.decodePositiveInt(reader);
				//int v = memory[i];
				if (v != (i % bound) + 1) throw new RuntimeException("on read " + i);
				//if (v != i) throw new RuntimeException("on read " + i);
				//if (v != i+1) throw new RuntimeException("on read " + i);
			}
			finish = System.currentTimeMillis();
			System.out.println(finish-start + " ms to read first " + count + " integers");
		}
	}


}
