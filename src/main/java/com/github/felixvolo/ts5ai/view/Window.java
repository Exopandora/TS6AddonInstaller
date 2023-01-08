package com.github.felixvolo.ts5ai.view;

import static com.github.felixvolo.ts5ai.TS5AddonInstaller.VERSION;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Window extends JFrame {
	public static final String TITLE = "TS5 Addon Installer " + VERSION.toString();
	
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private final InstallPane installPane = new InstallPane();
	private final UninstallPane uninstallPane = new UninstallPane();
	
	public Window() {
		super(TITLE);
		this.tabbedPane.addTab("Install", this.installPane);
		this.tabbedPane.addTab("Uninstall", this.uninstallPane);
		this.getContentPane().add(this.tabbedPane);
	}
	
	public void showWindow() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.requestFocus();
	}
	
	public InstallPane getInstallPane() {
		return this.installPane;
	}
	
	public UninstallPane getUninstallPane() {
		return this.uninstallPane;
	}
	
	public JTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}
	
	public static void setupNativeLook() {
		try {
			System.setProperty("sun.java2d.d3d", "false");
			System.setProperty("file.encoding", "UTF-8");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
