package com.company;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

public class Main extends JFrame {

	private static final String DEFAULT_INI_FILE_PATH = "menu.ini";
	private static final String BUTTON_TITLE_KEY = "ButtonTitle";
	private static final String BUTTON_EXECUTABLE_KEY = "ButtonExecutable";
	// private static final String FONT_SIZE_KEY = "FontSize";
	// private static final String MENU_TITLE_KEY = "MenuTitle";
	private static final String MENU_ITEM_KEY = "MenuItem";
	private static final int MENU_ITEM_COUNT = 5;

	private static Ini ini;

	public Main(String[] args) throws IOException, BackingStoreException {
		super("Main");
		ini = getIniFile(args);
		if (ini == null)
			return;

		Preferences prefs = new IniPreferences(ini);

		// mItems.setText(mainMenu.get(MENU_TITLE_KEY, null));
		// mItems.setFont(new Font(null, -1,
		// Integer.parseInt(mainMenu.get(FONT_SIZE_KEY, null))));
		// menuBar.add(mItems);
		setLayout(new java.awt.GridLayout(8, 8));

		for (int i = 0; i < MENU_ITEM_COUNT; i++) {
			Preferences menuItems = prefs.node(MENU_ITEM_KEY + (i + 1));
			JButton b = new JButton(menuItems.get(BUTTON_TITLE_KEY, null));
			b.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (menuItems.get(BUTTON_EXECUTABLE_KEY, null) != null) {
						String[] commands = buildAction(menuItems.get(BUTTON_EXECUTABLE_KEY, null));
						try {
							Runtime.getRuntime().exec(commands);
						} catch (IOException ex) {
							ex.printStackTrace();
						}

					}

				}
			});
			b.setPreferredSize(new Dimension(100, 30));
			add(b);
		}

		setPreferredSize(new Dimension(600, 400));
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private Ini getIniFile(String[] args) throws IOException {
		Ini ini = null;
		if (args.length != 0) {
			ini = new Ini(new File(args[0]));
		} else {
			try {
				ini = new Ini(this.getClass().getResourceAsStream(DEFAULT_INI_FILE_PATH));
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		return ini;
	}

	private String[] buildAction(String action) {
		String splittedCommade[] = null;
		String command = action;
		Preferences prefs = new IniPreferences(ini);
		Preferences env = prefs.node("ENV");

		Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(command);

		// get the variable (CHROME, FIREFOX ..) value from the ENV section ...
		String match = null;
		String value = null;
		while (matcher.find()) {
			match = matcher.group(1);
			value = env.get(match, null);
		}

		if (match != null) {
			// Replace the variable with its value in the command to execute
			splittedCommade = command.split(" ");
			for (int i = 0; i < splittedCommade.length; i++) {
				if (('#' + match).equals(splittedCommade[i])) {
					splittedCommade[i] = value;
				}
			}

		} else {
			splittedCommade = command.split(" ");
		}
		return splittedCommade;
	}

	public static void main(String[] args) {
		try {
			new Main(args).setVisible(true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}