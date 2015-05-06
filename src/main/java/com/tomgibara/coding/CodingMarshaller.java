package com.tomgibara.coding;

public interface CodingMarshaller<T> {

	int write(CodedWriter writer, T value);

	T read(CodedReader reader);

}
