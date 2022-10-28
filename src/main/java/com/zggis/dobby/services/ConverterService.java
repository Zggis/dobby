package com.zggis.dobby.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zggis.dobby.dto.ActiveAreaDTO;
import com.zggis.dobby.dto.BorderInfoDTO;
import com.zggis.dobby.dto.OldVideoFileDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.dto.mediainfo.TrackDTO;

public class ConverterService {

	private static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

	private static final String DEFAULT_RESOLUTION = "24.000";

	@Value("${media.dir}")
	private String mediaDir;

	@Value("${dovi.tool.location}")
	private String DOVI_TOOL;

	@Value("${mkvextract.location}")
	private String MKVEXTRACT;

	@Value("${mkvmerge.location}")
	private String MKVMERGE;

	@Value("${mp4extract.location}")
	private String MP4EXTRACT;

	@Value("${mediainfo.location}")
	private String MEDIAINFO;

	@Value("${ffmpeg.location}")
	private String FFMPEG;

	@Autowired
	private DoviProcessBuilder pbservice;

	private Map<String, String> showMap = new HashMap<>();

	private Map<String, String> dvShowMap = new HashMap<>();

	private static final Pattern EPISODE_NUM_REGEX = Pattern.compile("^.*?s(\\d{2})((?:e\\d{2})+).*");

	private static final Pattern DOLBY_VISION_REGEX = Pattern.compile("^.*?dv.*");

	private static final Map<String, String> fpsMap;

	static {
		fpsMap = new HashMap<>();
		fpsMap.put("23.976", "--default-duration 0:24000/1001p --fix-bitstream-timing-information 0:1");
		fpsMap.put(DEFAULT_RESOLUTION, "--default-duration 0:24p --fix-bitstream-timing-information 0:1");
		fpsMap.put("25.000", "--default-duration 0:25p --fix-bitstream-timing-information 0:1");
		fpsMap.put("30.000", "--default-duration 0:30p --fix-bitstream-timing-information 0:1");
		fpsMap.put("48.000", "--default-duration 0:48p --fix-bitstream-timing-information 0:1");
		fpsMap.put("50.000", "--default-duration 0:50p --fix-bitstream-timing-information 0:1");
		fpsMap.put("60.000", "--default-duration 0:60p --fix-bitstream-timing-information 0:1");
	}

