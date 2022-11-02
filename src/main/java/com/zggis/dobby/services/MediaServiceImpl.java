package com.zggis.dobby.services;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaServiceImpl {

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${results.dir}")
	private String resultsDir;

	@Value("${temp.dir}")
	private String tempDir;

	@Value("${cleanup:true}")
	private boolean cleanup;

	public String getMediaDirectory() {
		return mediaDir;
	}

	public String getTempDirectory() {
		return tempDir + "/";
	}

	public String getResultsDirectory() {
		return resultsDir + "/";
	}

	public void createTempDirectory() {
		createDirectory(tempDir);
	}

	public void deleteTempDirectory() {
		if (cleanup) {
			deleteDirectory(tempDir);
		}
	}

	private static void createDirectory(String directoryStr) {
		File directory = new File(directoryStr);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	private static void deleteDirectory(String directoryName) {
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
