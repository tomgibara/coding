package com.tomgibara.coding;

//TODO is this a worthwhile interface to have in this package? Consider
// don't expose it until we decide.
interface CodingMarshaller<T> {

	int write(CodedWriter writer, T value);

	T read(CodedReader reader);

}
