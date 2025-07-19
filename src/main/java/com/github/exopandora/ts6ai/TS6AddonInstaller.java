package com.github.exopandora.ts6ai;

import com.github.exopandora.ts6ai.cli.CLIController;
import com.github.exopandora.ts6ai.controller.MainController;
import com.github.exopandora.ts6ai.view.Window;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class TS6AddonInstaller {
	public static final String VERSION;
	
	static {
		try {
			ClassLoader classLoader = TS6AddonInstaller.class.getClassLoader();
			List<URL> manifests = enumerationToList(classLoader.getResources("META-INF/MANIFEST.MF"));
			if(manifests.size() == 1) {
				Manifest manifest = new Manifest(manifests.get(0).openStream());
				Attributes attributes = manifest.getMainAttributes();
				VERSION = attributes.getValue("Implementation-Version");
			} else {
				VERSION = "DEV";
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String... args) {
		Options options = new Options();
		Option patch = Option.builder()
			.longOpt("patch")
			.desc("Patches the TeamSpeak installation to enable addon support.")
			.required(false)
			.build();
		options.addOption(patch);
		Option install = Option.builder()
			.longOpt("install")
			.desc("Installs an addon from zip, folder or url, and applies patches to the TeamSpeak installation if required.")
			.required(false)
			.hasArg(true)
			.numberOfArgs(1)
			.argName("addon")
			.build();
		options.addOption(install);
		Option uninstall = Option.builder()
			.longOpt("uninstall")
			.desc("Uninstalls an addon by id or name.")
			.required(false)
			.hasArg(true)
			.numberOfArgs(1)
			.argName("addon")
			.build();
		options.addOption(uninstall);
		Option listInstalled = Option.builder()
			.longOpt("list-installed")
			.desc("Displays installed addons.")
			.build();
		options.addOption(listInstalled);
		Option help = Option.builder("h")
			.longOpt("help")
			.desc("Displays this help text.")
			.build();
		options.addOption(help);
		Option force = Option.builder("y")
			.longOpt("yes")
			.desc("Automatically answers all prompts with 'yes'.")
			.required(false)
			.build();
		options.addOption(force);
		Option version = Option.builder("v")
			.longOpt("version")
			.desc("Displays the version of the installer.")
			.build();
		options.addOption(version);
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption(help)) {
				printHelp(options);
			} else if(cmd.hasOption(version)) {
				System.out.println("Version: " + VERSION);
			} else if(cmd.hasOption(install)) {
				requireSingleArg(cmd);
				CLIController.install(cmd.getArgList().get(0), cmd.getOptionValue(install), cmd.hasOption(force));
			} else if(cmd.hasOption(uninstall)) {
				requireSingleArg(cmd);
				CLIController.uninstall(cmd.getArgList().get(0), cmd.getOptionValue(uninstall));
			} else if(cmd.hasOption(listInstalled)) {
				requireSingleArg(cmd);
				CLIController.listInstalledAddons(cmd.getArgList().get(0));
			} else if(cmd.hasOption(patch)) {
				requireSingleArg(cmd);
				CLIController.patch(cmd.getArgList().get(0));
			} else {
				Window.setupNativeLook();
				MainController controller = new MainController();
				SwingUtilities.invokeLater(controller);
			}
		} catch(ParseException e) {
			printHelp(options);
			System.exit(1);
		}
	}
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("TS6AddonInstaller [OPTIONS] <TEAMSPEAK_PATH>", options);
	}
	
	private static void requireSingleArg(CommandLine cmd) throws ParseException {
		requireArgCount(1, cmd);
	}
	
	private static void requireArgCount(int count, CommandLine cmd) throws ParseException {
		if(cmd.getArgList().size() != count) {
			throw new ParseException("Invalid installation path");
		}
	}
	
	private static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
		List<T> list = new LinkedList<T>();
		while(enumeration.hasMoreElements()) {
			list.add(enumeration.nextElement());
		}
		return list;
	}
}
