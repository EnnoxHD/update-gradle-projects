package com.github.ennoxhd.update.gradle.app;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

class Config {
	
	public static final Set<String> excludedFolders =
			Collections.unmodifiableSet(Set.of(".git", ".svn", ".gradle", "build", "bin", "gradle",
					"src", "resources", ".settings", "tmp", ".github", "submodules"));

	private boolean recursive;
	private Path folder;
	private String version;

	final boolean isRecursive() {
		return recursive;
	}

	private final void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}

	final Path getFolder() {
		return folder;
	}

	private final void setFolder(final Path folder) {
		Objects.requireNonNull(folder);
		if (!exists(folder))
			throw new IllegalArgumentException("Not a valid directory path: " + folder);
		this.folder = folder;
	}

	final String getVersion() {
		return version;
	}

	private final void setVersion(final String version) {
		Objects.requireNonNull(version);
		if (!isVersionNumber(version))
			throw new IllegalArgumentException("Not a x.y[.z] version number: " + version);
		this.version = version;
	}

	Config(final boolean recursive, final Path folder, final String version) {
		setRecursive(recursive);
		setFolder(folder);
		setVersion(version);
	}

	static final boolean isRecursiveArg(final String arg) {
		return "-r".equals(arg);
	}
	
	static final boolean exists(final Path folder) {
		Objects.requireNonNull(folder);
		return folder.toFile().isDirectory();
	}

	static final boolean isVersionNumber(final String version) {
		if (version == null || version.isBlank())
			return false;
		final Pattern versionString = Pattern.compile("^[0-9]+\\.[0-9]+(\\.[0-9]+)?$");
		return versionString.matcher(version).find();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Config))
			return false;
		Config other = (Config) obj;
		return recursive == other.recursive && Objects.equals(folder, other.folder)
				&& Objects.equals(version, other.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recursive, folder, version);
	}

	@Override
	public String toString() {
		return "Config[recursive:" + isRecursive() + ",folder:\"" + getFolder()
				+ "\",version:\"" + getVersion() + "\"]";
	}
}
