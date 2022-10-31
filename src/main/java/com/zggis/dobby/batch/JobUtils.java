package com.zggis.dobby.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.dto.mediainfo.TrackDTO;

public class JobUtils {

	private static final Logger logger = LoggerFactory.getLogger(JobUtils.class);

	public static void printOutput(Process p) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
			int c;
			while ((c = isr.read()) >= 0) {
				builder.append((char) c);
			}
		}
		try {
			p.waitFor();
			logger.info(builder.toString());
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			logger.info(e.getMessage());
		}
	}

	public static String getWithoutPathAndExtension(String fullFilename) {
		return fullFilename.substring(fullFilename.lastIndexOf('/') + 1, fullFilename.lastIndexOf('.'));
	}

	public static String getWithoutPath(String fullFilename) {
		return fullFilename.substring(fullFilename.lastIndexOf('/') + 1);
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

	public static String getFrameRate(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return track.frameRate;
			}
		}
		return null;
	}

	public static int getHeight(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return Integer.parseInt(track.height);
			}
		}
		return -1;
	}

	public static int getWidth(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return Integer.parseInt(track.width);
			}
		}
		return -1;
	}

	public static String getResolution(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				String result = track.width + "x" + track.height;
				if ("3840x21601920x1080".equals(result)) {
					return "3840x2160 DL";
				} else {
					return result;
				}
			}
		}
		return null;
	}

	public static String getHDRFormat(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return track.hDR_Format;
			}
		}
		return null;
	}

}
