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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.tomgibara.bits.BitStreamException;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.InputStreamBitReader;
import com.tomgibara.bits.OutputStreamBitWriter;

/**
 * Provides static methods for reading/writing data from/to bit streams using
 * {@link CodedReader}s/{@link CodedWriter}s.
 *
 * @author Tom Gibara
 *
 */

public final class CodedStreams {

	/**
	 * Implementations of this interface may be used to encapsulate an operation
	 * that writes to a {@link CodedWriter}.
	 */

	public interface WriteTask {

		/**
		 * Called to write data to a {@link CodedWriter}.
		 *
		 * @param writer to which data should be written
		 */

		void writeTo(CodedWriter writer);

	}

	/**
	 * Implementations of this interface may be used to encapsulate an operation
	 * that reads from a {@link CodedReader}.
	 */

	public interface ReadTask {

		/**
		 * Called to read data from a {@link CodedWriter}.
		 *
		 * @param writer to which data should be written
		 */

		void readFrom(CodedReader reader);

	}

	/**
	 * Writes a string to a coded writer. The length is written, followed by
	 * each character in the String.
	 *
	 * @param writer
	 *            the writer to which values will be written
	 * @param str
	 *            the string to write, not null
	 * @return the number of bits written
	 */

	public static int writeString(CodedWriter writer, String str) {
		int len = str.length();
		int c = writer.writePositiveInt(len);
		//TODO consider writing as UTF-8 instead
		for (int i = 0; i < len; i++) {
			c += writer.writePositiveInt(str.charAt(i));
		}
		return c;
	}

	/**
	 * Reads a string from a coded writer. The length is read, followed by each
	 * character in the String.
	 *
	 * @param reader
	 *            the reader from which values will be read
	 * @return the string read, never null
	 */

