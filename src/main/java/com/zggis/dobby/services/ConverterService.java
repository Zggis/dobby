package com.zggis.dobby.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zggis.dobby.dto.VideoFileDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.dto.mediainfo.TrackDTO;

@Component
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
				if (validateMergeCompatibility(standardFilename, standardMediaInfo)) {
					generateHevcFromMP4(dolbyVisionFilename);
					generateRPU(dolbyVisionFilename);
					generateHevcFromMKV(standardFilename);
					injectRPU(dolbyVisionFilename, standardFilename);
					createDirectory("results");
					String frameRate = getFrameRate(standardMediaInfo);
					generateMKV(standardFilename, frameRate);
				}
				deleteDirectory("temp");
			}
		}

	}

	private boolean validateMergeCompatibility(String standardFilename, MediaInfoDTO standardMediaInfo) {
		String resolution = getResolution(standardMediaInfo);
		if ("3840x2160 DL".equals(resolution)) {
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
				resolution, hdrFormat, frameRate);
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

	// Step 1
	private void generateHevcFromMP4(String dvFilename) throws IOException {
		logger.info("Generating HEVC file from {}...", dvFilename);
		String CMD = MP4EXTRACT + " -raw 1 -out \"" + mediaDir + "/temp/" + getWithoutExtension(dvFilename)
				+ ".hevc\" \"" + mediaDir + "/" + dvFilename + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
	}

	// Step 2
	private void generateRPU(String dolbyVisionFilename) throws IOException {
		logger.info("Generating RPU file from {}...", dolbyVisionFilename);
		String cmd = DOVI_TOOL + " -m 2 extract-rpu \"" + mediaDir + "/temp/" + getWithoutExtension(dolbyVisionFilename)
				+ ".hevc\"" + " -o \"" + mediaDir + "/temp/" + getWithoutExtension(dolbyVisionFilename) + "-RPU.bin\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
	}

	// Step 3
	private void generateHevcFromMKV(String stdFilename) throws IOException {
		logger.info("Generating HEVC file from {}...", stdFilename);
		String cmd = MKVEXTRACT + " \"" + mediaDir + "/" + stdFilename + "\" tracks 0:\"" + mediaDir + "/temp/"
				+ getWithoutExtension(stdFilename) + ".hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
	}

	// Step 4
	private void injectRPU(String dolbyVisionFilename, String standardFilename) throws IOException {
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
	}

//Step 5
	private void generateMKV(String standardFilename, String frameRate) throws IOException {
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
				+ "\" --language 0:und --compression 0:none " + duration + " \"" + mediaDir + "/temp/" + blrpu
				+ "\" --track-order 1:0";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		printOutput(p);
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
					VideoFileDTO newFile = new VideoFileDTO();
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
