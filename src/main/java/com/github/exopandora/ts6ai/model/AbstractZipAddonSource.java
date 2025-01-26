package com.github.exopandora.ts6ai.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.exopandora.ts6ai.util.IOUtils;

public abstract class AbstractZipAddonSource implements IAddonSource {
	protected ZipFile zipFile;
	
	@Override
	public String read(String path) throws IOException {
		this.ensureOpen();
		return IOUtils.readFileFromZip(this.zipFile, path);
	}
	
	@Override
	public Iterator<String> entries() {
		this.ensureOpen();
		return this.zipFile.stream().map(ZipEntry::getName).iterator();
	}
	
	protected void ensureOpen() {
		if(this.zipFile == null) {
			throw new IllegalStateException("The addon file is not open");
		}
	}
}
