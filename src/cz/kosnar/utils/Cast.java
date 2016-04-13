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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Cast {
	private static final String SYS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	// TODO extend cast functions as required
	private Cast() { }

	public static boolean toBoolean(Object o) {
		if(o == null) {
			return false;
		}
		if(o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		}
		if(o instanceof Integer) {
			return ((Integer) o).intValue() != 0;
		}
		if(o instanceof Float) {
			return ((Float) o).intValue() != 0;
		}
		if(o instanceof Double) {
			return ((Double) o).intValue() != 0;
		}
		if(o instanceof String) {
			String v = (String) o;
			return v.matches("[t\\+T]|[tT][rR][uU][eE]|[+-]?[1-9][0-9]*");
		}
		throw new ClassCastException("Could not convert " + o.getClass().getName() + " to boolean.");
	}

	public static int toInt(Object o) {
		if(o instanceof Integer) {
			return ((Integer) o).intValue();
		}
		if(o instanceof Float) {
			return ((Float) o).intValue();
		}
		if(o instanceof Double) {
			return ((Double) o).intValue();
		}
		if(o instanceof String) {
			return toDouble(o).intValue();
		}
		throw new ClassCastException("Could not convert " + o.getClass().getName() + " to int.");
	}

	public static Integer toInteger(Object o) {
		if(o == null) {
			return null;
		}
		return Integer.valueOf(toInt(o));
	}

	public static Double toDouble(Object o) {
		if(o instanceof String) {
			return Double.valueOf(normalizeNumber((String) o));
		}
		throw new ClassCastException("Could not convert " + o.getClass().getName() + " to Double.");
	}

	public static String toString(int n) {
		return String.valueOf(n);
	}

	public static String toString(Object o) {
		if(o == null) {
			return null;
		}
		if(o instanceof Date) {
			return new SimpleDateFormat(SYS_DATE_FORMAT).format((Date) o);
		}
		if(o instanceof Exception) {
			Exception e = (Exception) o;
			StringBuilder sb = new StringBuilder(e.getMessage());
			sb.append('\n');
			for (StackTraceElement el : e.getStackTrace()) {
				sb.append("\tat ").append(el.getClassName()).append(".").append(el.getMethodName()).append("(").append(el.getFileName()).append(":").append(el.getLineNumber()).append(")\n");
			}
			return sb.toString();
		}
		return o.toString();
	}

	private static String normalizeNumber(String n) {
		n = n.trim();
		if(n.equalsIgnoreCase("null")) {
			return null;
		} else if(n.matches("[+\\-]?[0-9]+")) {
			// 123 format - nothing to do
		} else if(n.matches("[+\\-]?[0-9]*\\.[0-9]*")) {
			// 12.3 format - nothing to do
		} else if(n.matches("[+\\-]?[0-9]*,[0-9]*")) {
			// 12,3 format - replace decimal point
			n = n.replace(",", ".");
		} else if(n.matches("[+\\-]?([0-9]{1,3})([ ,][0-9]{3})+\\.[0-9]*")) {
			// 1 123.4 or 1,123.4 format - drop thousand separator
			n = n.replaceAll("[ ,]", "");
		} else if(n.matches("[+\\-]?([0-9]{1,3})([ \\.][0-9]{3})+,[0-9]*")) {
			// 1 123,4 or 1.123,4 format - drop thousand separator and replace decimal point
			n = n.replaceAll("[ \\.]", "").replace(",", ".");
		} else {
			throw new NumberFormatException("Invalid number format [" + n + "]");
		}
		return n;
	}
}