	@PostConstruct
	public void start() throws IOException {
		scanDirectoryForTVShows();
		for (String key : dvShowMap.keySet()) {
			if (showMap.containsKey(key)) {
				createDirectory("temp");
				String dolbyVisionFilename = dvShowMap.get(key);
				String standardFilename = showMap.get(key);
				MediaInfoDTO standardMediaInfo = getMediaInfo(standardFilename);
				MediaInfoDTO dolbyVisionMediaInfo = getMediaInfo(dolbyVisionFilename);
				if (validateMergeCompatibility(standardFilename, standardMediaInfo, dolbyVisionFilename,
						dolbyVisionMediaInfo)) {
					if (!generateHevcFromMP4(dolbyVisionFilename)) {
						logger.error("Unable to convert {} to HEVC. Aborting conversion.", dolbyVisionFilename);
						deleteDirectory("temp");
						continue;
					}
					if (!generateRPU(dolbyVisionFilename)) {
						logger.error("Unable to generate RPU from {} Aborting conversion.", dolbyVisionFilename);
						deleteDirectory("temp");
						continue;
					}
					ActiveAreaDTO activeArea = getMKVBorderInfo(standardFilename, standardMediaInfo);
					BorderInfoDTO rpuBorderInfo = getRPUBorderInfo(dolbyVisionFilename);
					if (validateActiveArea(activeArea, rpuBorderInfo, standardMediaInfo, standardFilename,
							dolbyVisionFilename)) {
						if (!generateHevcFromMKV(standardFilename)) {
							logger.error("Unable to convert {} to HEVC Aborting conversion.", standardFilename);
							deleteDirectory("temp");
							continue;
						}
						if (!injectRPU(dolbyVisionFilename, standardFilename)) {
							logger.error("Unable to inject RPU into {}.hevc Aborting conversion.",
									getWithoutExtension(standardFilename));
							deleteDirectory("temp");
							continue;
						}
						createDirectory("results");
						String frameRate = getFrameRate(standardMediaInfo);
						if (!generateMKV(standardFilename, frameRate)) {
							logger.error("Unable to convert HEVC to MKV Aborting conversion.");
							deleteDirectory("temp");
							continue;
						}

					}
				}
				deleteDirectory("temp");
			}
		}
		logger.info("Operations complete, closing in 30s.");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean validateActiveArea(ActiveAreaDTO activeArea, BorderInfoDTO rpuBorderInfo,
			MediaInfoDTO standardMediaInfo, String standardFilename, String dolbyVisionFilename) {
		int rpuActiveHeight = getHeight(standardMediaInfo)
				- (rpuBorderInfo.getBottomOffset() + rpuBorderInfo.getTopOffset());
		int matches = 0;
		for (int item : activeArea.getActiveAreaHeights()) {
			if (item == rpuActiveHeight) {
				matches++;
			}
		}
		Double heightPercent = ((double) matches / (double) activeArea.getActiveAreaHeights().size()) * 100;
		if (matches < (activeArea.getActiveAreaHeights().size() * 0.6)) {
			logger.error("Active area height failed to pass matching threshold {}%, aborting merge.", heightPercent);
			return false;
		}
		int rpuActiveWidth = getWidth(standardMediaInfo)
				- (rpuBorderInfo.getLeftOffset() + rpuBorderInfo.getRightOffset());
		int widthMatches = 0;
		for (int item : activeArea.getActiveAreaWidths()) {
			if (item == rpuActiveWidth) {
				widthMatches++;
			}
		}
		Double widthPercent = ((double) widthMatches / (double) activeArea.getActiveAreaWidths().size()) * 100;
		if (widthMatches < (activeArea.getActiveAreaWidths().size() * 0.6)) {
			logger.error("Active area width failed to pass matching threshold {}%, aborting merge.", widthPercent);
			return false;
		}
		logger.info("Active areas match [Height:{}% Width:{}%], proceeding with merge...", heightPercent.intValue(),
				widthPercent.intValue());
		return true;
	}

	private boolean validateMergeCompatibility(String standardFilename, MediaInfoDTO standardMediaInfo,
			String dolbyVisionFilename, MediaInfoDTO dolbyVisionMediaInfo) {
		String standardResolution = getResolution(standardMediaInfo);
		String dolbyVisionResolution = getResolution(dolbyVisionMediaInfo);
		if (!standardResolution.equals(dolbyVisionResolution)) {
			logger.error("Resolutions do not match, DV:{}, HDR:{}", dolbyVisionResolution, standardResolution);
			return false;
		}
		if ("3840x2160 DL".equals(standardResolution)) {
			logger.error("{} - No Support for Double Layer Profile 7 File", standardFilename);
			return false;
		}
		String hdrFormat = getHDRFormat(standardMediaInfo);
		if (hdrFormat != null) {
			if (hdrFormat.toLowerCase().contains("dvhe.05")) {
				logger.error("{} - Dolby Vision Profile 5 found.", standardFilename);
				return false;
			}
		} else {
			logger.error("{} - No HDR format detected.", standardFilename);
			return false;
		}
		String frameRate = getFrameRate(standardMediaInfo);
		if (frameRate == null) {
			logger.error("{} - Could not determine Frame Rate of", standardFilename);
			return false;
		}
		logger.info("\n{}\nResolution:\t{}\tGOOD\nHDR Format:\t{}\tGOOD\nFrame Rate:\t{}\t\tGOOD", standardFilename,
				standardResolution, hdrFormat, frameRate);
		return true;
	}

	private MediaInfoDTO getMediaInfo(String standardFilename) throws IOException {
		logger.info("Fetching media info from {}...", standardFilename);
		String CMD = MEDIAINFO + " --output=JSON \"" + mediaDir + "/" + standardFilename + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		String output = returnOutput(p);
		ObjectMapper objectMapper = new ObjectMapper();
		MediaInfoDTO mediaInfo = objectMapper.readValue(output, MediaInfoDTO.class);
		return mediaInfo;
	}

	private ActiveAreaDTO getMKVBorderInfo(String standardFilename, MediaInfoDTO standardMediaInfo) throws IOException {
		logger.info("Fetching Border info from {}...", standardFilename);
		double duration = -1;
		for (TrackDTO track : standardMediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				duration = Double.parseDouble(track.duration);
			}
		}
		List<Integer> activeAreaHeights = new ArrayList<>();
		List<Integer> activeAreaWidths = new ArrayList<>();
		for (double i = 0.2; i <= 0.8; i += 0.1) {
			String CMD = FFMPEG + " -ss 00:" + (int) ((duration * i) / 60) + ":00 -i \"" + mediaDir + "/"
					+ standardFilename + "\" -vf cropdetect -frames:v 400 -f null -";
			logger.debug(CMD);
			ProcessBuilder pb = pbservice.get(CMD);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			String infoOutput = returnOutput(p);
			String[] lines = infoOutput.split("\n");
			for (String line : lines) {
				try {
					if (line.startsWith("[Parsed_cropdetect")) {
						String[] tokens = line.split(" ");
						if (tokens.length < 6) {
							logger.error("No value for Parsed_cropdetect");
							return null;
						}
						String strBotVal = tokens[5].split(":")[1].trim();
						String strLeftVal = tokens[3].split(":")[1].trim();
						activeAreaHeights.add(getHeight(standardMediaInfo) - (Integer.parseInt(strBotVal) * 2));
						activeAreaWidths.add(getWidth(standardMediaInfo) - (2 * +Integer.parseInt(strLeftVal)));
						logger.debug("Adding {} as bottom value", strBotVal);
						logger.debug("Adding {} as left value", strLeftVal);
					}
				} catch (NumberFormatException e) {
					logger.error("Cannot parse offset value");
					return null;
				}
			}
		}
		ActiveAreaDTO result = new ActiveAreaDTO();
		result.setActiveAreaHeights(activeAreaHeights);
		result.setActiveAreaWidths(activeAreaWidths);
		return result;
	}

