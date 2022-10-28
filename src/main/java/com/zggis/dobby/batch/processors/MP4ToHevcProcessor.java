package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.HevcFileDTO;
import com.zggis.dobby.batch.HevcVideoConversion;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MP4ToHevcProcessor implements ItemProcessor<HevcVideoConversion, HevcVideoConversion> {

	private static final Logger logger = LoggerFactory.getLogger(MP4ToHevcProcessor.class);

	private String MP4EXTRACT;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private boolean execute;

	public MP4ToHevcProcessor(DoviProcessBuilder pbservice, String outputDir, String MP4EXTRACT, boolean execute) {
		this.MP4EXTRACT = MP4EXTRACT;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public HevcVideoConversion process(HevcVideoConversion conversion) throws IOException {
		logger.info("Generating HEVC file from {}...", conversion.getDolbyVisionFile().getName());
		String CMD = MP4EXTRACT + " -raw 1 -out \"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(conversion.getDolbyVisionFile().getName()) + ".hevc\" \""
				+ conversion.getDolbyVisionFile().getName() + "\"";
		logger.debug(CMD);
		ProcessBuilder pb = pbservice.get(CMD);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		conversion.getResults()
				.add(new HevcFileDTO(outputDir
						+ JobUtils.getWithoutPathAndExtension(conversion.getDolbyVisionFile().getName()) + ".hevc",
						conversion.getKey(), true));
		return conversion;

	}

}
