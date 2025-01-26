package com.github.exopandora.ts6ai.view;

import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.exopandora.ts6ai.model.InstalledAddon;

public class UninstallPane extends JPanel implements InstallDirPane {
	private final JLabel installDirLabel = new JLabel("Installation Directory");
	private final JTextField installDirTextField = new JTextField();
	private final JButton selectInstallDirButton = new JButton("...");
	private final JLabel addonLabel = new JLabel("Addon");
	private final JComboBox<InstalledAddon> addonComboBox = new JComboBox<>();
	private final JButton loadAddonsButton = new JButton("Load");
	private final JButton uninstallButton = new JButton("Uninstall");
	
	public UninstallPane() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 275, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints installDirLabelGbc = new GridBagConstraints();
		installDirLabelGbc.insets = new Insets(10, 10, 10, 10);
		installDirLabelGbc.anchor = EAST;
		installDirLabelGbc.gridx = 0;
		installDirLabelGbc.gridy = 0;
		this.add(this.installDirLabel, installDirLabelGbc);
		
		GridBagConstraints installDirTextFieldGbc = new GridBagConstraints();
		installDirTextFieldGbc.insets = new Insets(10, 0, 10, 0);
		installDirTextFieldGbc.fill = HORIZONTAL;
		installDirTextFieldGbc.gridx = 1;
		installDirTextFieldGbc.gridy = 0;
		this.add(this.installDirTextField, installDirTextFieldGbc);
		
		GridBagConstraints selectInstallDirButtonGbc = new GridBagConstraints();
		selectInstallDirButtonGbc.insets = new Insets(10, 0, 10, 10);
		selectInstallDirButtonGbc.fill = HORIZONTAL;
		selectInstallDirButtonGbc.gridx = 2;
		selectInstallDirButtonGbc.gridy = 0;
		this.add(this.selectInstallDirButton, selectInstallDirButtonGbc);
		
		GridBagConstraints addonLabelGbc = new GridBagConstraints();
		addonLabelGbc.anchor = EAST;
		addonLabelGbc.insets = new Insets(0, 10, 10, 10);
		addonLabelGbc.gridx = 0;
		addonLabelGbc.gridy = 1;
		this.add(this.addonLabel, addonLabelGbc);
		
		this.addonComboBox.setEnabled(false);
		GridBagConstraints addonComboBoxGbc = new GridBagConstraints();
		addonComboBoxGbc.insets = new Insets(0, 0, 10, 0);
		addonComboBoxGbc.fill = HORIZONTAL;
		addonComboBoxGbc.gridx = 1;
		addonComboBoxGbc.gridy = 1;
		this.add(this.addonComboBox, addonComboBoxGbc);
		
		GridBagConstraints loadAddonsButtonGbc = new GridBagConstraints();
		loadAddonsButtonGbc.insets = new Insets(0, 0, 10, 10);
		loadAddonsButtonGbc.gridx = 2;
		loadAddonsButtonGbc.gridy = 1;
		this.add(this.loadAddonsButton, loadAddonsButtonGbc);
		
		this.uninstallButton.setEnabled(false);
		GridBagConstraints uninstallButtonGbc = new GridBagConstraints();
		uninstallButtonGbc.gridwidth = 3;
		uninstallButtonGbc.insets = new Insets(0, 0, 10, 10);
		uninstallButtonGbc.gridx = 0;
		uninstallButtonGbc.gridy = 2;
		this.add(this.uninstallButton, uninstallButtonGbc);
	}
	
	@Override
	public JTextField getInstallDirTextField() {
		return this.installDirTextField;
	}
	
	public JLabel getInstallDirLabel() {
		return this.installDirLabel;
	}
	
	public JButton getSelectInstallDirButton() {
		return this.selectInstallDirButton;
	}
	
	public JLabel getAddonLabel() {
		return this.addonLabel;
	}
	
	public JComboBox<InstalledAddon> getAddonComboBox() {
		return this.addonComboBox;
	}
	
	public JButton getLoadAddonsButton() {
		return this.loadAddonsButton;
	}
	
	public JButton getUninstallButton() {
		return this.uninstallButton;
	}
}
