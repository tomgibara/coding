# coding
A Java library for binary coding

## Overview

The coding library builds on the bits library, to provide a range
entropy codings, both universal and non.

### Universal codes

The library currently supports the following universal codings.

 * Elias Delta
 * Elias Omega
 * Fibonacci
 * Golomb / Rice
 * Unary

All universal codings support arbitrarily large numbers via BigInteger.

### Non-universal codes

In addition to universal codings, the following non-universal codings are supported.

 * Huffman
 * Truncated Binary

The Huffman implementation in particular has seen lots of work: it accelerates encoding through canonicalization and block reads bits for faster decoding (without a some degree of read-ahead, I’m not sure there’s a faster approach). It can also return a dictionary which contains the minimal state needed to reconstruct the encoding. This can be efficiently transmitted ahead of a compressed message.

### Other features

A number of helpful classes are provided in addition to the core coding implementations.

 * ExtendedCoding wraps a coding to provide canonical encoding of numerical values
 * CodedReader / CodedWriter conveniently pairs an ExtendedCoding with a BitReader/BitWriter
 * CodedStreams provides static utility methods for common encoding and decoding tasks.
 * CharFrequencyRecorder accumulates character frequencies from Strings and other sources of character data; useful for Huffman coding.
 * CodingFrequencies calculates ‘zero-order’ information entropy from data arrays.
