package com.zggis.dobby.services;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaServiceImpl {

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${cleanup:true}")
	private boolean cleanup;

	public String getMediaDirectory() {
		return mediaDir;
	}

	public String getTempDirectory() {
		return mediaDir + "/temp/";
	}

	public String getResultsDirectory() {
		return mediaDir + "/results/";
	}

	public void createTempDirectory() {
		createDirectory(mediaDir + "/temp");
	}

	public void deleteTempDirectory() {
		if (cleanup) {
			deleteDirectory(mediaDir + "/temp");
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
