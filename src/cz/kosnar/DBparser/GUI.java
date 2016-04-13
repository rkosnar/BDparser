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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import cz.kosnar.utils.Cast;
import cz.kosnar.utils.ChangeListener;
import cz.kosnar.utils.StringUtils;
import cz.kosnar.utils.Subprocess;


@SuppressWarnings("serial")
public class GUI extends JFrame implements ComponentListener, ChangeListener {
	private static final int	MIN_HEIGHT = 480,
								MIN_WIDTH = 640;

	private static final String	WIN_WIDTH = "WIN_WIDTH",
								WIN_HEIGHT = "WIN_HEIGHT",
								WIN_LEFT = "WIN_LEFT",
								WIN_TOP = "WIN_TOP",

								LAST_SOURCE = "LAST_SOURCE",
								LAST_TARGET = "LAST_TARGET",

								QUALITY = "quality",
								PRESET = "preset";

	public static final String[] KNOW_KEYS = {
		WIN_WIDTH, WIN_HEIGHT, WIN_LEFT, WIN_TOP, LAST_SOURCE, LAST_TARGET,
		QUALITY, PRESET
	};

	private static final String[] HEADER = {
		"", "Datum", "Délka"
	};
	private static final String CFG = ".BDparser";

	private SimpleDateFormat cdf = new SimpleDateFormat("dd.MMMM.yyyy HH:mm:ss"),
							 sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private TreeMap<String, Object> params;

	private JLabel source, target, status;
	private JSpinner quality;
	private JComboBox<x264> preset;
	private JButton srcBtn, trgBtn, process;
	private JPanel mainPane, statusPane;
	private JTable playlist;
	private DefaultTableModel plModel;

	private Insets okraj = new Insets(5, 5, 5, 5);

	private boolean working = false;

	private ArrayList<MPLItem> mpls = new ArrayList<MPLItem>();
	private StringBuffer history = new StringBuffer("<html><div style=\"background: #ffffff;\">");

	private int info = -1;

	private Convertor convertor;

	public GUI() {
		super("BD convert");

		setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());

		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
	    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		params = loadConfig();
		setSize(Cast.toInt(params.get(WIN_WIDTH)), Cast.toInt(params.get(WIN_HEIGHT)));
		setLocation(Cast.toInt(params.get(WIN_LEFT)), Cast.toInt(params.get(WIN_TOP)));
		addComponentListener(this);

		setLayout(new BorderLayout(5, 5));

