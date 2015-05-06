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



public class UnaryCodingTest extends ExtendedCodingTest<ExtendedCoding> {

	@Override
	Iterable<ExtendedCoding> getCodings() {
		return Arrays.asList(UnaryCoding.oneExtended, UnaryCoding.zeroExtended);
	}

	// put a limit on it, otherwise encodings take too much memory
	@Override
	int getMaxEncodableValue(ExtendedCoding coding) {
		return 100;
	}

}