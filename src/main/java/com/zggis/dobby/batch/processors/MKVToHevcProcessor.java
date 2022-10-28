package com.zggis.dobby.batch.processors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MKVToHevcProcessor implements ItemProcessor<String, String> {

	private static final Logger logger = LoggerFactory.getLogger(MKVToHevcProcessor.class);

	private String MKVEXTRACT;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private String mediaDir;

	private boolean execute;

	public MKVToHevcProcessor(DoviProcessBuilder pbservice, String mediaDir, String outputDir, String MKVEXTRACT,
			boolean execute) {
		this.MKVEXTRACT = MKVEXTRACT;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.mediaDir = mediaDir;
		this.execute = execute;
	}

	@Override
	public String process(String standardFileName) throws IOException {
		logger.info("Generating HEVC file from {}...", standardFileName);
		String cmd = MKVEXTRACT + " \"" + mediaDir + standardFileName + "\" tracks 0:\"" + outputDir
				+ JobUtils.getWithoutExtension(standardFileName) + ".hevc\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return outputDir + JobUtils.getWithoutExtension(standardFileName) + ".hevc";
	}

}
