package com.company;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {

    private static final String DEFAULT_INI_FILE_PATH = "menu.ini";
    private static final String BUTTON_TITLE_KEY = "ButtonTitle";
    private static final String BUTTON_EXECUTABLE_KEY = "ButtonExecutable";
    private static final String FONT_SIZE_KEY = "FontSize";
    private static final String MENU_TITLE_KEY = "MenuTitle";
    private static final String MENU_ITEM_KEY = "MenuItem";
    private static final int MENU_ITEM_COUNT = 5;

    private static Ini ini;

    public Main(String[] args) throws IOException, BackingStoreException {
        super("Main");
        ini = getIniFile(args);
        if (ini == null) return;

        Preferences prefs = new IniPreferences(ini);
        Preferences mainMenu = prefs.node("MENU");
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu mItems = new JMenu();

        mItems.setText(mainMenu.get(MENU_TITLE_KEY, null));
        mItems.setFont(new Font(null, -1, Integer.parseInt(mainMenu.get(FONT_SIZE_KEY, null))));
        menuBar.add(mItems);

        for (int i = 0; i < MENU_ITEM_COUNT; i++) {
            Preferences menuItems = prefs.node(MENU_ITEM_KEY + (i + 1));
            mItems.add(JM.create(menuItems.get(BUTTON_TITLE_KEY, null), menuItems.get(BUTTON_EXECUTABLE_KEY, null)));
        }

        setPreferredSize(new Dimension(200, 200));
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

    private static class JM extends JMenu
            implements MouseListener {
        private static final String ARROW_ICON_KEY = "Menu.arrowIcon";

        private boolean populated = false; // Submenu already populated?
        private String action;

        protected JM(String label, String action) {
            super(label);
            this.action = action;
            addMouseListener(this);
        }

        public static JM create(String label, String action) {
            UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
            Object savedArrowIcon = uiDefaults.get(ARROW_ICON_KEY);
            uiDefaults.put(ARROW_ICON_KEY, null);
            JM newJM = new JM(label, action);
            uiDefaults.put(ARROW_ICON_KEY, savedArrowIcon);
            return newJM;
        }

        public void mouseClicked(MouseEvent ev) {
            if (this.action != null) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                String[] commands = buildAction();
                processBuilder.command(commands);

//                if(this.action.endsWith(".bat")) {
//                    processBuilder.command(this.action);
//                } else {
//                    processBuilder.command();
//                }
                try {

                    Process process = processBuilder.start();

                    StringBuilder output = new StringBuilder();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line + "\n");
                    }

                    int exitVal = process.waitFor();
                    if (exitVal == 0) {
                        System.out.println("Success!");
                        System.out.println(output);
                        System.exit(0);
                    } else {
                        System.out.println("Failure :(");
                        System.out.println(output);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            MenuSelectionManager.defaultManager().clearSelectedPath();
        }

        public void mouseEntered(MouseEvent ev) {
        }

        public void mouseExited(MouseEvent ev) {
        }

        public void mousePressed(MouseEvent ev) {
        }

        public void mouseReleased(MouseEvent ev) {
        }


        private String[] buildAction() {
            String[] cmds = null;
            Preferences prefs = new IniPreferences(ini);
            Preferences env = prefs.node("ENV");

            Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(action);
            while (matcher.find()) {
                String var = matcher.group(1);
                String value = env.get("#" + var, null);
            }

            return cmds;
        }
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