		mainPane = new JPanel(new GridBagLayout());
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 1));

		source = new JLabel(Cast.toString(params.get(LAST_SOURCE)));
		source.setBackground(Color.white);
		source.setOpaque(true);
		add(mainPane, source, 0, 0, 3, 1);

		srcBtn = new JButton("Vybrat");
		srcBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDirDialog(LAST_SOURCE, Cast.toString(params.get(LAST_SOURCE)));
			}
		});
		add(mainPane, srcBtn, 3, 0, 1, 1);

		target = new JLabel(Cast.toString(params.get(LAST_TARGET)));
		target.setBackground(Color.white);
		target.setOpaque(true);
		add(mainPane, target, 0, 1, 3, 1);

		trgBtn = new JButton("Vybrat");
		trgBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDirDialog(LAST_TARGET, Cast.toString(params.get(LAST_TARGET)));
			}
		});

		add(mainPane, trgBtn, 3, 1, 1, 1);

		JLabel qual = new JLabel("RF koeficient", JLabel.RIGHT);
		qual.setToolTipText("Nižší hodnota znamená vyšší kvalitu výsledného videa");
		add(mainPane, qual, 0, 2, 1, 1);

		Integer q = Cast.toInteger(params.get(QUALITY));
		int qv;
		if(q == null) {
			qv = 21;
		} else {
			qv = q.intValue();
		}
		if(qv < 0) {
			qv = 0;
		} else if(qv > 50) {
			qv = 50;
		}

		quality = new JSpinner(new SpinnerNumberModel(qv, 0, 50, 1));
		quality.setToolTipText(qual.getToolTipText());
		add(mainPane, quality, 1, 2, 1, 1);

		JLabel pres = new JLabel("Nastavení", JLabel.RIGHT);
		pres.setToolTipText(  "<html>Pokročilé nastavení zpracování videa:<table>"
							+ "<tr><td>defaults</td><td>Výchozí nastavení</td></tr>"
							+ "<tr><td>ultrafast</td><td>Velmi rychlé zpracování, nejhorší výstup</td></tr>"
							+ "<tr><td>superfast</td><td></td></tr>"
							+ "<tr><td>veryfast</td><td></td></tr>"
							+ "<tr><td>faster</td><td></td></tr>"
							+ "<tr><td>fast</td><td></td></tr>"
							+ "<tr><td>medium</td><td>Vyvážený poměr mezi kvalitou a dobou zpracování</td></tr>"
							+ "<tr><td>slow</td><td></td></tr>"
							+ "<tr><td>slower</td><td></td></tr>"
							+ "<tr><td>veryslow</td><td>Velmi pomalé zpracování, nejkvalitnější výstup</td></tr>"
							+ "</table></html>");
		add(mainPane, pres, 2, 2, 1, 1);

		String sv = Cast.toString(params.get(PRESET));

		preset = new JComboBox<x264>(x264.values());
		preset.setToolTipText(pres.getToolTipText());
		if(StringUtils.isNotBlank(sv)) {
			try {
			x264 cv = x264.valueOf(sv);
			preset.setSelectedItem(cv);
			} catch (Exception e) { }
		}
		add(mainPane, preset, 3, 2, 1, 1);

		add(mainPane, BorderLayout.NORTH);

		statusPane = new JPanel(new BorderLayout(5, 5));
		statusPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		status = new JLabel("");
		statusPane.add(status, BorderLayout.CENTER);

		process = new JButton("   Start   ");
		process.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(working) {
					terminate();
				} else {
					process();
				}
			}
		});
		statusPane.add(process, BorderLayout.EAST);
		process.setEnabled(StringUtils.isNotBlank(source.getText()) && StringUtils.isNotBlank(target.getText()));

		add(statusPane, BorderLayout.AFTER_LAST_LINE);

		plModel = new DefaultTableModel(0, HEADER.length);
		plModel.setColumnIdentifiers(HEADER);
		playlist = new JTable(plModel) {
			@Override
			public Class<?> getColumnClass(int column) {
				if(column == 0) {
					return Boolean.class;
				}
				return String.class;
			}
		};
		final JCheckBox hcb = new JCheckBox();
		hcb.setSelected(true);
		playlist.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if(column == 0) {
					return hcb;
				}
				JLabel lbl = new JLabel(Cast.toString(value));
				lbl.setBorder(new EmptyBorder(5, 35, 5, 5));
				return lbl;
			}
		});
		playlist.getTableHeader().addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) { }

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) { }

			@Override
			public void mouseEntered(MouseEvent e) { }

			@SuppressWarnings("boxing")
			@Override
			public void mouseClicked(MouseEvent e) {
				int c = playlist.getTableHeader().columnAtPoint(e.getPoint());
				if(c == 0) {
					boolean val = !hcb.isSelected();
					hcb.setSelected(val);
					for(int i = 0; i < mpls.size(); i++) {
						plModel.setValueAt(val, i, 0);
					}
				}
			}
		});

		JScrollPane sp = new JScrollPane(playlist);
		sp.setBorder(new EmptyBorder(0, 8, 0, 8));
		add(sp, BorderLayout.CENTER);
		playlist.getColumnModel().getColumn(0).setMaxWidth(playlist.getColumnModel().getColumn(0).getMinWidth() + 6);
		setVisible(true);
		loadPlaylist();
	}

	private void loadPlaylist() {
		cleanPlaylist();
		if(null != validateSourceDir(Cast.toString(params.get(LAST_SOURCE)))) {
			try {
				MPLreader mplr = new MPLreader(new File(Cast.toString(params.get(LAST_SOURCE))));
				TreeMap<String, MPL> mpls = mplr.mpls;
				MPL mpl;
				for(String key : mpls.keySet()) {
					mpl = mpls.get(key);
					for(MPLItem item : mpl.items) {
						this.mpls.add(item);
						plModel.addRow(buildRow(item));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void cleanPlaylist() {
		mpls.clear();
		plModel.setRowCount(0);
	}

	private void process() {
		if(StringUtils.isNotBlank(source.getText()) && StringUtils.isNotBlank(target.getText())) {
			process.setText("   Stop   ");
			working = true;
			ArrayList<MPLItem> queue = new ArrayList<MPLItem>();
			for(int i = 0; i < mpls.size(); i++) {
				if(Cast.toBoolean(plModel.getValueAt(i, 0))) {
					queue.add(mpls.get(i));
				}
			}
			Integer qv = (Integer) quality.getValue();
			params.put(QUALITY, qv);
			x264 sv = (x264) preset.getSelectedItem();
			params.put(PRESET, sv.name());
			updateConfig();
			convertor = Convertor.convert(queue, target.getText(), qv, sv, this);
		}
	}

	private void terminate() {
		process.setText("   Start   ");
		working = false;
		convertor.terminate();
	}

	public void add(JPanel trg, Component c, int x, int y, int w, int h) {
		trg.add(c, new GridBagConstraints(x, y, w, h, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, okraj, 0, 0));
	}

	@Override
	public void componentHidden(ComponentEvent ce) { }

	@SuppressWarnings("boxing")
	@Override
	public void componentMoved(ComponentEvent ce) {
		Component c = ce.getComponent();
		params.put(WIN_LEFT, c.getX());
		params.put(WIN_TOP, c.getY());
		updateConfig();
	}

	@SuppressWarnings("boxing")
	@Override
	public void componentResized(ComponentEvent ce) {
		Component c = ce.getComponent();
		int	width = c.getWidth(),
			height = c.getHeight();

		params.put(WIN_HEIGHT, height);
		params.put(WIN_WIDTH, width);
		updateConfig();
	}

	private void openDirDialog(final String key, final String initDir) {
		final String dir = ((StringUtils.isNotBlank(initDir) ? initDir : System.getProperty("user.home")) + "/").replaceAll("//", "/");
		Thread dt = new Thread(new Runnable() {
			@SuppressWarnings("unused")
			@Override
			public void run() {
				new Subprocess(new String[] {"zenity", "--file-selection", "--directory", "--filename=" + dir + "'",  "--title=Výběr adresáře"}, new ChangeListener() {

					@Override
					public void status(String msg) {
						setDir(key, msg);
					}

					@Override
					public void error(String msg) {}

					@Override
					public void done() {}
				}, false);
			}
		});
		dt.start();
	}

	private void setDir(String key, String dir) {
		if(StringUtils.isNotBlank(dir)) {
			if(LAST_SOURCE.equals(key)) {
				dir = validateSourceDir(dir);
				if(dir == null) {
					dir = "";
				}
			}
		} else {
			dir = "";
		}
		updateConfig();
		params.put(key, dir);

		if(LAST_SOURCE.equals(key)) {
			cleanPlaylist();
			source.setText(dir);
			if(StringUtils.isNotBlank(dir)) {
				loadPlaylist();
			}
		} else {
			target.setText(dir);
		}
		process.setEnabled(StringUtils.isNotBlank(source.getText()) && StringUtils.isNotBlank(target.getText()));
	}

	private String validateSourceDir(String dir) {
		if(StringUtils.isNotBlank(dir)) {
			File baseDir = new File(dir);
			while(baseDir != null && baseDir.getAbsolutePath().length() > 0) {
				if(MPLreader.containsPlaylist(baseDir) && MPLreader.containsSteams(baseDir)) {
					return baseDir.getAbsolutePath();
				}
				baseDir = baseDir.getParentFile();
			}
		}
		return null;
	}

	@Override
	public void componentShown(ComponentEvent ce) { }

	@SuppressWarnings("boxing")
	private TreeMap<String, Object> loadConfig() {
		TreeMap<String, Object> params = Actions.loadConfig(CFG);
		if(params == null) {
			params = new TreeMap<String, Object>();
		}
		int w, h, t, l;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if(!params.containsKey(WIN_WIDTH) || params.get(WIN_WIDTH) == null) {
			w = MIN_WIDTH;
		} else {
			w = Cast.toInt(params.get(WIN_WIDTH));
			if(w < MIN_WIDTH) {
				w = MIN_WIDTH;
			} else if (w > dim.width) {
				w = dim.width;
			}
		}
		params.put(WIN_WIDTH, w);
		if(!params.containsKey(WIN_HEIGHT) || params.get(WIN_HEIGHT) == null) {
			h = MIN_HEIGHT;
		} else {
			h = Cast.toInt(params.get(WIN_HEIGHT));
			if(h < MIN_HEIGHT) {
				h = MIN_HEIGHT;
			} else if (h > dim.height) {
				h = dim.height;
			}
		}
		params.put(WIN_HEIGHT, h);
		if(!params.containsKey(WIN_TOP) || params.get(WIN_TOP) == null) {
			t = (dim.height - h) / 2;
		} else {
			t = Cast.toInt(params.get(WIN_TOP));
			if(t + h > dim.height) {
				t = dim.height - h;
			}
		}
		params.put(WIN_TOP, t);
		if(!params.containsKey(WIN_LEFT) || params.get(WIN_LEFT) == null) {
			l = (dim.width - w) / 2;
		} else {
			l = Cast.toInt(params.get(WIN_LEFT));
			if(l + w > dim.width) {
				l = dim.width - w;
			}
		}
		params.put(WIN_LEFT, l);
		return params;
	}

	private Object[] buildRow(MPLItem item) {
		Object[] row = new Object[3];
		row[0] = Boolean.TRUE;
		row[1] = cdf.format(item.dateTime);
		row[2] = "N/A";
		return row;
	}

	private void updateConfig() {
		if(!Actions.updateConfig(params, CFG)) {
			error("Aktualizace konfigurace selhala");
		}
	}

	@SuppressWarnings("boxing")
	private void addStatus(Color c, String text) {
		String line = sdf.format(new Date()) + ": " + text;
		status.setForeground(c);
		status.setText(line);
		line = line.replaceAll("\n", "<br>");
		history.append("<p style=\"color:").append(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue())).append("\">").append(line).append("</p>");
		status.setToolTipText(history.toString() + "</div></html>");
	}

	@Override
	public void status(String msg) {
		if(info == -1) {
			info = history.length();
			addStatus(Color.BLACK, msg);
		} else {
			history.setLength(info);
			addStatus(Color.BLACK, msg);
		}
	}

	@Override
	public void done() {
		info = -1;
		addStatus(Color.BLACK, "Zpracování dokončeno.");
		process.setText("   Start   ");
		working = false;
	}

	@Override
	public void error(String msg) {
		info = -1;
		if(StringUtils.isNotBlank(msg)) {
			addStatus(Color.RED, msg);
		}
	}
}
