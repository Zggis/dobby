package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.TVShowConversionDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MediaInfoProcessor implements ItemProcessor<TVShowConversionDTO, TVShowConversionDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MediaInfoProcessor.class);

	private String MEDIAINFO;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public MediaInfoProcessor(DoviProcessBuilder pbservice, String outputDir, String MEDIAINFO, boolean execute) {
		this.MEDIAINFO = MEDIAINFO;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public TVShowConversionDTO process(TVShowConversionDTO conversion) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		logger.info("Fetching media info from {}...", JobUtils.getWithoutPath(conversion.getStandardFile().getName()));
		String stdInfoCMD = MEDIAINFO + " --output=JSON \"" + conversion.getStandardFile().getName() + "\"";
		logger.debug(stdInfoCMD);
		ProcessBuilder stdPB = pbservice.get(stdInfoCMD);
		stdPB.redirectErrorStream(true);
		MediaInfoDTO stdMediaInfo = null;
		if (execute) {
			Process stdProcess = stdPB.start();
			String stdOutput = JobUtils.returnOutput(stdProcess);
			stdMediaInfo = objectMapper.readValue(stdOutput, MediaInfoDTO.class);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		VideoFileDTO stdFileDTO = new VideoFileDTO(conversion.getStandardFile().getName(), conversion.getKey());
		stdFileDTO.setMediaInfo(stdMediaInfo);
		//
		logger.info("Fetching media info from {}...",
				JobUtils.getWithoutPath(conversion.getDolbyVisionFile().getName()));
		String CMD2 = MEDIAINFO + " --output=JSON \"" + conversion.getDolbyVisionFile().getName() + "\"";
		logger.debug(CMD2);
		ProcessBuilder pb2 = pbservice.get(CMD2);
		pb2.redirectErrorStream(true);
		MediaInfoDTO mediaInfo2 = null;
		if (execute) {
			Process p2 = pb2.start();
			String output2 = JobUtils.returnOutput(p2);
			mediaInfo2 = objectMapper.readValue(output2, MediaInfoDTO.class);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		VideoFileDTO dvFileDTO = new VideoFileDTO(conversion.getDolbyVisionFile().getName(), conversion.getKey());
		dvFileDTO.setMediaInfo(mediaInfo2);
		TVShowConversionDTO tvShowConversionDTO = null;
		if (validateMergeCompatibility(stdFileDTO, dvFileDTO)) {
			tvShowConversionDTO = new TVShowConversionDTO(conversion.getKey(), stdFileDTO, dvFileDTO);
		} else {
			tvShowConversionDTO = new TVShowConversionDTO(conversion.getKey(), null, null);
		}
		return tvShowConversionDTO;
	}

	private boolean validateMergeCompatibility(VideoFileDTO standardFile, VideoFileDTO dolbyVisionFile) {
		String standardResolution = JobUtils.getResolution(standardFile.getMediaInfo());
		String dolbyVisionResolution = JobUtils.getResolution(dolbyVisionFile.getMediaInfo());
		if (!standardResolution.equals(dolbyVisionResolution)) {
			logger.error(ConsoleColor.RED.value + "Resolutions do not match, DV:{}, HDR:{}" + ConsoleColor.NONE.value,
					dolbyVisionResolution, standardResolution);
			return false;
		}
		if ("3840x2160 DL".equals(standardResolution)) {
			logger.error(ConsoleColor.RED.value + "{} cannot support double layer profile 7" + ConsoleColor.NONE.value,
					JobUtils.getWithoutPath(standardFile.getName()));
			return false;
		}
		String hdrFormat = JobUtils.getHDRFormat(standardFile.getMediaInfo());
		if (hdrFormat == null) {
			logger.error(ConsoleColor.RED.value + "No HDR format detected for {}" + ConsoleColor.NONE.value,
					JobUtils.getWithoutPath(standardFile.getName()));
			return false;
		}
		String frameRate = JobUtils.getFrameRate(standardFile.getMediaInfo());
		if (frameRate == null) {
			logger.error(ConsoleColor.RED.value + "Could not determine Frame Rate of {}" + ConsoleColor.NONE.value,
					JobUtils.getWithoutPath(standardFile.getName()));
			return false;
		}
		logger.info(
				"\n{}\nResolution:\t{}\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value
						+ "\nHDR Format:\t{}\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value
						+ "\nFrame Rate:\t{}\t\t" + ConsoleColor.GREEN.value + "GOOD" + ConsoleColor.NONE.value + "",
				JobUtils.getWithoutPath(standardFile.getName()), standardResolution, hdrFormat, frameRate);
		return true;
	}

}
