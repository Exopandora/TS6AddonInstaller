package com.github.exopandora.ts6ai.model;

import com.github.exopandora.ts6ai.util.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class Packer {
	private static final String NODE_SCRIPT = "script";
	private static final String NODE_STYLE = "style";
	private static final QName SRC_ATTRIBUTE = new QName("src");
	
	public static String pack(Addon addon, String injectionString, IAddonSource source, String addonRoot) throws Exception {
		if(addon.getSources().isEmpty()) {
			return injectionString;
		}
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader("<addon>" + injectionString + "</addon>"));
		Element root = document.getRootElement();
		packElement(root, source, addonRoot, addon.getSources());
		try(XMLWriter writer = new XMLWriter()) {
			writer.setEscapeText(false);
			StringWriter output = new StringWriter();
			writer.setWriter(output);
			for(Element element : root.elements()) {
				writer.write(element);
			}
			return output.toString();
		}
	}
	
	private static void packElement(Element element, IAddonSource source, String addonRoot, String sources) throws IOException {
		if(NODE_SCRIPT.equals(element.getName()) || NODE_STYLE.equals(element.getName())) {
			Attribute src = element.attribute(SRC_ATTRIBUTE);
			if(src != null) {
				if(src.getValue().startsWith(sources)) {
					String data = source.read(addonRoot + src.getValue());
					element.remove(src);
					element.setText(data);
				} else if(src.getValue().startsWith("https://") || src.getValue().startsWith("http://")) {
					try {
						URL url = new URI(src.getValue()).toURL();
						URLConnection connection = url.openConnection();
						InputStream inputStream = connection.getInputStream();
						String data = IOUtils.toString(inputStream);
						element.remove(src);
						element.setText(data);
					} catch(Exception e) {
						System.err.println("Could not download referenced remote file \"" + src.getValue() + "\" (" + e.getMessage() + "). Keeping reference.");
					}
				}
			}
		}
		// Workaround to create <script></script> instead of <script/>
		if(NODE_SCRIPT.equals(element.getName()) && !element.hasContent()) {
			element.setText("");
		}
		for(Element child : element.elements()) {
			packElement(child, source, addonRoot, sources);
		}
	}
}
