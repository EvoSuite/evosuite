/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package ncs;

public class Encoder
{
	public String exe(byte[] octetString) 
	{	
		final char[] ALPHABET = {
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',  //  0 to  7
				'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',  //  8 to 15
				'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  // 16 to 23
				'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  // 24 to 31
				'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  // 32 to 39
				'o', 'p', 'q', 'r', 's', 't', 'u', 'v',  // 40 to 47
				'w', 'x', 'y', 'z', '0', '1', '2', '3',  // 48 to 55
				'4', '5', '6', '7', '8', '9', '+', '/'}; // 56 to 63

		
		int bits24 =0;
		int bits6 =0;

		char[] out = new char[((octetString.length - 1) / 3 + 1) * 4];
		int outIndex = 0;
		int i = 0;

		while ((i + 3) <= octetString.length) {
			// store the octets
			bits24 = (octetString[i++] & 0xFF) << 16;
			bits24 |= (octetString[i++] & 0xFF) << 8;
			bits24 |= octetString[i++];

			bits6 = (bits24 & 0x00FC0000) >> 18;
			out[outIndex++] = ALPHABET[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			out[outIndex++] = ALPHABET[bits6];
			bits6  = (bits24 & 0x00000FC0) >> 6;
		out[outIndex++] = ALPHABET[bits6];
		bits6 = (bits24 & 0x0000003F);
		out[outIndex++] = ALPHABET[bits6];
		}
		if (octetString.length - i == 2) {
			// store the octets
			bits24 = (octetString[i] & 0xFF) << 16;
			bits24 |= (octetString[i + 1] & 0xFF) << 8;
			bits6 = (bits24 & 0x00FC0000) >> 18;
			out[outIndex++] = ALPHABET[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			out[outIndex++] = ALPHABET[bits6];
			bits6 = (bits24 & 0x00000FC0) >> 6;
			out[outIndex++] = ALPHABET[bits6];

			// padding
			out[outIndex++] = '=';
		} else if (octetString.length - i == 1) {
			// store the octets
			bits24 = (octetString[i] & 0xFF) << 16;
			bits6 = (bits24 & 0x00FC0000) >> 18;
			out[outIndex++] = ALPHABET[bits6];
			bits6 = (bits24 & 0x0003F000) >> 12;
			out[outIndex++] = ALPHABET[bits6];

			// padding
			out[outIndex++] = '=';
			out[outIndex++] = '=';
		}
		return new String(out);
	}
}