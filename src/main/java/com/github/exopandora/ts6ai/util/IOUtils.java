package com.github.exopandora.ts6ai.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IOUtils {
	public static String readFile(File file) throws IOException {
		return toString(Files.newInputStream(file.toPath()));
	}
	
	public static String readFileFromZip(ZipFile zipFile, String path) throws IOException {
		ZipEntry zipEntry = zipFile.getEntry(path);
		if(zipEntry == null || zipEntry.isDirectory()) {
			throw new IOException("Zip file does not contain expected file \"" + path + "\"");
		}
		return toString(zipFile.getInputStream(zipEntry));
	}
	
	public static String toString(InputStream inputStream) throws IOException {
		StringOutputStream outputStream = new StringOutputStream();
		copy(inputStream, outputStream);
		return outputStream.toString();
	}
	
	public static void writeFile(File file, String contents) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(contents);
		writer.close();
	}
	
	public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[8192];
		int length;
		while((length = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		inputStream.close();
		outputStream.close();
	}
}
