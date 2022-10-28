package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zggis.dobby.batch.HevcVideoConversion;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.batch.VideoFileDTO;
import com.zggis.dobby.dto.mediainfo.MediaInfoDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MediaInfoProcessor implements ItemProcessor<HevcVideoConversion, HevcVideoConversion> {

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
	public HevcVideoConversion process(HevcVideoConversion conversion) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		logger.info("Fetching media info from {}...", conversion.getStandardFile().getName());
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
		logger.info("Fetching media info from {}...", conversion.getDolbyVisionFile().getName());
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
		return new HevcVideoConversion(conversion.getKey(), stdFileDTO, dvFileDTO);
	}

}
