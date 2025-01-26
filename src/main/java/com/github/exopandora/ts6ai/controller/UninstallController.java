package com.github.exopandora.ts6ai.controller;

import static com.github.exopandora.ts6ai.view.Window.TITLE;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import com.github.exopandora.ts6ai.model.InstalledAddon;
import com.github.exopandora.ts6ai.model.Installer;
import com.github.exopandora.ts6ai.view.UninstallPane;

public class UninstallController {
	private final MainController mainController;
	private final UninstallPane uninstallPane;
	
	public UninstallController(MainController mainController) {
		this.mainController = mainController;
		this.uninstallPane = this.mainController.getWindow().getUninstallPane();
		this.uninstallPane.getUninstallButton().addActionListener(this::uninstall);
		this.uninstallPane.getLoadAddonsButton().addActionListener(this::loadAddons);
	}
	
	private void loadAddons(ActionEvent action) {
		this.loadAddons(true);
	}
	
	private void loadAddons(boolean showEmptyWarningMessage) {
		String installDir = this.mainController.getInstallDir();
		try {
			Installer.validateInstallationPath(installDir, false);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), TITLE, ERROR_MESSAGE);
		}
		try {
			List<InstalledAddon> installedAddons = Installer.installedAddons(installDir);
			installedAddons.sort(Comparator.comparing(InstalledAddon::getName).thenComparing(InstalledAddon::getVersion));
			this.uninstallPane.getAddonComboBox().removeAllItems();
			for(InstalledAddon installedAddon : installedAddons) {
				this.uninstallPane.getAddonComboBox().addItem(installedAddon);
			}
			this.uninstallPane.getUninstallButton().setEnabled(!installedAddons.isEmpty());
			this.uninstallPane.getAddonComboBox().setEnabled(!installedAddons.isEmpty());
			if(showEmptyWarningMessage && installedAddons.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Could not find any installed addons", TITLE, WARNING_MESSAGE);
			}
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not find installed addons:\n" + e.getMessage(), TITLE, ERROR_MESSAGE);
		}
	}
	
	private void uninstall(ActionEvent action) {
		String installDir = this.mainController.getInstallDir();
		try {
			Installer.validateInstallationPath(installDir, false);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), TITLE, ERROR_MESSAGE);
		}
		InstalledAddon addon = (InstalledAddon) this.uninstallPane.getAddonComboBox().getSelectedItem();
		assert addon != null;
		try {
			Installer.uninstall(addon, installDir);
			JOptionPane.showMessageDialog(null, "The addon " + addon + " has been successfully uninstalled", TITLE, INFORMATION_MESSAGE);
			this.loadAddons(false);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Error uninstalling addon " + addon + ":\n" + e.getMessage(), TITLE, ERROR_MESSAGE);
		}
	}
	
	public UninstallPane getUninstallPane() {
		return this.uninstallPane;
	}
}
