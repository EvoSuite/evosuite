/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.stable;

public class Base64 {
    /**
     * Chunk size per RFC 2045 section 6.8.
     *
     * <p>The character limit does not count the trailing CRLF, but counts all other characters, including any
     * equal signs.</p>
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 6.8</a>
     */
    static final int CHUNK_SIZE = 76;

    /**
     * Chunk separator per RFC 2045 section 2.1.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 2.1</a>
     */
    static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();

    /**
     * The base length.
     */
    private static final int BASELENGTH = 255;

    /**
     * Used to calculate the number of bits in a byte.
     */
    private static final int EIGHTBIT = 8;

    /**
     * Used when encoding something which has fewer than 24 bits.
     */
    private static final int SIXTEENBIT = 16;

    /**
     * Used to determine how many bits data contains.
     */
    private static final int TWENTYFOURBITGROUP = 24;

    /**
     * Used to test the sign of a byte.
     */
    private static final int SIGN = -128;

    /**
     * Byte used to pad output.
     */
    private static final byte PAD = (byte) '=';

    /**
     * Contains the Base64 values <code>0</code> through <code>63</code> accessed by using character encodings as
     * indices.
     *
     * <p>For example, <code>base64Alphabet['+']</code> returns <code>62</code>.</p>
     *
     * <p>The value of undefined encodings is <code>-1</code>.</p>
     */
    private static final byte[] base64Alphabet;

    /**
     * <p>Contains the Base64 encodings <code>A</code> through <code>Z</code>, followed by <code>a</code> through
     * <code>z</code>, followed by <code>0</code> through <code>9</code>, followed by <code>+</code>, and
     * <code>/</code>.</p>
     *
     * <p>This array is accessed by using character values as indices.</p>
     *
     * <p>For example, <code>lookUpBase64Alphabet[62] </code> returns <code>'+'</code>.</p>
     */
    private static final byte[] lookUpBase64Alphabet;

    // Populating the lookup and character arrays
    static {
        base64Alphabet = new byte[BASELENGTH];
        lookUpBase64Alphabet = new byte[BASELENGTH];

        for (int i = 0; i <= 25; i++) {
            lookUpBase64Alphabet[i] = (byte) ('A' + i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('a' + j);
        }

        for (int i = 52, j = 0; i <= 61; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('0' + j);
        }

        lookUpBase64Alphabet[62] = (byte) '+';
        lookUpBase64Alphabet[63] = (byte) '/';

        for (int i = 0; i < BASELENGTH; i++) {
            base64Alphabet[i] = (byte) -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            base64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            base64Alphabet[i] = (byte) (i - 'a' + 26);
        }
        for (int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i - '0' + 52);
        }

        base64Alphabet['+'] = 62;
        base64Alphabet['/'] = 63;

    }


//	private static byte[] makeLookUpBase64Alphabet() {
//
//		byte[] lookUpBase64Alphabet = new byte[BASELENGTH];
//
//		for (int i = 0; i <= 25; i++) {
//			lookUpBase64Alphabet[i] = (byte) ('A' + i);
//		}
//
//		for (int i = 26, j = 0; i <= 51; i++, j++) {
//			lookUpBase64Alphabet[i] = (byte) ('a' + j);
//		}
//
//		for (int i = 52, j = 0; i <= 61; i++, j++) {
//			lookUpBase64Alphabet[i] = (byte) ('0' + j);
//		}
//
//		lookUpBase64Alphabet[62] = (byte) '+';
//		lookUpBase64Alphabet[63] = (byte) '/';
//
//		return lookUpBase64Alphabet;
//	}

