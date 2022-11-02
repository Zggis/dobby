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

public class ResultValidatorProcessor implements ItemProcessor<VideoFileDTO, VideoFileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(ResultValidatorProcessor.class);

	private String MEDIAINFO;

	private DoviProcessBuilder pbservice;

	public ResultValidatorProcessor(DoviProcessBuilder pbservice, String MEDIAINFO) {
		this.MEDIAINFO = MEDIAINFO;
		this.pbservice = pbservice;
	}

	@Override
	public VideoFileDTO process(VideoFileDTO videoFile) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		logger.info("Fetching media info from {}...", JobUtils.getWithoutPath(videoFile.getName()));
		String cmd = MEDIAINFO + " --output=JSON \"" + videoFile.getName() + "\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		MediaInfoDTO mediaInfo = null;
		Process process = pb.start();
		String output = JobUtils.returnOutput(process);
		mediaInfo = objectMapper.readValue(output, MediaInfoDTO.class);
		if (!JobUtils.isBLRPU(mediaInfo)) {
			logger.warn(ConsoleColor.RED.value + "Dolby Vision and HDR were not detected on {}, something went wrong."
					+ ConsoleColor.NONE.value, videoFile.getName());
			return null;
		}
		if (JobUtils.getFrameCount(mediaInfo) < 2000) {
			logger.warn(ConsoleColor.RED.value + "Not enough video frames detected for {}, something went wrong."
					+ ConsoleColor.NONE.value, videoFile.getName());
			return null;
		}
		logger.info(ConsoleColor.GREEN.value + "Dolby Vision and HDR were detected on {}, result looks good!"
				+ ConsoleColor.NONE.value, JobUtils.getWithoutPath(videoFile.getName()));
		videoFile.setMediaInfo(mediaInfo);
		return videoFile;
	}

}
