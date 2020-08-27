package com.github.ennoxhd.update.gradle.app;

import java.nio.file.Path;
import java.util.Optional;

public class UpdateGradle {

	public static void main(String[] args) {
		Optional<Config> configOpt = Optional.empty();
		try {
			configOpt = analyseArgs(args);
		} catch (Exception e) {
			e.printStackTrace();
			help();
		}
		if(configOpt.isEmpty())
			help();
		System.out.println(configOpt.get());
		
		// TODO: implement
		
		//Notes:
		//for all projects do
		//.\gradlew.bat wrapper --gradle-version=6.6.1
		//.\gradlew.bat tasks
		
		System.exit(0);
	}

	private static void help() {
		System.out.println("Help for UpdateGradle:\n" + "----------------------\n"
				+ "Summary: gradlew run --args=\"[-r] <path> <version>\"\n"
				+ "Example: gradlew run --args=\"-r .. 6.6\"\n\n"
				+ "     Option      -r: recursively check and update all child folders\n"
				+ "Requirement    path: path of the folder where gradle project is located or\n"
				+ "                     with option '-r' where gradle projects are located\n"
				+ "Requirement version: the new gradle version to update the project(s) to");
		System.exit(1);
	}

	private static Optional<Config> analyseArgs(final String[] args) {
		if (args == null || args.length != 2 && args.length != 3)
			return Optional.empty();
		final boolean isRecursive = Config.isRecursiveArg(args[0]);
		final int folderArgIdx = isRecursive ? 1 : 0;
		final int versionArgIdx = folderArgIdx + 1;		
		return Optional.ofNullable(
				new Config(isRecursive, Path.of(args[folderArgIdx]), args[versionArgIdx]));
	}
}
