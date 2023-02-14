package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MediaInfoProcessor implements ItemProcessor<VideoFileDTO, VideoFileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MediaInfoProcessor.class);

	private final String MEDIAINFO;

	private final DoviProcessBuilder pbservice;

	private final boolean acceptBLRPUInput;

	public MediaInfoProcessor(DoviProcessBuilder pbservice, String MEDIAINFO, boolean acceptBLRPUInput) {
		this.MEDIAINFO = MEDIAINFO;
		this.pbservice = pbservice;
		this.acceptBLRPUInput = acceptBLRPUInput;
	}

	@Override
	public VideoFileDTO process(VideoFileDTO videoFile) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		logger.info("Fetching media info from {}...", JobUtils.getWithoutPath(videoFile.getName()));
		String cmd = MEDIAINFO + " --output=JSON \"" + videoFile.getName() + "\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		MediaInfoDTO mediaInfo;
		Process process = pb.start();
		String output = JobUtils.returnOutput(process);
		mediaInfo = objectMapper.readValue(output, MediaInfoDTO.class);
		if (!acceptBLRPUInput && JobUtils.isBLRPU(mediaInfo)) {
			logger.warn(ConsoleColor.YELLOW.value
					+ "Dolby Vision and HDR were already detected on {}, this file will be ignored."
					+ ConsoleColor.NONE.value, videoFile.getName());
			return null;
		}
		if (JobUtils.getFrameCount(mediaInfo) < 2000) {
			logger.warn(ConsoleColor.YELLOW.value
					+ "Not enough video frames detected for {}, a minimum of 2,000 is required in order to properly validate video borders. This file will be ignored."
					+ ConsoleColor.NONE.value, videoFile.getName());
			return null;
		}
		if (!JobUtils.isHDR(mediaInfo)) {

			logger.warn(ConsoleColor.YELLOW.value + "No HDR format detected for {}. This file will be ignored."
					+ ConsoleColor.NONE.value, videoFile.getName());
			return null;
		}
		videoFile.setMediaInfo(mediaInfo);
		logger.info(ConsoleColor.GREEN.value + "Added file!" + ConsoleColor.NONE.value);
		return videoFile;
	}

}
