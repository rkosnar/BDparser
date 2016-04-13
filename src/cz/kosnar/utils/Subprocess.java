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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Subprocess extends Thread {

	private String[] cmd;
	private ChangeListener listener;
	private Process proc;

	public Subprocess(String cmd, ChangeListener listener, boolean async) {
		this(cmd.split(" "), listener, async);
	}

	public Subprocess(String[] cmd, ChangeListener listener, boolean async) {
		this.cmd = cmd;
		this.listener = listener;
		this.start();
		if(!async) {
			try {
				this.join();
			} catch (InterruptedException e) {
				listener.error("Neočekávaná chyba.");
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		proc.destroy();
		try {
			this.join();
			listener.error("Zpracování přerušeno.");
		} catch (InterruptedException e) {
			listener.error("Neočekávaná chyba.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			proc = rt.exec(cmd);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
			BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String stdOut, stdErr = null;
			while ((stdOut = readLine(out)) != null || (stdErr = readLine(err)) != null) {
				if(stdOut != null) {
					listener.status(stdOut);
				}
				stdOut = null;
				if(stdErr != null) {
					listener.error(stdErr);
				}
				stdErr = null;
			}
		} catch (IOException e) {
			listener.error("Neočekávaná chyba.");
			e.printStackTrace();
		}
		listener.done();
	}

	private String readLine(BufferedReader br) {
		if(br != null) {
			try {
				return br.readLine();
			} catch (IOException e) { }
		}
		return null;
	}
}
