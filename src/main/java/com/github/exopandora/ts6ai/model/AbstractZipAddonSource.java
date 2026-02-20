package com.github.exopandora.ts6ai.model;

import com.github.exopandora.ts6ai.util.IOUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class AbstractZipAddonSource implements IAddonSource {
	protected ZipFile zipFile;
	
	@Override
	public String read(String path) throws IOException {
		this.ensureOpen();
		return IOUtils.readFileFromZip(this.zipFile, path);
	}
	
	@Override
	public Optional<String> findFile(Predicate<String> predicate) {
		this.ensureOpen();
		return this.zipFile.stream().map(ZipEntry::getName)
			.dropWhile(predicate.negate())
			.findFirst();
	}
	
	protected void ensureOpen() {
		if(this.zipFile == null) {
			throw new IllegalStateException("The addon file is not open");
		}
	}
}
