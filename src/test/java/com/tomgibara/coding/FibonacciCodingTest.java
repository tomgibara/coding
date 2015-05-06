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
import java.util.Random;

import com.tomgibara.bits.IntArrayBitReader;
import com.tomgibara.bits.IntArrayBitWriter;

public class FibonacciCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Collections.singleton(FibonacciCoding.extended);
	}


	public void testGeneral() {
		for (ExtendedCoding coding : getCodings()) {

			int[] memory = new int[3];
			IntArrayBitWriter writer = new IntArrayBitWriter(memory, 96);
			IntArrayBitReader reader = new IntArrayBitReader(memory, 96);
			for (int i = 1; i <= 12; i++) {
				coding.encodePositiveInt(writer, i);
				writer.setPosition(0);
				//System.out.println(String.format("%3d = %s", i, writer));
				reader.setPosition(0);
				int j = coding.decodePositiveInt(reader);
				assertEquals(i, j);
			}

			coding.encodePositiveInt(writer, 2057509736);
			writer.setPosition(0);
			reader.setPosition(0);
			coding.decodePositiveInt(reader);
			writer.setPosition(0);
			writer.writeBooleans(false, 96);
			writer.setPosition(0);
			coding.encodePositiveInt(writer, 3005096);
			writer.setPosition(0);
			reader.setPosition(0);
			coding.decodePositiveInt(reader);

			Random r = new Random(0L);

			for (int i = 0; i < 100000; i++) {
				int j = -1;
				while (j < 1) j = r.nextInt();
				coding.encodePositiveInt(writer, j);
				writer.setPosition(0);
				reader.setPosition(0);
				int k = coding.decodePositiveInt(reader);
				assertEquals(j, k);
			}

			for (int i = 0; i < 100000; i++) {
				long l = -1;
				while (l < 1) l = r.nextLong();
				coding.encodePositiveLong(writer, l);
				writer.setPosition(0);
				reader.setPosition(0);
				long m = coding.decodePositiveLong(reader);
				assertEquals(l, m);
			}
		}
	}

}