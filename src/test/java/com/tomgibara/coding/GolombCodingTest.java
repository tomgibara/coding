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

import java.util.Arrays;

import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.NullBitWriter;

public class GolombCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	private static ExtendedCoding coding(int divisor) {
		return new ExtendedCoding(new GolombCoding(divisor));
	}

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Arrays.asList( coding(10), coding(5), coding(4), coding(1) );
	}

	@Override
	int getMaxEncodableValue(ExtendedCoding coding) {
		return ((GolombCoding)(coding.getUniversalCoding())).getDivisor() * 10;
	}

	public void testKnownValues() {
		test(10, 5, "11000");
		test(4, 5, "0111");
		test(4, 4, "1000");
		test(4, 3, "1010");
		test(4, 2, "1100");
		test(4, 1, "11110");
	}

	private static void test(int value, int divisor, String expected) {
		GolombCoding coding = new GolombCoding(divisor);
		NullBitWriter w = new NullBitWriter();
		coding.encodePositiveInt(w, value);
		BitVector v = new BitVector((int) w.getPosition());
		coding.encodePositiveInt(v.openWriter(), value);
		assertEquals(expected, v.toString());
	}

}
