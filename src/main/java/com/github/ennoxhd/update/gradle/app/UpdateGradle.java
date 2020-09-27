package com.github.ennoxhd.update.gradle.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	private static void walkFolders(final Config config, final Function<Path, Boolean> condition,
			final BiConsumer<Config, Boolean> conditionalAction) throws IOException {
		Objects.requireNonNull(config);
		Files.walk(config.getFolder(), 1)
				.filter(path -> {
					final String name = path.getFileName().toString();
					return path.toFile().isDirectory()
							&& !Config.excludedFolders.contains(name)
							&& path != config.getFolder();
				})
				.forEach(path -> {
					conditionalAction.accept(new Config(false, path, config.getVersion()), condition.apply(path));
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
					&& !folder.toAbsolutePath().normalize().toString().equals(
							Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString())
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
	
	private static void updateGradle(final Config config, final Boolean doUpdate) {
		final String folder = config.getFolder().toAbsolutePath().normalize().toString();
		if(doUpdate) {
			System.out.println("Updating at: " + folder);
			final File folderFile = new File(folder);
			final String wrapper = isWindows() ? "gradlew.bat" : "gradlew";
			boolean successful = true;
			try {
				final Process updateProcess = new ProcessBuilder(new String[] {
						wrapper, "wrapper",
						"--gradle-version", config.getVersion()
				}).directory(folderFile).start();
				updateProcess.waitFor();
				checkExitCode(updateProcess.exitValue());
				final Process versionProcess = new ProcessBuilder(new String[] {
						wrapper,
						"--version"
				}).directory(folderFile).start();
				versionProcess.waitFor();
				checkExitCode(versionProcess.exitValue());
			} catch (IOException | InterruptedException | RuntimeException e) {
				successful = false;
				System.out.println("             Went wrong with error:");
				e.printStackTrace(System.out);
			}
			if(successful) System.out.println("             Done!");
		} else {
			System.out.println("    Skipped: " + folder);
		}
	}
	
	private static boolean isWindows() {
		final String osName = System.getProperty("os.name");
		return osName != null
				&& osName.toLowerCase().contains("windows");
	}
	
	private static void checkExitCode(final int exitCode) throws RuntimeException {
		if(exitCode != 0) {
			throw new RuntimeException("Abnormal termination with exit code " + exitCode + "!");
		}
	}
}
