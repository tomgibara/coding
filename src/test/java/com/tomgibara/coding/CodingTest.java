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

import com.tomgibara.bits.BitReader;
import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.NullBitWriter;

import junit.framework.TestCase;

public abstract class CodingTest<C extends Coding> extends TestCase {

	abstract Iterable<C> getCodings();

	abstract int getMaxEncodableValue(C coding);

	boolean isEncodableValueLimited(C coding) {
		return getMaxEncodableValue(coding) >= 0;
	}

	public void testSmallInts() {
		for (C coding : getCodings()) {
			int min = 0;
			int max = 1000;
			if (isEncodableValueLimited(coding)) max = Math.min(max, getMaxEncodableValue(coding));

			int size;
			{
				NullBitWriter w = new NullBitWriter();
				for (int i = min; i <= max; i++) {
					coding.encodePositiveInt(w, i);
				}
				size = (int) w.getPosition();
			}

			BitVector v = new BitVector(size);

			BitWriter w = v.openWriter();
			for (int i = min; i <= max; i++) {
				coding.encodePositiveInt(w, i);
			}

			BitReader r = v.openReader();
			for (int i = min; i <= max; i++) {
				assertEquals(i, coding.decodePositiveInt(r));
			}
		}
	}
}