	private BorderInfoDTO getRPUBorderInfo(String dolbyVisionFilename) throws IOException {
		String rpu = getWithoutExtension(dolbyVisionFilename) + "-RPU.bin";
		logger.info("Fetching Border info from {}...", rpu);
		String CMD = DOVI_TOOL + " info -f 123 -i \"" + mediaDir + "/temp/" + rpu + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		BorderInfoDTO result = new BorderInfoDTO();
		String infoOutput = returnOutput(p);
		String[] lines = infoOutput.split("\n");
		for (String line : lines) {
			try {
				if (line.contains("active_area_top_offset")) {
					if (line.split(":").length < 2) {
						logger.error("No value for active_area_top_offset");
						return null;
					}
					String strVal = line.split(":")[1].replaceAll(",", "").trim();
					result.setTopOffset(Integer.parseInt(strVal));
				} else if (line.contains("active_area_bottom_offset")) {
					if (line.split(":").length < 2) {
						logger.error("No value for active_area_bottom_offset");
						return null;
					}
					String strVal = line.split(":")[1].replaceAll(",", "").trim();
					result.setBottomOffset(Integer.parseInt(strVal));
				} else if (line.contains("active_area_left_offset")) {
					if (line.split(":").length < 2) {
						logger.error("No value for active_area_left_offset");
						return null;
					}
					String strVal = line.split(":")[1].replaceAll(",", "").trim();
					result.setLeftOffset(Integer.parseInt(strVal));
				} else if (line.contains("active_area_right_offset")) {
					if (line.split(":").length < 2) {
						logger.error("No value for active_area_right_offset");
						return null;
					}
					String strVal = line.split(":")[1].replaceAll(",", "").trim();
					result.setRightOffset(Integer.parseInt(strVal));
				}
			} catch (NumberFormatException e) {
				logger.error("Cannot parse offset value");
				return null;
			}
		}
		return result;
	}

	// Step 1
	private boolean generateHevcFromMP4(String dvFilename) throws IOException {
		logger.info("Generating HEVC file from {}...", dvFilename);
		String CMD = MP4EXTRACT + " -raw 1 -out \"" + mediaDir + "/temp/" + getWithoutExtension(dvFilename)
				+ ".hevc\" \"" + mediaDir + "/" + dvFilename + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
		return doesMediaFileExists(mediaDir + "/temp/" + getWithoutExtension(dvFilename) + ".hevc");
	}

	// Step 2
	private boolean generateRPU(String dolbyVisionFilename) throws IOException {
		logger.info("Generating RPU file from {}...", dolbyVisionFilename);
		String cmd = DOVI_TOOL + " -m 2 extract-rpu \"" + mediaDir + "/temp/" + getWithoutExtension(dolbyVisionFilename)
				+ ".hevc\"" + " -o \"" + mediaDir + "/temp/" + getWithoutExtension(dolbyVisionFilename) + "-RPU.bin\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
		return doesMediaFileExists(mediaDir + "/temp/" + getWithoutExtension(dolbyVisionFilename) + "-RPU.bin");
	}

	// Step 3
	private boolean generateHevcFromMKV(String stdFilename) throws IOException {
		logger.info("Generating HEVC file from {}...", stdFilename);
		String cmd = MKVEXTRACT + " \"" + mediaDir + "/" + stdFilename + "\" tracks 0:\"" + mediaDir + "/temp/"
				+ getWithoutExtension(stdFilename) + ".hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
		return doesMediaFileExists(mediaDir + "/temp/" + getWithoutExtension(stdFilename) + ".hevc");
	}

