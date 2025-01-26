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

public class InstallPane extends JPanel implements InstallDirPane {
	private final JLabel installDirLabel = new JLabel("Installation Directory");
	private final JTextField installDirTextField = new JTextField();
	private final JButton selectInstallDirButton = new JButton("...");
	private final JLabel addonLabel = new JLabel("Addon");
	private final JComboBox<AddonEntry> addonComboBox = new JComboBox<AddonEntry>();
	private final JLabel addonLocationLabel = new JLabel("Addon Location");
	private final JTextField selectAddonLocationTextField = new JTextField();
	private final JButton selectAddonLocationButton = new JButton("...");
	private final JComboBox<Object> versionComboBox = new JComboBox<Object>();
	private final JLabel versionLabel = new JLabel("Version");
	private final JButton loadVersionsButton = new JButton("Load");
	private final JButton installButton = new JButton("Install");
	
	public InstallPane() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 275, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints installDirLabelGbc = new GridBagConstraints();
		installDirLabelGbc.insets = new Insets(10, 10, 10, 10);
		installDirLabelGbc.anchor = GridBagConstraints.EAST;
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
		
		GridBagConstraints addonComboBoxGbc = new GridBagConstraints();
		addonComboBoxGbc.gridwidth = 2;
		addonComboBoxGbc.insets = new Insets(0, 0, 10, 10);
		addonComboBoxGbc.fill = HORIZONTAL;
		addonComboBoxGbc.gridx = 1;
		addonComboBoxGbc.gridy = 1;
		this.add(this.addonComboBox, addonComboBoxGbc);
		
		GridBagConstraints addonFileLabelGbc = new GridBagConstraints();
		addonFileLabelGbc.anchor = EAST;
		addonFileLabelGbc.insets = new Insets(0, 10, 10, 10);
		addonFileLabelGbc.gridx = 0;
		addonFileLabelGbc.gridy = 2;
		this.add(this.addonLocationLabel, addonFileLabelGbc);
		
		this.selectAddonLocationTextField.setColumns(1);
		GridBagConstraints selectAddonFileTextFieldGbc = new GridBagConstraints();
		selectAddonFileTextFieldGbc.insets = new Insets(0, 0, 10, 0);
		selectAddonFileTextFieldGbc.fill = HORIZONTAL;
		selectAddonFileTextFieldGbc.gridx = 1;
		selectAddonFileTextFieldGbc.gridy = 2;
		this.add(this.selectAddonLocationTextField, selectAddonFileTextFieldGbc);
		
		GridBagConstraints selectAddonFileButtonGbc = new GridBagConstraints();
		selectAddonFileButtonGbc.insets = new Insets(0, 0, 10, 10);
		selectAddonFileButtonGbc.gridx = 2;
		selectAddonFileButtonGbc.gridy = 2;
		this.add(this.selectAddonLocationButton, selectAddonFileButtonGbc);
		
		GridBagConstraints versionLabelGbc = new GridBagConstraints();
		versionLabelGbc.anchor = EAST;
		versionLabelGbc.insets = new Insets(0, 10, 10, 10);
		versionLabelGbc.gridx = 0;
		versionLabelGbc.gridy = 3;
		this.add(this.versionLabel, versionLabelGbc);
		
		GridBagConstraints versionComboBoxGbc = new GridBagConstraints();
		versionComboBoxGbc.insets = new Insets(0, 0, 10, 0);
		versionComboBoxGbc.fill = HORIZONTAL;
		versionComboBoxGbc.gridx = 1;
		versionComboBoxGbc.gridy = 3;
		this.add(this.versionComboBox, versionComboBoxGbc);
		
		GridBagConstraints loadVersionsButtonGbc = new GridBagConstraints();
		loadVersionsButtonGbc.insets = new Insets(0, 0, 10, 10);
		loadVersionsButtonGbc.gridx = 2;
		loadVersionsButtonGbc.gridy = 3;
		this.add(this.loadVersionsButton, loadVersionsButtonGbc);
		
		GridBagConstraints installButtonGbc = new GridBagConstraints();
		installButtonGbc.gridwidth = 3;
		installButtonGbc.insets = new Insets(0, 0, 10, 10);
		installButtonGbc.gridx = 0;
		installButtonGbc.gridy = 4;
		this.add(this.installButton, installButtonGbc);
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
	
	public JComboBox<AddonEntry> getAddonComboBox() {
		return this.addonComboBox;
	}
	
	public JLabel getAddonLocationLabel() {
		return this.addonLocationLabel;
	}
	
	public JButton getSelectAddonLocationButton() {
		return this.selectAddonLocationButton;
	}
	
	public JButton getInstallButton() {
		return this.installButton;
	}
	
	public JTextField getSelectAddonLocationTextField() {
		return this.selectAddonLocationTextField;
	}
	
	public JButton getLoadVersionsButton() {
		return this.loadVersionsButton;
	}
	
	public JComboBox<Object> getVersionComboBox() {
		return this.versionComboBox;
	}
	
	public JLabel getVersionLabel() {
		return this.versionLabel;
	}
}
