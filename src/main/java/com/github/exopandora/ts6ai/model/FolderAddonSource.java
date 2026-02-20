package com.github.exopandora.ts6ai.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import com.github.exopandora.ts6ai.util.IOUtils;

public class FolderAddonSource implements IAddonSource {
	private final String root;
	
	public FolderAddonSource(String root) {
		this.root = root;
	}
	
	@Override
	public void open() throws IOException {
		File file = new File(this.root);
		if(!file.exists() || !file.isDirectory()) {
			throw new IOException("Invalid addon folder");
		}
	}
	
	@Override
	public String read(String path) throws IOException {
		return IOUtils.readFile(new File(this.root, path));
	}
	
	@Override
	public Iterator<String> entries() throws IOException {
		Path root = Paths.get(this.root);
		try(Stream<Path> paths = Files.walk(root)) {
			return paths.map(entry -> root.relativize(entry).toString())
				.iterator();
		}
	}
	
	@Override
	public void close() {}
}
