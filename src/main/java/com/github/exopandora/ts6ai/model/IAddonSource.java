package com.github.exopandora.ts6ai.model;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

public interface IAddonSource extends AutoCloseable {
	void open() throws IOException;
	
	String read(String path) throws IOException;
	
	Optional<String> findFile(Predicate<String> predicate) throws IOException;
}
