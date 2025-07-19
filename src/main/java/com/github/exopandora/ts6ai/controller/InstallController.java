package com.github.exopandora.ts6ai.controller;

import static com.github.exopandora.ts6ai.util.Util.OBJECT_MAPPER;
import static com.github.exopandora.ts6ai.view.Window.TITLE;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_AND_DIRECTORIES;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.exopandora.ts6ai.model.Addon;
import com.github.exopandora.ts6ai.model.FolderAddonSource;
import com.github.exopandora.ts6ai.model.IAddonSource;
import com.github.exopandora.ts6ai.model.Installer;
import com.github.exopandora.ts6ai.model.LocalZipAddonSource;
import com.github.exopandora.ts6ai.model.RemoteZipAddonSource;
import com.github.exopandora.ts6ai.util.SimpleDocumentListener;
import com.github.exopandora.ts6ai.view.AddonEntry;
import com.github.exopandora.ts6ai.view.AddonEntry.RemoteAddonEntry;
import com.github.exopandora.ts6ai.view.InstallPane;
import com.vdurmont.semver4j.Semver;

public class InstallController {
	private final MainController mainController;
	private final InstallPane installPane;
	private final Map<String, List<Entry<Semver, String>>> versionCache = new HashMap<String, List<Entry<Semver, String>>>();
	
	public InstallController(MainController mainController) {
		this.mainController = mainController;
		this.installPane = this.mainController.getWindow().getInstallPane();
		this.installPane.getSelectAddonLocationButton().addActionListener(this::selectAddonLocation);
		this.installPane.getSelectAddonLocationTextField().getDocument().addDocumentListener(new SimpleDocumentListener(this::updateInterface));
		JComboBox<AddonEntry> addonComboBox = this.installPane.getAddonComboBox();
		addonComboBox.addActionListener(this::selectAddon);
		try {
			InputStream addonsJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("addons.json");
			JsonNode node = OBJECT_MAPPER.readTree(addonsJson);
			Iterator<Entry<String, JsonNode>> iterator = node.fields();
			List<AddonEntry> addons = new ArrayList<AddonEntry>();
			while(iterator.hasNext()) {
				Entry<String, JsonNode> entry = iterator.next();
				if(entry.getValue().isTextual()) {
					addons.add(new RemoteAddonEntry(entry.getKey(), new URI(entry.getValue().asText()).toURL()));
				}
			}
			addons.sort(Comparator.comparing(AddonEntry::getName));
			addons.forEach(addonComboBox::addItem);
		} catch(Exception e) {
			e.printStackTrace();
		}
		addonComboBox.addItem(new AddonEntry("Local Addon"));
		addonComboBox.setSelectedIndex(0);
		this.installPane.getLoadVersionsButton().addActionListener(this::loadVersions);
		this.installPane.getInstallButton().addActionListener(this::install);
	}
	
	private void selectAddonLocation(ActionEvent event) {
		JFileChooser chooser = new JFileChooser(new File("."));
		chooser.setFileSelectionMode(FILES_AND_DIRECTORIES);
		File current = new File(this.installPane.getSelectAddonLocationTextField().getText());
		if(current.exists()) {
			if(current.isFile()) {
				chooser.setCurrentDirectory(current.getParentFile());
			} else if(current.isDirectory()) {
				chooser.setCurrentDirectory(current);
			}
		}
		int result = chooser.showSaveDialog(null);
		if(result == APPROVE_OPTION) {
			this.installPane.getSelectAddonLocationTextField().setText(chooser.getSelectedFile().getAbsolutePath());
		}
		this.updateInterface();
	}
	
	private void selectAddon(ActionEvent event) {
		Object selected = this.installPane.getAddonComboBox().getSelectedItem();
		Boolean mode = null;
		if(selected instanceof RemoteAddonEntry) {
			RemoteAddonEntry addon = (RemoteAddonEntry) selected;
			JComboBox<Object> versionComboBox = this.installPane.getVersionComboBox();
			versionComboBox.removeAllItems();
			if(this.versionCache.containsKey(addon.getName())) {
				for(Entry<Semver, String> entry : this.versionCache.get(addon.getName())) {
					versionComboBox.addItem(entry.getKey());
				}
			} else {
				versionComboBox.addItem("Latest");
			}
			mode = false;
		} else if(selected instanceof AddonEntry) {
			mode = true;
		}
		if(mode != null) {
			this.installPane.getAddonLocationLabel().setEnabled(mode);
			this.installPane.getSelectAddonLocationTextField().setEnabled(mode);
			this.installPane.getSelectAddonLocationButton().setEnabled(mode);
			this.installPane.getVersionLabel().setVisible(!mode);
			this.installPane.getVersionComboBox().setVisible(!mode);
			this.installPane.getLoadVersionsButton().setVisible(!mode);
			this.installPane.getAddonLocationLabel().setVisible(mode);
			this.installPane.getSelectAddonLocationTextField().setVisible(mode);
			this.installPane.getSelectAddonLocationButton().setVisible(mode);
		}
		this.updateInterface();
	}
	
