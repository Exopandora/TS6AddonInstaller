package com.github.exopandora.ts6ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.exopandora.ts6ai.model.Patcher.TS6Patch.FilePatch;
import com.github.exopandora.ts6ai.model.Patcher.TS6Patch.FilePatch.Patch;
import com.github.exopandora.ts6ai.util.OS;
import com.github.exopandora.ts6ai.util.Util;
import com.vdurmont.semver4j.Semver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.github.exopandora.ts6ai.util.Util.OBJECT_MAPPER;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Patcher {
	public static void patch(String installDir, Semver ts6Version) throws Exception {
		Optional<Map<String, FilePatch>> filePatches = loadFilePatches(ts6Version);
		if(filePatches.isPresent()) {
			Set<Entry<File, FilePatch>> patchesToApply = filterFilePatches(installDir, filePatches.get());
			Set<Entry<File, FilePatch>> successfulPatches = new HashSet<Entry<File, FilePatch>>();
			for(Entry<File, FilePatch> entry : patchesToApply) {
				try {
					applyFilePatches(entry.getKey(), entry.getValue());
					successfulPatches.add(entry);
				} catch(Exception e) {
					revertPatches(successfulPatches);
					throw new IllegalStateException("Could not patch file \"" + entry.getKey() + "\"", e);
				}
			}
		}
	}
	
	private static void revertPatches(Set<Entry<File, FilePatch>> successfulPatches) throws IOException {
		for(Entry<File, FilePatch> successfulPatch : successfulPatches) {
			File file = successfulPatch.getKey();
			FilePatch filePatch = successfulPatch.getValue();
			File backup = new File(successfulPatch.getKey() + ".bak");
			if(backup.exists() && filePatch.getVanilla().equals(Util.md5sum(backup))) {
				Files.move(backup.toPath(), file.toPath(), REPLACE_EXISTING);
			}
		}
	}
	
	private static void applyFilePatches(File file, FilePatch filePatch) throws IOException, IllegalStateException {
		File patched = Files.createTempFile("", "").toFile();
		Files.copy(file.toPath(), patched.toPath(), COPY_ATTRIBUTES, REPLACE_EXISTING);
		try(RandomAccessFile randomAccess = new RandomAccessFile(patched, "rw")) {
			for(Patch encodedPatch : filePatch.getPatches()) {
				DecodedPatch patch = decodePatch(encodedPatch);
				randomAccess.seek(patch.getOffset());
				byte[] currentBytes = new byte[patch.getVanillaBytes().length];
				randomAccess.readFully(currentBytes, 0, currentBytes.length);
				if(Arrays.equals(currentBytes, patch.getVanillaBytes())) {
					randomAccess.seek(patch.getOffset());
					randomAccess.write(patch.getPatchedBytes(), 0, patch.getPatchedBytes().length);
				} else if(!Arrays.equals(currentBytes, patch.getPatchedBytes())) {
					throw new IllegalStateException("Unexpected bytes for offset " + Long.toHexString(patch.getOffset()).toUpperCase() + " in file \"" + file.getName() + "\"");
				}
			}
		}
		// Validate
		String md5 = Util.md5sum(patched);
		if(md5.equalsIgnoreCase(filePatch.getPatched())) {
			File backup = new File(file.getAbsolutePath() + ".bak");
			Files.move(file.toPath(), backup.toPath(), REPLACE_EXISTING);
			Files.move(patched.toPath(), file.toPath(), REPLACE_EXISTING);
		} else {
			Files.delete(patched.toPath());
			throw new IllegalStateException("Patched file \"" + file.getName() + "\" failed validation check. Expected " + filePatch.getPatched() + " but got " + md5);
		}
	}
	
	private static Set<Entry<File, FilePatch>> filterFilePatches(String installDir, Map<String, FilePatch> filePatches) throws IOException {
		Set<Entry<File, FilePatch>> patchesToApply = new HashSet<Entry<File, FilePatch>>();
		for(Entry<String, FilePatch> entry : filePatches.entrySet()) {
			File file = new File(installDir, entry.getKey());
			FilePatch patch = entry.getValue();
			String md5 = Util.md5sum(file);
			FilePatch resolved = resolvePatch(md5, patch);
			if(resolved == null) {
				throw new IllegalStateException("Corrupted file \"" + entry.getKey() + "\"");
			}
			if(md5.equalsIgnoreCase(resolved.getVanilla()) || resolved.getMigrations().stream().anyMatch(md5::equalsIgnoreCase)) {
				patchesToApply.add(new SimpleEntry<File, FilePatch>(file, resolved));
			}
		}
		return patchesToApply;
	}
	
	private static FilePatch resolvePatch(String md5, FilePatch patch) {
		if(matchesAny(md5, patch)) {
			return patch;
		}
		for(FilePatch alt : patch.getAlternatives()) {
			if(matchesAny(md5, alt)) {
				return alt;
			}
		}
		return null;
	}
	
	private static boolean matchesAny(String md5, FilePatch patch) {
		return md5.equalsIgnoreCase(patch.getVanilla())
			|| md5.equalsIgnoreCase(patch.getPatched())
			|| patch.getMigrations().stream().anyMatch(md5::equalsIgnoreCase);
	}
	
	public static Optional<Map<String, FilePatch>> loadFilePatches(Semver ts6Version) throws Exception {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("patches.json");
		Map<String, TS6Patch> patches = OBJECT_MAPPER.readValue(inputStream, new TypeReference<Map<String, TS6Patch>>() {});
		if(!patches.containsKey(ts6Version.toString())) {
			return Optional.empty();
		}
		return patches.get(ts6Version.toString()).getFilePatches(OS.getOrThrow());
	}
	
	public static DecodedPatch decodePatch(Patch patch) {
		long offset = Long.parseLong(patch.getOffset(), 16);
		byte[] vanillaBytes = hexToBytes(patch.getVanilla());
		byte[] patchedBytes = hexToBytes(patch.getPatched());
		return new DecodedPatch(offset, vanillaBytes, patchedBytes);
	}
	
	private static byte[] hexToBytes(String hex) {
		String[] hexes = hex.split(" ");
		byte[] result = new byte[hexes.length];
		for(int x = 0; x < result.length; x++) {
			result[x] = (byte) Integer.parseInt(hexes[x], 16);
		}
		return result;
	}
	
	public static class DecodedPatch {
		private final long offset;
		private final byte[] vanilla;
		private final byte[] patched;
		
		public DecodedPatch(long offset, byte[] vanilla, byte[] patched) {
			this.offset = offset;
			this.vanilla = vanilla;
			this.patched = patched;
		}
		
		public long getOffset() {
			return this.offset;
		}
		
		public byte[] getVanillaBytes() {
			return this.vanilla;
		}
		
		public byte[] getPatchedBytes() {
			return this.patched;
		}
	}
	
	public static class TS6Patch {
		private final Map<String, FilePatch> windows;
		private final Map<String, FilePatch> linux;
		private final Map<String, FilePatch> macos;
		
		public TS6Patch(
			@JsonProperty("windows") Map<String, FilePatch> windows,
			@JsonProperty("linux") Map<String, FilePatch> linux,
			@JsonProperty("macos") Map<String, FilePatch> macos
		) {
			this.windows = windows;
			this.linux = linux;
			this.macos = macos;
		}
		
		@JsonIgnore
		public Optional<Map<String, FilePatch>> getFilePatches(OS os) {
			Map<String, FilePatch> result = null;
			switch(os) {
				case WINDOWS:
					result = this.windows;
					break;
				case LINUX:
					result = this.linux;
					break;
				case MAC_OS:
					result = this.macos;
					break;
			}
			return Optional.ofNullable(result);
		}
		
		public static class FilePatch {
			private final String vanilla;
			private final String patched;
			private final List<String> migrations;
			private final List<Patch> patches;
			private final List<FilePatch> alternatives;

			public FilePatch(
				@JsonProperty("vanilla") String vanilla,
				@JsonProperty("patched") String patched,
				@JsonSetter(nulls = Nulls.AS_EMPTY) @JsonProperty("migrations") List<String> migrations,
				@JsonProperty("patches") List<Patch> patches,
				@JsonSetter(nulls = Nulls.AS_EMPTY) @JsonProperty("alternatives") List<FilePatch> alternatives
			) {
				this.vanilla = vanilla;
				this.patched = patched;
				this.migrations = migrations;
				this.patches = patches;
				this.alternatives = alternatives;
			}
			
			public String getVanilla() {
				return this.vanilla;
			}
			
			public String getPatched() {
				return this.patched;
			}
			
			public List<String> getMigrations() {
				return this.migrations;
			}
			
			public List<Patch> getPatches() {
				return this.patches;
			}

			public List<FilePatch> getAlternatives() {
				return this.alternatives;
			}

			@Override
			public int hashCode() {
				return Objects.hash(this.vanilla, this.patched, this.migrations, this.patches, this.alternatives);
			}
			
			public static class Patch {
				private final String offset;
				private final String vanilla;
				private final String patched;
				
				public Patch(
					@JsonProperty("offset") String offset,
					@JsonProperty("vanilla") String vanilla,
					@JsonProperty("patched") String patched
				) {
					this.offset = offset;
					this.vanilla = vanilla;
					this.patched = patched;
				}
				
				public String getOffset() {
					return this.offset;
				}
				
				public String getVanilla() {
					return this.vanilla;
				}
				
				public String getPatched() {
					return this.patched;
				}
				
				@Override
				public int hashCode() {
					return Objects.hash(this.offset, this.vanilla, this.patched);
				}
			}
		}
	}
}
