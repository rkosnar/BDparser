package cz.kosnar.DBparser;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

import cz.kosnar.utils.ByteUtils;
import cz.kosnar.utils.SystemUtils;

public class MPLreader {
	private static final String PLAYLIST_PATH = "PRIVATE/AVCHD/BDMV/PLAYLIST",
								STREAM_PATH = "PRIVATE/AVCHD/BDMV/STREAM";
	private static final String[] AUDIOS = new String[0];
	private static final int FF = 65535;

	public TreeMap<String, MPL> mpls = new TreeMap<String, MPL>();

	public static boolean containsPlaylist(File baseDir) {
		return containsPath(baseDir, PLAYLIST_PATH);
	}

	public static boolean containsSteams(File baseDir) {
		return containsPath(baseDir, STREAM_PATH);
	}

	private static boolean containsPath(File baseDir, String path) {
		try {
			return null != SystemUtils.getSubdirByNameIgnoreCase(baseDir, path);
		} catch (IOException e) { }
		return false;
	}

	public MPLreader(File baseDir) throws IOException {
		File	playlist = SystemUtils.getSubdirByNameIgnoreCase(baseDir, PLAYLIST_PATH),
				stream = SystemUtils.getSubdirByNameIgnoreCase(baseDir, STREAM_PATH);
		String  streamDir = stream.getAbsolutePath();
		if(!streamDir.endsWith("/")) {
			streamDir += "/";
		}

		for(File f : playlist.listFiles()) {
			mpls.put(f.getName(), parseFile(f, streamDir));
		}
	}

	private MPL parseFile(File f, String streamDir) throws IOException {
		int index;
		byte[] data = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
		MPL mpl = new MPL();

		// get base structure
		mpl.version = new String(data, 0, 8);
		int secA = ByteUtils.byteToInt(data, 8, 12),
			secB = ByteUtils.byteToInt(data, 12, 16),
			secC = ByteUtils.byteToInt(data, 16, 20),

			lenA = ByteUtils.byteToInt(data, secA, secA + 4),
			lenB = ByteUtils.byteToInt(data, secB, secB + 4),
			lenC = ByteUtils.byteToInt(data, secC, secC + 4);

		if(secB != secA + lenA + 4)
			throw new RuntimeException("Parsing error: length_A not match with section_B....corupted file?");
		if(secC != secB + lenB + 4)
			throw new RuntimeException("Parsing error: length_B not match with section_C....corupted file?");
		if(data.length != secC + lenC + 4)
			throw new RuntimeException("Parsing error: length_C not match with EOF....corupted file?");

		secA += 4;
		secB += 4;
		secC += 4;

		// TODO identify bytes form <20> to <secA>

		// parse Section A
		index = secA;
		int count = ByteUtils.byteToInt(data, index, index + 4);
		MPLItem[] items = new MPLItem[count];
		mpl.items = items;
		MPLItem tmp;
		index = secA + 6;
		for(int i = 0; i < count; i++) {
			items[i] = new MPLItem();
			tmp = items[i];
			int j, length = ByteUtils.byteToInt(data, index, index + 2);
			index += 2;
			j = index + 77;
			tmp.stream = checkFile(streamDir + new String(data, index, 9));

			// TODO identify bytes form <index + 9> to <index + 77>

			ArrayList<String> audiocodes = new ArrayList<>();
			while (true) {
				audiocodes.add(new String(data, j, 3));
				if(data[j + 3] == 0)
					break;
				j += 15;

				// TODO identify bytes form <j + 3> to <j + 15>

			}

			tmp.audioTrac = audiocodes.toArray(AUDIOS);

			index += length;
		}

		// parse Section B
		// TODO identify bytes form <secB> to <secB + lenB> ... whole section B

		// parse Section C
		// TODO identify bytes form <secC> to <secC + 344> ... header of section C, pointers to parts, Last modify date

		index = secC + 344;
		@SuppressWarnings("unused")
		int len = index + ByteUtils.byteToInt(data, index, index + 4) + 4;
		index += 4;

		int i = 0;
		do {
			int j = index;
			// TODO identify bytes form <j> to <j + 6>
			j += 6;
			// TODO identify bytes form <j> to <j + 4>
			j += 4;
			int seq = ByteUtils.byteToInt(data, j, j + 2);
			if(seq != FF) {
				//TODO check seq to file name

				j += 2;
				// TODO identify byte <j>
				j++;
				// parse 7B as date time, 4bit per number, yyyyMMddHHmmss
				int y = ByteUtils.halfBytesToInt(data[j++]) * 100 + ByteUtils.halfBytesToInt(data[j++]),
					M = ByteUtils.halfBytesToInt(data[j++]),
					d = ByteUtils.halfBytesToInt(data[j++]),
					H = ByteUtils.halfBytesToInt(data[j++]),
					m = ByteUtils.halfBytesToInt(data[j++]),
					s = ByteUtils.halfBytesToInt(data[j++]);
				Calendar c = Calendar.getInstance();
				c.set(y, M - 1, d, H, m, s);
				items[i].dateTime = c.getTime();

				// next 42B is some metadata

				// 4B 'FF FF' separator

				i++;
			}
			index += 66;
		} while (i < count);
		//TODO check index equal to len

		return mpl;
	}

	private String checkFile(String file) {
		try {
			File tmp = new File(file);
			if(tmp.exists()) {
				return file;
			}
			tmp = new File(file.replaceAll("M2TS$", ".M2TS"));
			if(tmp.exists()) {
				return tmp.getCanonicalPath();
			}
			tmp = new File(file.replaceAll("M2TS$", ".MTS"));
			if(tmp.exists()) {
				return tmp.getCanonicalPath();
			}
			tmp = new File(file.replaceAll("M2TS$", ".TS"));
			if(tmp.exists()) {
				return tmp.getCanonicalPath();
			}
		} catch (IOException e) { }
		return file;
	}
}
