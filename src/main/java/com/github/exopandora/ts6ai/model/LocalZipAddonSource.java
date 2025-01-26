package com.github.exopandora.ts6ai.model;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class LocalZipAddonSource extends AbstractZipAddonSource {
	private final String path;
	
	public LocalZipAddonSource(String path) {
		this.path = path;
	}
	
	@Override
	public void open() throws IOException {
		File file = new File(this.path);
		if(!file.exists() || !file.isFile()) {
			throw new IOException("Invalid addon file");
		}
		this.zipFile = new ZipFile(file);
	}
	
	@Override
	public void close() throws Exception {
		if(this.zipFile != null) {
			this.zipFile.close();
		}
	}
}
