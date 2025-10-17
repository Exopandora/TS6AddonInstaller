package com.github.exopandora.ts6ai.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
	private Supplier<T> supplier;
	
	public Lazy(Supplier<T> supplier) {
		this.supplier = () -> {
			T value = supplier.get();
			Lazy.this.supplier = () -> value;
			return value;
		};
	}
	
	@Override
	public T get() {
		return supplier.get();
	}
}