	public static String readString(CodedReader reader) {
		int len = reader.readPositiveInt();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append((char) (reader.readPositiveInt()));
		}
		return sb.toString();
	}

	/**
	 * Writes an array of primitives to a coded writer. The length is written,
	 * followed by each element of the array.
	 *
	 * @param writer
	 *            the writer to which values will be written
	 * @param array
	 *            the array to write, not null
	 * @return the number of bits written
	 */

	public static int writePrimitiveArray(CodedWriter writer, Object array) {
		if (array == null) throw new IllegalArgumentException("null array");
		Class<?> clss = array.getClass();
		if (!clss.isArray()) throw new IllegalArgumentException("not an array");
		Class<?> comp = clss.getComponentType();
		if (!comp.isPrimitive()) throw new IllegalArgumentException("array components not primitives");

		int length = Array.getLength(array);
		writer.writePositiveInt(length);
		//TODO would love a switch statement here
		int c = 0;
		if (comp == boolean.class) {
			BitWriter w = writer.getWriter();
			boolean[] a = (boolean[]) array;
			for (int i = 0; i < a.length; i++) c += w.writeBoolean(a[i]);
		} else if (comp == byte.class) {
			byte[] a = (byte[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeInt(a[i]);
		} else if (comp == short.class) {
			short[] a = (short[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeInt(a[i]);
		} else if (comp == int.class) {
			int[] a = (int[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeInt(a[i]);
		} else if (comp == long.class) {
			long[] a = (long[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeLong(a[i]);
		} else if (comp == float.class) {
			//TODO add float
			float[] a = (float[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeDouble(a[i]);
		} else if (comp == double.class) {
			double[] a = (double[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writeDouble(a[i]);
		} else if (comp == char.class) {
			char[] a = (char[]) array;
			for (int i = 0; i < a.length; i++) c += writer.writePositiveInt(a[i]);
		} else {
			throw new UnsupportedOperationException("unsupported primitive type " + comp.getName());
		}
		return c;
	}

	/**
	 * Reads an array of ints from a coded writer. The length is read, followed
	 * by each int in the array.
	 *
	 * @param reader
	 *            the reader from which the array will be read
	 * @return the array read, never null
	 */

	public static int[] readIntArray(CodedReader reader) {
		int length = reader.readPositiveInt();
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = reader.readInt();
		}
		return a;
	}

	//TODO add other methods
	/**
	 * Reads an array of longs from a coded writer. The length is read, followed
	 * by each long in the array.
	 *
	 * @param reader
	 *            the reader from which the array will be read
	 * @return the array read, never null
	 */

	public static long[] readLongArray(CodedReader reader) {
		int length = reader.readPositiveInt();
		long[] a = new long[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = reader.readLong();
		}
		return a;
	}

	/**
	 * Writes an array of Strings to a coded writer. The length is written,
	 * followed by every String in the array as by the
	 * {@link #writeString(CodedWriter, String)} method.
	 *
	 * @param writer
	 *            the writer to which values will be written
	 * @param array
	 *            the array to write, not null
	 * @return the number of bits written
	 */

	public static int writeStringArray(CodedWriter writer, String[] array) {
		int len = array.length;
		int c = writer.writePositiveInt(len);
		for (int i = 0; i < len; i++) {
			c += writeString(writer, array[i]);
		}
		return c;
	}

	/**
	 * Reads an array of Strings from a coded writer. The length is read,
	 * followed by each String in the array as by the
	 * {@link #readString(CodedReader)} method.
	 *
	 * @param reader
	 *            the reader from which the array will be read
	 * @return the array read, never null
	 */

	public static String[] readStringArray(CodedReader reader) {
		int len = reader.readPositiveInt();
		String[] array = new String[len];
		for (int i = 0; i < len; i++) {
			array[i] = readString(reader);
		}
		return array;
	}

	/**
	 * Writes an array of enums to a coded writer. The length is written,
	 * followed by the ordinal of every enum in the array.
	 *
	 * @param writer
	 *            the writer to which values will be written
	 * @param array
	 *            the array to write, not null
	 * @return the number of bits written
	 */

	public static <E extends Enum<?>> int writeEnumArray(CodedWriter writer, E[] enums) {
		int length = enums.length;
		writer.writePositiveInt(length);
		int c = 0;
		for (int i = 0; i < length; i++) {
			c += writer.writePositiveInt(enums[i].ordinal());
		}
		return c;
	}

	/**
	 * Reads an array of enums from a coded writer. The length is read, followed
	 * by the ordinal of each enum in the array.
	 *
	 * @param reader
	 *            the reader from which the array will be read
	 * @return the array read, never null
	 */

	@SuppressWarnings("unchecked")
	public static <E extends Enum<?>> E[] readEnumArray(CodedReader reader, Class<E> enumClass) {
		if (enumClass == null) throw new IllegalArgumentException("null enumClass");
		E[] values = enumClass.getEnumConstants();
		if (values == null) throw new IllegalArgumentException("not an enum class");
		int length = reader.readPositiveInt();
		E[] a = (E[]) Array.newInstance(enumClass, length);
		for (int i = 0; i < length; i++) {
			a[i] = values[reader.readInt() - 1];
		}
		return a;
	}

	/**
	 * Writes a list of enums to a coded writer. The size of the list is is
	 * written, followed by the ordinal of every enum in the array.
	 *
	 * @param writer
	 *            the writer to which values will be written
	 * @param list
	 *            a list of enums containing no nulls
	 * @return the number of bits written
	 */

	public static <E extends Enum<?>> int writeEnumList(CodedWriter writer, List<E> list) {
		int length = list.size();
		writer.writePositiveInt(length);
		int c = 0;
		for (E e : list) c += writer.writePositiveInt(e.ordinal());
		return c;
	}

	/**
	 * Reads a list of enums from a coded writer. The size of the list is read,
	 * followed by the ordinal of each enum in the list.
	 *
	 * @param reader
	 *            the reader from which the array will be read
	 * @return the list read, never null
	 */

	public static <E extends Enum<?>> List<E> readEnumList(CodedReader reader, Class<E> enumClass) {
		if (enumClass == null) throw new IllegalArgumentException("null enumClass");
		E[] values = enumClass.getEnumConstants();
		if (values == null) throw new IllegalArgumentException("not an enum class");
		int length = reader.readPositiveInt();
		List<E> list = new ArrayList<E>(length);
		for (int i = 0; i < length; i++) list.add( values[reader.readPositiveInt()] );
		return list;
	}

	/**
	 * Writes data to a file using a specified coding.
	 *
	 * @param task
	 *            writes the data values
	 * @param coding
	 *            performs the encoding of the values
	 * @param file
	 *            stores the values
	 * @throws BitStreamException
	 *             if an I/O problem occurs.
	 */

	public static void writeToFile(WriteTask task, ExtendedCoding coding, File file) {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file), 1024);
			BitWriter writer = new OutputStreamBitWriter(out);
			CodedWriter coded = new CodedWriter(writer, coding);
			task.writeTo(coded);
			writer.flush();
		} catch (IOException e) {
			throw new BitStreamException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads data from a file using a specified coding.
	 *
	 * @param task
	 *            reads the data values
	 * @param coding
	 *            performs the decoding of the values
	 * @param file
	 *            stores the values
	 * @throws BitStreamException
	 *             if an I/O problem occurs.
	 */

	public static void readFromFile(ReadTask task, ExtendedCoding coding, File file) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file), 1024);
			InputStreamBitReader reader = new InputStreamBitReader(in);
			CodedReader coded = new CodedReader(reader, coding);
			task.readFrom(coded);
		} catch (IOException e) {
			throw new BitStreamException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private CodedStreams() {
	}

}
