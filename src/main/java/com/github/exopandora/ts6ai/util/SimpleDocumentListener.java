package com.github.exopandora.ts6ai.util;

import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleDocumentListener implements DocumentListener {
	private final Consumer<DocumentEvent> updateConsumer;
	
	public SimpleDocumentListener(Runnable updateRunnable) {
		this(event -> updateRunnable.run());
	}
	
	public SimpleDocumentListener(Consumer<DocumentEvent> updateConsumer) {
		this.updateConsumer = updateConsumer;
	}
	
	@Override
	public void insertUpdate(DocumentEvent event) {
		this.updateConsumer.accept(event);
	}
	
	@Override
	public void removeUpdate(DocumentEvent event) {
		this.updateConsumer.accept(event);
	}
	
	@Override
	public void changedUpdate(DocumentEvent event) {
		this.updateConsumer.accept(event);
	}
}