    /**
     * Encodes binary data using the base64 algorithm, optionally chunking the output into 76 character blocks.
     *
     * @param binaryData Array containing binary data to encodeToChars.
     * @return Base64-encoded data.
     * @throws IllegalArgumentException Thrown when the input array needs an output array bigger than {@link Integer#MAX_VALUE}
     */
    private static byte[] encode(byte[] binaryData) {

        boolean isChunked = true;

        long binaryDataLength = binaryData.length;
        long lengthDataBits = binaryDataLength * EIGHTBIT;
        long fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
        long tripletCount = lengthDataBits / TWENTYFOURBITGROUP;
        long encodedDataLengthLong = 0;
        int chunckCount = 0;

        if (fewerThan24bits != 0) {
            // data not divisible by 24 bit
            encodedDataLengthLong = (tripletCount + 1) * 4;
        } else {
            // 16 or 8 bit
            encodedDataLengthLong = tripletCount * 4;
        }

        // If the output is to be "chunked" into 76 character sections,
        // for compliance with RFC 2045 MIME, then it is important to
        // allow for extra length to account for the separator(s)
        if (isChunked) {

            chunckCount = (CHUNK_SEPARATOR.length == 0 ? 0 : (int) Math
                    .ceil((float) encodedDataLengthLong / CHUNK_SIZE));
            encodedDataLengthLong += chunckCount * CHUNK_SEPARATOR.length;
        }

        if (encodedDataLengthLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Input array too big, output array would be bigger than Integer.MAX_VALUE="
                            + Integer.MAX_VALUE);
        }
        int encodedDataLength = (int) encodedDataLengthLong;
        byte[] encodedData = new byte[encodedDataLength];

        byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

        int encodedIndex = 0;
        int dataIndex = 0;
        int i = 0;
        int nextSeparatorIndex = CHUNK_SIZE;
        int chunksSoFar = 0;

        // log.debug("number of triplets = " + numberTriplets);
        for (i = 0; i < tripletCount; i++) {
            dataIndex = i * 3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            // log.debug("b1= " + b1 +", b2= " + b2 + ", b3= " + b3);

            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                    : (byte) ((b2) >> 4 ^ 0xf0);
            byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6)
                    : (byte) ((b3) >> 6 ^ 0xfc);

            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            // log.debug( "val2 = " + val2 );
            // log.debug( "k4 = " + (k<<4) );
            // log.debug( "vak = " + (val2 | (k<<4)) );
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2
                    | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[(l << 2)
                    | val3];
            encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 0x3f];

            encodedIndex += 4;

            // If we are chunking, let's put a chunk separator down.
            if (isChunked) {
                // this assumes that CHUNK_SIZE % 4 == 0
                if (encodedIndex == nextSeparatorIndex) {
                    System.arraycopy(CHUNK_SEPARATOR, 0, encodedData,
                            encodedIndex, CHUNK_SEPARATOR.length);
                    chunksSoFar++;
                    nextSeparatorIndex = (CHUNK_SIZE * (chunksSoFar + 1))
                            + (chunksSoFar * CHUNK_SEPARATOR.length);
                    encodedIndex += CHUNK_SEPARATOR.length;
                }
            }
        }

        // form integral number of 6-bit groups
        dataIndex = i * 3;

        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) (b1 & 0x03);
            // log.debug("b1=" + b1);
            // log.debug("b1<<2 = " + (b1>>2) );
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);
            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k << 4];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == SIXTEENBIT) {

            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                    : (byte) ((b2) >> 4 ^ 0xf0);

            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2
                    | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2];
            encodedData[encodedIndex + 3] = PAD;
        }

        if (isChunked) {
            // we also add a separator to the end of the final chunk.
            if (chunksSoFar < chunckCount) {
                System.arraycopy(CHUNK_SEPARATOR, 0, encodedData,
                        encodedDataLength - CHUNK_SEPARATOR.length,
                        CHUNK_SEPARATOR.length);
            }
        }

        return encodedData;
    }

    public static byte[] test1() {
        byte[] byteArray0 = new byte[2];
        byte byte0 = (byte) (-1);
        byteArray0[0] = byte0;
        byte[] byteArray1 = Base64.encode(byteArray0);
        return byteArray1;
    }

    public static byte[] test2() {
        byte[] byteArray0 = new byte[8];
        byte byte0 = (byte) (-111);
        byteArray0[0] = byte0;
        byte[] byteArray1 = Base64.encode(byteArray0);
        return byteArray1;
    }

}
