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

import java.math.BigInteger;

import com.tomgibara.bits.BitWriter;

/**
 * Universal codings are capable of encoding any non-negative whole number, no
 * matter how large. Naturally, resource limitations may impede the coding of
 * extremely large numbers.
 *
 * @author Tom Gibara
 *
 */

public abstract class UniversalCoding implements Coding {

	abstract int unsafeEncodePositiveInt(BitWriter writer, int value);

	abstract int unsafeEncodePositiveLong(BitWriter writer, long value);

	abstract int unsafeEncodePositiveBigInt(BitWriter writer, BigInteger value);

	@Override
	public int encodePositiveInt(BitWriter writer, int value) {
		if (value < 0) throw new IllegalArgumentException("negative value");
		return unsafeEncodePositiveInt(writer, value);
	}

	@Override
	public int encodePositiveLong(BitWriter writer, long value) {
		if (value < 0L) throw new IllegalArgumentException("negative value");
		return unsafeEncodePositiveLong(writer, value);
	}

	@Override
	public int encodePositiveBigInt(BitWriter writer, BigInteger value) {
		if (value == null) throw new IllegalArgumentException("null value");
		if (value.signum() < 0) throw new IllegalArgumentException("negative value");
		return unsafeEncodePositiveBigInt(writer, value);
	}

}
