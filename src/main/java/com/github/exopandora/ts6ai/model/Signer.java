package com.github.exopandora.ts6ai.model;

import com.github.exopandora.ts6ai.util.OS;

public class Signer {
	public static void sign(String installDir) throws Exception {
		try {
			if(OS.getOrThrow() == OS.MAC_OS) {
				Process signProcess = new ProcessBuilder()
					.command("codesign", "-fs", "-", installDir)
					.inheritIO()
					.start();
				int signResult = signProcess.waitFor();
				if(signResult != 0) {
					throw new Exception("Failed to ad-hoc sign app. Error code " + signResult);
				}
			}
		} catch(Exception e) {
			throw new Exception("Could not create app signature. Please make sure you started the installer with elevated privileges.", e);
		}
	}
	
	public static void unsign(String installDir) throws Exception {
		try {
			if(OS.getOrThrow() == OS.MAC_OS) {
				Process process = new ProcessBuilder()
					.command("codesign", "--remove-signature", installDir)
					.inheritIO()
					.start();
				int result = process.waitFor();
				if(result != 0) {
					throw new Exception("Failed to remove app signature. Error code " + result);
				}
			}
		} catch(Exception e) {
			throw new Exception("Could not remove app signature. Please make sure you started the installer with elevated privileges.", e);
		}
	}
}
