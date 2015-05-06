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

import static java.lang.Math.log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;

public class CodingFrequenciesTest extends TestCase {

	private Random r = new Random(0L);

	public void testEmpty() throws Exception {
		CodingFrequencies empty = CodingFrequencies.fromEmpty();
		assertEquals(0.0, empty.binaryEntropy());
		assertFalse(empty.iterator().hasNext());
		assertTrue(empty.isCompact());
		assertEquals(0, empty.getFrequencies().length);
		assertEquals(0, empty.getFrequency(0));
	}

	public void testFrequencies() throws Exception {
		CodingFrequencies freqs = CodingFrequencies.fromFrequencies(new int[] {4, 5, 6, 0, 1}, 1, 4, -1);
		assertEquals(12, freqs.getFrequencyTotal());
		assertEquals((-5.0/12.0*log(5.0/12.0)-6.0/12.0*log(6.0/12.0)-1.0/12.0*log(1.0/12.0))/Math.log(2.0), freqs.binaryEntropy());
		assertEquals(5, freqs.getFrequency(0));
		assertTrue(freqs.getMinimumValue() <= 0);
		assertTrue(freqs.getMaximumValue() >= 4);
		assertFalse(freqs.isCompact());
		assertEquals(3, freqs.getFrequencies().length);
		checkFrequencies(freqs);
	}

	public void testBytes() throws Exception {
		testUniformValues(-257);
	}

	public void testSmallUniform() throws Exception {
		testUniformValues(100);
	}

	public void testMediumUniform() throws Exception {
		testUniformValues(10000);
	}

	public void testLargeUniform() throws Exception {
		testUniformValues(100000);
	}

	public void testNarrowRandom() throws Exception {
		testRandom(10000, 10);
	}

	public void testWideRandom() throws Exception {
		testRandom(10000, 1000000);
	}

	public void testLargeRandom() throws Exception {
		testRandom(100000, 1000000);
	}

	public void testZero() throws Exception {
		CodingFrequencies freqs = CodingFrequencies.fromValues(new byte[] {0, 0, 0, 0});
		assertEquals(0.0, freqs.binaryEntropy());
	}

	public void testWebsite() throws Exception {
		//define some data
		int[] values = {7, 7, 3, 3, 3, 2, 7};

		//analyze its frequencies
		CodingFrequencies freqs = CodingFrequencies.fromValues(values);

		//outputs: 1.4488156357251847
		assertEquals("1.4488", String.format("%.4f", freqs.binaryEntropy()));

		//outputs: 3
		assertEquals(3, freqs.getFrequency(7));

		//outputs: [1, 3, 3]
		assertTrue(Arrays.equals(new int[] {1,3,3}, freqs.getFrequencies()));

	}

	private void testRandom(int n, int x) throws Exception {
		int[] values = new int[n];
		for (int i = 0; i < values.length; i++) {
			int v = r.nextInt(x);
			values[i] = v;
		}
		CodingFrequencies freqs = CodingFrequencies.fromValues(values);
		checkFrequencies(freqs);
		assertEquals(n, freqs.getFrequencyTotal());
		assertTrue(freqs.binaryEntropy() - uniformBinaryEntropy(x) < 0.1);
		assertFalse(freqs.isCompact());
		assertTrue(freqs.compact().getFrequencies().length <= x);
	}

	private void testUniformValues(int n) throws Exception {
		CodingFrequencies freqs;
		if (n < 0) {
			n = - n - 1;
			byte[] values = new byte[n];
			for (int i = 0; i < values.length; i++) {
				values[i] = (byte)i;
			}
			freqs = CodingFrequencies.fromValues(values);
			for (int i = 0; i < n; i++) {
				assertEquals(1, freqs.getFrequency((byte)i));
			}
			assertEquals(0, freqs.getFrequency(-129));
			assertEquals(0, freqs.getFrequency(128));
		} else {
			int[] values = new int[n];
			for (int i = 0; i < values.length; i++) {
				values[i] = i;
			}
			freqs = CodingFrequencies.fromValues(values);
			for (int i = 0; i < n; i++) {
				assertEquals(1, freqs.getFrequency(i));
			}
			assertEquals(0, freqs.getFrequency(-1));
			assertEquals(0, freqs.getFrequency(n));
		}

		checkFrequencies(freqs);
		assertEquals(n, freqs.getFrequencyTotal());
		assertTrue(freqs.binaryEntropy() - uniformBinaryEntropy(n) < 0.000001);
	}

	private void checkFrequencies(CodingFrequencies freqs) {
		int[] fs = freqs.getFrequencies();
		int sumA = 0;
		int sumB = 0;
		Iterator<Integer> it = freqs.iterator();
		for (int i = 0; i < fs.length; i++) {
			sumA += fs[i];
			sumB += it.next();
		}
		assertEquals(freqs.getFrequencyTotal(), sumA);
		assertEquals(freqs.getFrequencyTotal(), sumB);
	}

	private double uniformBinaryEntropy(int n) {
		return Math.log(n)/Math.log(2);
	}

}
