package com.github.ennoxhd.update.gradle.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
		Config config = configOpt.get();
		System.out.println("Running with " + config);
		try {
			walkFolders(config, UpdateGradle::checkForGradleWrapper, UpdateGradle::updateGradle);
		} catch (Exception e) {
			e.printStackTrace();
			help();
		}	
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
	
	// TODO: exclude this project from running the update process on its own
	private static void walkFolders(final Config config, final Function<Path, Boolean> condition, final BiConsumer<Path, Boolean> conditionalAction) throws IOException {
		Objects.requireNonNull(config);
		Files.walk(config.getFolder(), 1)
				.filter(path -> {
					final String name = path.getFileName().toString();
					return path.toFile().isDirectory() && !Config.excludedFolders.contains(name) && path != config.getFolder();
				})
				.forEach(path -> {
					conditionalAction.accept(path, condition.apply(path));
					if(config.isRecursive()) {
						try {
							walkFolders(new Config(true, path, config.getVersion()), condition, conditionalAction);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
	}
	 
	private static boolean checkForGradleWrapper(final Path folder) {
		try {
			return folder != null
					&& folder.toFile().isDirectory()
					&& folder.resolve("gradlew").toFile().isFile()
					&& folder.resolve("gradlew.bat").toFile().isFile()
					&& folder.resolve("gradle").toFile().isDirectory()
					&& folder.resolve("gradle/wrapper").toFile().isDirectory()
					&& folder.resolve("gradle/wrapper/gradle-wrapper.jar").toFile().isFile()
					&& folder.resolve("gradle/wrapper/gradle-wrapper.properties").toFile().isFile();
		} catch(Exception e) {
			return false;
		}
	}
	
	// TODO: implement
	//Notes:
	//for all projects do
	//.\gradlew.bat wrapper --gradle-version=6.6.1
	//.\gradlew.bat tasks
	private static void updateGradle(final Path folder, final Boolean doUpdate) {
		if(doUpdate) {
			System.out.println("Updates: " + folder);
		} else {
			System.out.println("Doesn't update: " + folder);
		}
	}
}
