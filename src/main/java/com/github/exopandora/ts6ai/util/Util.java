package com.github.exopandora.ts6ai.util;

import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final char[] HEX_ALPHABET = "0123456789ABCDEF".toCharArray();
	
	public static String validate(String input, String regex) throws Exception {
		if(!input.matches(regex)) {
			throw new Exception(input + " does not match pattern " + regex);
		}
		return input;
	}
	
	public static String md5sum(File file) throws IOException {
		return md5sum(Files.newInputStream(file.toPath()));
	}
	
	public static String md5sum(InputStream inputStream) throws IOException {
		try(inputStream) {
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[8192];
			int length;
			while((length = inputStream.read(buffer)) != -1) {
				md5Digest.update(buffer, 0, length);
			}
			byte[] digest = md5Digest.digest();
			return bytesToHex(digest).toLowerCase();
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not get md5 algorithm");
		}
	}
	
	public static String bytesToHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder(bytes.length * 2);
		for(byte b : bytes) {
			builder.append(HEX_ALPHABET[(b >> 4) & 0xF]);
			builder.append(HEX_ALPHABET[(b & 0xF)]);
		}
		return builder.toString();
	}
	
	public static int clamp(int input, int min, int max) {
		return input < min ? min : Math.min(input, max);
	}
}
