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
import java.util.Random;

import com.tomgibara.bits.BitVector;
import com.tomgibara.bits.NullBitWriter;
import com.tomgibara.bits.BitVector.Operation;

public class RiceCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	private static ExtendedCoding coding(int divisor) {
		return new ExtendedCoding(new RiceCoding(divisor));
	}

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Arrays.asList( coding(6), coding(3), coding(1), coding(0) );
	}

	@Override
	int getMaxEncodableValue(ExtendedCoding coding) {
		return 10 << ((RiceCoding)(coding.getUniversalCoding())).getBits();
	}

	public void testEqualsGolomb() {
		Random r = new Random();
		for (int i = 0; i < 10000; i++) {
			int bits = r.nextInt(8);
			int value = r.nextInt(10 << bits);
			RiceCoding rc = new RiceCoding(bits);
			GolombCoding gc = new GolombCoding(1 << bits);
			NullBitWriter rw = new NullBitWriter();
			rc.encodePositiveInt(rw, value);
			NullBitWriter gw = new NullBitWriter();
			gc.encodePositiveInt(gw, value);
			assertEquals(rw.getPosition(), gw.getPosition());
			BitVector v = new BitVector((int) rw.getPosition());
			rc.encodePositiveInt(v.openWriter(), value);
			rc.encodePositiveInt(v.openWriter(Operation.XOR, 0), value);
			assertTrue(v.isAllZeros());
		}
	}

}
