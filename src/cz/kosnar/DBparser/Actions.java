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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import cz.kosnar.utils.Cast;

public class Actions {
	private Actions() { }

	public static TreeMap<String, Object> loadConfig(String fileName) {
		TreeMap<String, Object> cfg = new TreeMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String[] pair;
			String tmp;
			while (br.ready()) {
				tmp = br.readLine().trim();
				if(tmp.length() != 0 && tmp.charAt(0) != '#') {
					//skip empty and # starting lines
					pair = tmp.split("[\\s]*=[\\s]*", 2);
					cfg.put(pair[0], pair.length > 1 ? pair[1] : null);
				}
			}
			br.close();
		} catch (IOException e) {
			System.err.println(Cast.toString(e));
		}
		return cfg;
	}

	public static boolean updateConfig(TreeMap<String, Object> cfg, String fileName) {
		try {
			FileWriter fw = new FileWriter(fileName, false);
			for(String k : cfg.keySet()) {
				fw.append(k);
				fw.append('=');
				fw.append(Cast.toString(cfg.get(k)));
				fw.append('\n');
			}
			fw.close();
		} catch (IOException e) {
			System.err.println(Cast.toString(e));
			return false;
		}
		return true;
	}
}