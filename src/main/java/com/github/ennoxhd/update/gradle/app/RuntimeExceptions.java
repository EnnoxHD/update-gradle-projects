package com.github.ennoxhd.update.gradle.app;

import java.util.Objects;

enum RuntimeExceptions {
	NO_VERSION_IN_ARGS("No gradle version specified!\n"
			+ "Please run with --args '<gradle-version>' where <gradle-version> is the new version to update to.\n"
			+ "(e.g. 6.5.1 or 6.6)");

	private String message;

	private String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		Objects.requireNonNull(message);
		this.message = message;
	}

	private RuntimeExceptions(String message) {
		setMessage(message);
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
