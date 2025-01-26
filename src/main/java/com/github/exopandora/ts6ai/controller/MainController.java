package com.github.exopandora.ts6ai.controller;

import static com.github.exopandora.ts6ai.util.OS.LINUX;
import static com.github.exopandora.ts6ai.util.OS.MAC_OS;
import static com.github.exopandora.ts6ai.util.OS.OPERATING_SYSTEM;
import static com.github.exopandora.ts6ai.util.OS.WINDOWS;
import static com.github.exopandora.ts6ai.view.Window.TITLE;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;

import com.github.exopandora.ts6ai.util.SimpleDocumentListener;
import com.github.exopandora.ts6ai.view.InstallDirPane;
import com.github.exopandora.ts6ai.view.InstallPane;
import com.github.exopandora.ts6ai.view.UninstallPane;
import com.github.exopandora.ts6ai.view.Window;

public class MainController implements Runnable {
	private String installDir = MainController.findTeamSpeakInstallDir().map(File::toString).orElseGet(() -> {
		JOptionPane.showMessageDialog(null, "Unsupported Operating System", TITLE, ERROR_MESSAGE);
		System.exit(1);
		return null;
	});
	private final Window window = new Window();
	private final InstallController installController = new InstallController(this);
	
	public MainController() {
		this.window.getTabbedPane().addChangeListener(this::onTabChanged);
		SimpleDocumentListener documentListener = new SimpleDocumentListener(this::onInstallDirChanged);
		InstallPane installPane = this.installController.getInstallPane();
		installPane.getInstallDirTextField().setText(this.installDir);
		installPane.getInstallDirTextField().getDocument().addDocumentListener(documentListener);
		installPane.getSelectInstallDirButton().addActionListener(this::selectInstallDir);
		UninstallController uninstallController = new UninstallController(this);
		UninstallPane uninstallPane = uninstallController.getUninstallPane();
		uninstallPane.getInstallDirTextField().setText(this.installDir);
		uninstallPane.getInstallDirTextField().getDocument().addDocumentListener(documentListener);
		uninstallPane.getSelectInstallDirButton().addActionListener(this::selectInstallDir);
	}
	
	@Override
	public void run() {
		this.window.showWindow();
	}
	
	private void onTabChanged(ChangeEvent event) {
		this.getInstallDirPane().getInstallDirTextField().setText(this.installDir);
	}
	
	private void onInstallDirChanged(DocumentEvent event) {
		this.installDir = this.getInstallDirPane().getInstallDirTextField().getText();
	}
	
	private void selectInstallDir(ActionEvent event) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setFileSelectionMode(DIRECTORIES_ONLY);
		int result = chooser.showSaveDialog(this.window);
		if(result == APPROVE_OPTION) {
			this.getInstallDirPane().getInstallDirTextField().setText(chooser.getSelectedFile().getAbsolutePath());
		}
		this.installController.updateInterface();
	}
	
	private InstallDirPane getInstallDirPane() {
		return (InstallDirPane) this.window.getTabbedPane().getSelectedComponent();
	}
	
	public Window getWindow() {
		return this.window;
	}
	
	public String getInstallDir() {
		return this.installDir;
	}
	
	private static Optional<File> findTeamSpeakInstallDir() {
		return OPERATING_SYSTEM.map(os -> {
			if(WINDOWS.equals(os)) {
				File file = new File(System.getenv("ProgramFiles"), "TeamSpeak");
				if(file.exists()) {
					return file;
				}
				return new File(System.getenv("LOCALAPPDATA"), "Programs/TeamSpeak");
			} else if(MAC_OS.equals(os)) {
				return new File("/Applications/TeamSpeak.app");
			} else if(LINUX.equals(os)) {
				File file = new File(System.getProperty("user.home"), ".local/share/TeamSpeak");
				if(file.exists()) {
					return file;
				}
				file = new File("/opt/TeamSpeak/");
				if(file.exists()) {
					return file;
				}
				return new File(System.getProperty("user.home"), "Programs/TeamSpeak");
			}
			throw new IllegalArgumentException("Unknown operating system " + os);
		});
	}
}
