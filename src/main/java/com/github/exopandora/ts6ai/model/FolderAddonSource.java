package com.github.exopandora.ts6ai.model;

import com.github.exopandora.ts6ai.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
	public Optional<String> findFile(Predicate<String> predicate) throws IOException {
		Path root = Paths.get(this.root);
		try(Stream<Path> stream = Files.walk(root)) {
			return stream.map(entry -> root.relativize(entry).toString())
				.dropWhile(predicate.negate())
				.findFirst();
		}
	}
	
	@Override
	public void close() {}
}
