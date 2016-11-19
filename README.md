# coding
A Java library for binary coding

Overview
--------

The coding library builds on the bits library, to provide a range
entropy codings, both universal and non. Full [javadocs are available][0].

### Universal codes

The library currently supports the following universal codings.

 * Elias Delta
 * Elias Omega
 * Fibonacci
 * Golomb / Rice
 * Unary

All universal codings support arbitrarily large numbers via
BigInteger.

### Non-universal codes

In addition to universal codings, the following non-universal codings
are supported.

 * Huffman
 * Truncated Binary

The Huffman implementation in particular has seen lots of work: it
accelerates encoding through canonicalization and block reads bits for
faster decoding (without a some degree of read-ahead, I’m not sure
there’s a faster approach). It can also return a dictionary which
contains the minimal state needed to reconstruct the encoding. This
can be efficiently transmitted ahead of a compressed message.

### Other features

A number of helpful classes are provided in addition to the core
coding implementations.

 * `ExtendedCoding` wraps a coding to provide canonical encoding of
   numerical values
 * `CodedReader` / `CodedWriter` conveniently pairs an ExtendedCoding with
   a BitReader/BitWriter
 * `CodedStreams` provides static utility methods for common encoding
   and decoding tasks.
 * `CharFrequencyRecorder` accumulates character frequencies from
   Strings and other sources of character data; useful for Huffman
   coding.
 * `CodingFrequencies` calculates ‘zero-order’ information entropy from
   data arrays.

Usage
-----

The coding library is available from the Maven central repository:

> Group ID:    `com.tomgibara.coding`
> Artifact ID: `coding`
> Version:     `1.0.1`

The Maven dependency being:

    <dependency>
      <groupId>com.tomgibara.coding</groupId>
      <artifactId>coding</artifactId>
      <version>1.0.1</version>
    </dependency>

Release History
---------------

**2016.11.19** Version 1.0.1

 * Built against newest dependency versions.
 * Java dependency promoted to Java 1.8

**2015.05.26** Version 1.0.0-j6

 *Java 6 compatible release*

**2015.05.25** Version 1.0.0

 *Initial release*

[0]: http://www.javadoc.io/doc/com.tomgibara.coding/coding