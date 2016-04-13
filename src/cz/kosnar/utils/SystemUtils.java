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

import java.io.File;
import java.io.IOException;

public class SystemUtils {
	private SystemUtils() {}

	public static File getSubdirByNameIgnoreCase(File baseDir, String path) throws IOException {
		if(baseDir == null)
			throw new IOException("Base directory is null.");
		if(!baseDir.exists())
			throw new IOException("Base directory not exists.");
		if(!baseDir.isDirectory())
			throw new IOException("Base directory is not directory.");
		if(StringUtils.isBlank(path))
			throw new IOException("Invalid sub path.");

		String[] dirs = path.toLowerCase().split("[\\\\/]");
		File tmp = baseDir;
		vnejsi:for(String dir : dirs) {
			for(File f : tmp.listFiles()) {
				if(f.getName().toLowerCase().equals(dir)) {
					if(f.isDirectory()) {
						tmp = f;
						continue vnejsi;
					} else {
						throw new IOException(f.getAbsolutePath() + " is not directory.");
					}
				}
			}
			throw new IOException(tmp.getAbsolutePath() + "/" + dir + " not found.");
		}
		return tmp;
	}
}
