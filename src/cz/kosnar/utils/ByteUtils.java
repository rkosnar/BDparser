package cz.kosnar.utils;
/**
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

public class ByteUtils {
	private ByteUtils() {}

	public static int byteToInt(byte[] num) {
		int i = 0;
		for(byte b : num) {
			i <<= 8;
			i += b;
		}
		return i;
	}

	public static int byteToInt(byte[] num, int from, int to) {
		int i = 0;
		for( ; from < to; from++) {
			i <<= 8;
			i += num[from] & 0xFF;
		}
		return i;
	}

	public static int halfBytesToInt(byte nums) {
		int a = nums & 0xF0, b = nums & 0x0F;
		a >>= 4;
		return a * 10 + b;
	}
}
