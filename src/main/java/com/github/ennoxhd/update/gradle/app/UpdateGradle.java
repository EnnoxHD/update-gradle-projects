package com.github.ennoxhd.update.gradle.app;

import java.util.Objects;
import java.util.regex.Pattern;

public class UpdateGradle {

	public static void main(String[] args) {
		if (args == null || args.length != 1)
			runtimeException(RuntimeExceptions.NO_VERSION_IN_ARGS);
		final String version = args[0];
		if (version == null || !isVersionNumber(version))
			runtimeException(RuntimeExceptions.NO_VERSION_IN_ARGS);

		System.out.println("Working on update to version " + version + "!");
		System.out.println("Searching for Gradle projects...");
	}

	private static void runtimeException(RuntimeExceptions message) {
		Objects.requireNonNull(message);
		throw new RuntimeException(message.toString());
	}

	private static boolean isVersionNumber(String version) {
		if (version == null || version.isBlank())
			return false;
		Pattern versionString = Pattern.compile("^[0-9]+\\.[0-9]+(\\.[0-9]+)?$");
		return versionString.matcher(version).find();
	}
}
