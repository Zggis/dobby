package com.zggis.dobby.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JobUtils {

	public static void printOutput(Process p) throws IOException {
		try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
			int c;
			while ((c = isr.read()) >= 0) {
				System.out.print((char) c);
				System.out.flush();
			}
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
	}

	public static String getWithoutExtension(String fullFilename) {
		return fullFilename.substring(0, fullFilename.lastIndexOf('.'));
	}

	public static String getWithoutPathAndExtension(String fullFilename) {
		return fullFilename.substring(fullFilename.lastIndexOf('/') + 1, fullFilename.lastIndexOf('.'));
	}

	public static boolean doesMediaFileExists(String filename) {
		File file = new File(filename);
		if (file.exists() && !file.isDirectory()) {
			Path path = Paths.get(filename);
			try {
				long bytes = Files.size(path);
				return bytes > 10000;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void createDirectory(String directoryStr) {
		File directory = new File(directoryStr);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public static String returnOutput(Process p) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
			int c;
			while ((c = isr.read()) >= 0) {
				builder.append((char) c);
			}
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		return builder.toString();
	}

	public static void deleteDirectory(String directoryName) {
		File directory = new File(directoryName);
		if (directory.exists()) {
			File[] directoryListing = directory.listFiles();
			if (directoryListing != null) {
				for (File file : directoryListing) {
					file.delete();
				}
			}
			directory.delete();
		}

	}
}
