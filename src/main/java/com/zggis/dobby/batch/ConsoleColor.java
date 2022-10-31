package com.zggis.dobby.batch;

public enum ConsoleColor {
	YELLOW("\u001B[33m"), RED("	\u001B[31m"), GREEN("\u001B[32m"), BLUE("\u001B[34m"), NONE("\u001B[0m"),
	CYAN("\u001B[36m");

	public final String value;

	private ConsoleColor(String label) {
		this.value = label;
	}
}