	private void loadVersions(ActionEvent event) {
		JButton loadVersionsButton = this.installPane.getLoadVersionsButton();
		loadVersionsButton.setEnabled(false);
		SwingWorker<List<Entry<Semver, String>>, Void> worker = new SwingWorker<List<Entry<Semver, String>>, Void>() {
			@Override
			protected List<Entry<Semver, String>> doInBackground() throws Exception {
				RemoteAddonEntry addon = (RemoteAddonEntry) InstallController.this.installPane.getAddonComboBox().getSelectedItem();
				assert addon != null;
				List<Entry<Semver, String>> versions = Installer.loadVersions(addon.getVersionIndex());
				InstallController.this.versionCache.put(addon.getName(), versions);
				return versions;
			}
			
			@Override
			protected void done() {
				try {
					List<Entry<Semver, String>> versions = this.get();
					JComboBox<Object> versionComboBox = InstallController.this.installPane.getVersionComboBox();
					versionComboBox.removeAllItems();
					for(Entry<Semver, String> entry : versions) {
						versionComboBox.addItem(entry.getKey());
					}
				} catch(Exception e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					JOptionPane.showMessageDialog(null, "Failed to load versions (" + cause.getMessage() + ")", TITLE, ERROR_MESSAGE);
				}
				loadVersionsButton.setEnabled(true);
			}
		};
		worker.execute();
	}
	
	private void install(ActionEvent event) {
		JButton installButton = this.installPane.getInstallButton();
		installButton.setEnabled(false);
		String installButtonText = installButton.getText();
		installButton.setText("Installing...");
		SwingWorker<Optional<Addon>, Void> worker = new SwingWorker<Optional<Addon>, Void>() {
			@Override
			protected Optional<Addon> doInBackground() throws Exception {
				String installDir = InstallController.this.mainController.getInstallDir();
				Installer.validateInstallationPath(installDir, true);
				try(IAddonSource addonSource = InstallController.this.getAddonSource()) {
					return Installer.install(addonSource, installDir, (addon, installedAddon, compareResult) -> {
						String message;
						if(compareResult < 0) {
							message = "An older version of " + addon.getName() + " is already installed. Do you want to update?\n" + installedAddon.getVersion() + " -> " + addon.getVersion();
						} else if(compareResult > 0) {
							message = "A newer version of " + addon.getName() + " is already installed. Do you want to downgrade?\n" + installedAddon.getVersion() + " -> " + addon.getVersion();
						} else {
							message = "The target version of " + addon.getName() + " is already installed. Do you want to install anyway?";
						}
						int dialogResult = JOptionPane.showConfirmDialog(null, message, TITLE, YES_NO_OPTION);
						return dialogResult == YES_OPTION;
					});
				}
			}
			
			@Override
			protected void done() {
				try {
					this.get().ifPresent(addon -> {
						JOptionPane.showMessageDialog(null,addon.getName() + " has successfully been installed!", TITLE, INFORMATION_MESSAGE);
					});
				} catch(Exception e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					cause.printStackTrace();
					JOptionPane.showMessageDialog(null, cause.getMessage(), TITLE, ERROR_MESSAGE);
				}
				installButton.setText(installButtonText);
				installButton.setEnabled(true);
			}
		};
		worker.execute();
	}
	
	public void updateInterface() {
		boolean enabled = !this.mainController.getInstallDir().isEmpty();
		AddonEntry entry = (AddonEntry) this.installPane.getAddonComboBox().getSelectedItem();
		if(!(entry instanceof RemoteAddonEntry)) {
			enabled = enabled && !this.installPane.getSelectAddonLocationTextField().getText().isEmpty();
		}
		this.installPane.getInstallButton().setEnabled(enabled);
	}
	
	public InstallPane getInstallPane() {
		return this.installPane;
	}
	
	private IAddonSource getAddonSource() throws Exception {
		AddonEntry selectedEntry = (AddonEntry) this.installPane.getAddonComboBox().getSelectedItem();
		if(selectedEntry instanceof RemoteAddonEntry) {
			RemoteAddonEntry remoteAddon = (RemoteAddonEntry) selectedEntry;
			Object selectedVersion = this.installPane.getVersionComboBox().getSelectedItem();
			if(selectedVersion instanceof Semver) {
				Map<Semver, String> versions = this.versionCache.get(remoteAddon.getName())
					.stream()
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				String version = versions.get(selectedVersion);
				if(version != null) {
					return new RemoteZipAddonSource(new URI(version).toURL());
				}
			} else if(selectedVersion instanceof String) {
				try {
					List<Entry<Semver, String>> versions = Installer.loadVersions(remoteAddon.getVersionIndex());
					return new RemoteZipAddonSource(new URI(versions.get(0).getValue()).toURL());
				} catch(Exception e) {
					throw new Exception("Failed to load versions");
				}
			}
		} else {
			String path = this.installPane.getSelectAddonLocationTextField().getText();
			File file = new File(path);
			if(file.exists()) {
				if(file.isDirectory() && file.canRead()) {
					return new FolderAddonSource(path);
				} else if(file.isFile() && file.canRead()) {
					return new LocalZipAddonSource(path);
				}
			}
		}
		throw new IllegalStateException("Could not create addon source");
	}
}