	// Step 4
	private boolean injectRPU(String dolbyVisionFilename, String standardFilename) throws IOException {
		String rpu = getWithoutExtension(dolbyVisionFilename) + "-RPU.bin";
		String std = getWithoutExtension(standardFilename) + ".hevc";
		logger.info("Injecting {} into {}", rpu, std);
		String cmd = DOVI_TOOL + " inject-rpu -i \"" + mediaDir + "/temp/" + std + "\" --rpu-in \"" + mediaDir
				+ "/temp/" + rpu + "\" -o \"" + mediaDir + "/temp/" + getWithoutExtension(standardFilename)
				+ "[BL+RPU].hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
		return doesMediaFileExists(mediaDir + "/temp/" + getWithoutExtension(standardFilename) + "[BL+RPU].hevc");
	}

//Step 5
	private boolean generateMKV(String standardFilename, String frameRate) throws IOException {
		String duration = null;
		if (fpsMap.containsKey(frameRate)) {
			duration = fpsMap.get(frameRate);
		} else {
			duration = fpsMap.get(DEFAULT_RESOLUTION);
		}
		String blrpu = getWithoutExtension(standardFilename) + "[BL+RPU].hevc";
		logger.info("Generating MKV file from {}...", blrpu);
		String cmd = MKVMERGE + " --output \"" + mediaDir + "/results/" + getWithoutExtension(standardFilename)
				+ "[BL+RPU].mkv\"" + " --no-video \"" + mediaDir + "/" + standardFilename
				+ "\" --language 0:und --track-order 1:0 --compression 0:none " + duration + " \"" + mediaDir + "/temp/"
				+ blrpu + "\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
		return doesMediaFileExists(mediaDir + "/results/" + getWithoutExtension(standardFilename) + "[BL+RPU].mkv");
	}

	private String getFrameRate(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return track.frameRate;
			}
		}
		return null;
	}

	private String getResolution(MediaInfoDTO mediaInfo) {
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

	private int getHeight(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return Integer.parseInt(track.height);
			}
		}
		return -1;
	}

	private int getWidth(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return Integer.parseInt(track.width);
			}
		}
		return -1;
	}

	private String getHDRFormat(MediaInfoDTO mediaInfo) {
		for (TrackDTO track : mediaInfo.media.track) {
			if ("1".equals(track.iD)) {
				return track.hDR_Format;
			}
		}
		return null;
	}

	private void createDirectory(String name) {
		String PATH = mediaDir;
		String directoryName = PATH.concat("/" + name);
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	private boolean doesMediaFileExists(String filename) {
		File file = new File(filename);
		if (file.exists() && !file.isDirectory()) {
			Path path = Paths.get(filename);
			try {
				long bytes = Files.size(path);
				return bytes > 10000;
			} catch (IOException e) {
				logger.error("Unable to determine size of {}", filename);
			}
		}
		return false;
	}

	private void deleteDirectory(String name) {
		String PATH = mediaDir;
		String directoryName = PATH.concat("/" + name);
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

	private void printOutput(Process p) throws IOException {
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

	private String returnOutput(Process p) throws IOException {
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

	private String getWithoutExtension(String fullFilename) {
		return fullFilename.substring(0, fullFilename.lastIndexOf('.'));
	}

	private void scanDirectoryForTVShows() {
		logger.info("Scanning {} for TV shows...", mediaDir);
		File dir = new File(mediaDir);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.getName().endsWith(".mkv") || child.getName().endsWith(".mp4")) {
					logger.debug("Checking {}", child.getName());
					Matcher m = EPISODE_NUM_REGEX.matcher(child.getName().toLowerCase());
					OldVideoFileDTO newFile = new OldVideoFileDTO();
					newFile.setFullName(child.getName());
					if (m.find()) {
						newFile.setSeason(m.group(1));
						newFile.setEpisode(m.group(2).substring(1, m.group(2).length()).replace("e", "-"));
					}
					Matcher m2 = DOLBY_VISION_REGEX.matcher(child.getName().toLowerCase());
					if (m2.find()) {
						newFile.setDolbyVision(true);
					}
					if (newFile.getSeason() != null && newFile.getEpisode() != null) {
						if (newFile.isDolbyVision()) {
							logger.debug("Added DV file {}", child.getName());
							dvShowMap.put(newFile.getSeason() + newFile.getEpisode(), newFile.getFullName());
						} else {
							logger.debug("Added file {}", child.getName());
							showMap.put(newFile.getSeason() + newFile.getEpisode(), newFile.getFullName());
						}
					}
				}
			}
		} else {
			logger.error("{} Does not exist.", mediaDir);
		}
	}

}
