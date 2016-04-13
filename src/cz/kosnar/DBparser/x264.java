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

public enum x264 {
	defaults (" -x ref=1:rc-lookahead=10:trellis=0:me=umh:subme=7:b-adapt=2"),
	ultrafast, superfast, veryfast, faster, fast, medium, slow, slower, veryslow;
	;

	private static final String BASE = "HandBrakeCLI -f mkv -m --vfr -E copy -8 medium -e x264 -q %d %s";
	private String params;

	private x264() {
		params = " --x264-preset " + name();
	}

	private x264(String params) {
		this.params = params;
	}

	public String[] buildCommand(Integer quality, String in, String out) {
		String[] cmd = String.format(BASE, quality, params).split("[ ]+"),
				 res = new String[cmd.length + 4];
		System.arraycopy(cmd, 0, res, 0, cmd.length);
		System.arraycopy(new String[]{"-i", in, "-o", out}, 0, res, cmd.length, 4);
		return res;
	}
}
