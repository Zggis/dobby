package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.services.DoviProcessBuilder;

public class ExtractRpuProcessor implements ItemProcessor<String, String> {

	private static final Logger logger = LoggerFactory.getLogger(ExtractRpuProcessor.class);

	private DoviProcessBuilder pbservice;

	private String outputDir;

	private String DOVI_TOOL;

	private boolean execute;

	public ExtractRpuProcessor(DoviProcessBuilder pbservice, String outputDir, String DOVI_TOOL, boolean execute) {
		this.pbservice = pbservice;
		this.DOVI_TOOL = DOVI_TOOL;
		this.outputDir = outputDir;
		this.execute = execute;
	}

	@Override
	public String process(String dolbyVisionHevcFileName) throws IOException {
		logger.info("Generating RPU file from {}...", dolbyVisionHevcFileName);
		String cmd = DOVI_TOOL + " -m 2 extract-rpu \"" + dolbyVisionHevcFileName + "\"" + " -o \"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(dolbyVisionHevcFileName) + "-RPU.bin\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return outputDir + JobUtils.getWithoutPathAndExtension(dolbyVisionHevcFileName) + "-RPU.bin";
	}

}
