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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.kosnar.utils.ChangeListener;
import cz.kosnar.utils.Subprocess;

public class Convertor extends Thread implements ChangeListener {

	private SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH''mm''ss");

	private Pattern p = Pattern.compile(".*, ([0-9\\.]+)[ ]*%(.*avg ([0-9\\.]+) fps, ETA ([0-9]+)h([0-9]+)m([0-9]+)s.*)?");
	private ChangeListener listener;
	private ArrayList<MPLItem> queue;
	private String target, name;
	private int count = 0, current = 0;
	private Integer quality = Integer.valueOf(21);
	private x264 set;
	private Subprocess subprocess;

	private Convertor(ArrayList<MPLItem> queue, String target, Integer quality, x264 set, ChangeListener listener) {
		this.count = queue.size();
		this.queue = queue;
		this.target = target;
		this.listener = listener;
		this.quality = quality;
		this.set = set;
	}

	public static Convertor convert(ArrayList<MPLItem> in, String target, Integer quality, x264 set, ChangeListener listener) {
		if(in == null || in.size() == 0) {
			listener.done();
			return null;
		}
		Convertor c = new Convertor(in, target, quality, set, listener);
		c.start();
		return c;
	}

	@SuppressWarnings("boxing")
	@Override
	public void run() {
		String[] cmd;
		MPLItem item;
		for( ; current < count; ) {
			item = queue.get(current++);
			name = getFileName(target, item);
			cmd = set.buildCommand(quality, item.stream, name);
			if(listener != null) {
				listener.status(String.format("%d/%d zahajuji konverzi %s", current, count, name));
				listener.error(null);
			}
			synchronized(this) {
				subprocess = new Subprocess(cmd, this, true);
			}
			try {
				subprocess.join();
			} catch (InterruptedException e) { }
			if(listener != null) {
				listener.status(String.format("%d/%d konverze hotova %s", current, count, name));
				listener.error(null);
			}
		}
		if(listener != null) {
			listener.done();
		}
	}

	public void terminate() {
		synchronized(this) {
			current = Integer.MAX_VALUE;
			listener = null;
			if(subprocess != null) {
				subprocess.terminate();
			}
		}
	}

	private String getFileName(String target, MPLItem item) {
		StringBuffer sb = new StringBuffer(target);
		sb.append("/").append(ft.format(item.dateTime)).append(".mkv");
		return sb.toString().replaceAll("//", "/");
	}

	@Override
	public void status(String d) {
		Matcher m = p.matcher(d);
		StringBuilder sb = new StringBuilder();
		sb.append(current).append('/').append(count).append(": ");
		if(m.find()) {
			sb.append(m.group(1)).append("% ");
			if(m.group(2) != null) {
				sb.append(m.group(4)).append("h ");
				sb.append(m.group(5)).append("m ");
				sb.append(m.group(6)).append("s (");
				sb.append(m.group(3));
			} else {
				sb.append("__h __m __s (__.__");
			}
			sb.append("fps) ").append(name);
			if(listener != null) {
				listener.status(sb.toString());
			}
		}
	}

	@Override
	public void error(String msg) {
//		if(listener != null) {
//			listener.error(msg);
//		}
	}

	@Override
	public void done() {}
}
