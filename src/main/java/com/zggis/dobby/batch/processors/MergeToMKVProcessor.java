package com.zggis.dobby.batch.processors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.batch.VideoMergeDTO;
import com.zggis.dobby.services.DoviProcessBuilder;

public class MergeToMKVProcessor implements ItemProcessor<VideoMergeDTO, String> {

	private static final Logger logger = LoggerFactory.getLogger(MergeToMKVProcessor.class);

	private static final String DEFAULT_RESOLUTION = "24.000";

	private String MKVMERGE;

	private String outputDir;

	private DoviProcessBuilder pbservice;

	private boolean execute;

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

	public MergeToMKVProcessor(DoviProcessBuilder pbservice, String outputDir, String MKVMERGE, boolean execute) {
		this.MKVMERGE = MKVMERGE;
		this.outputDir = outputDir;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public String process(VideoMergeDTO merge) throws IOException {
		String duration = null;
		duration = fpsMap.get(DEFAULT_RESOLUTION);
		logger.info("Generating MKV file from {}...", merge.getBlRPUFileName());
		String cmd = MKVMERGE + " --output \"" + outputDir
				+ JobUtils.getWithoutPathAndExtension(merge.getStandardFileName()) + "[BL+RPU].mkv\"" + " --no-video \""
				+ merge.getStandardFileName() + "\" --language 0:und --track-order 1:0 --compression 0:none " + duration
				+ " \"" + merge.getBlRPUFileName() + "\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return outputDir + JobUtils.getWithoutPathAndExtension(merge.getStandardFileName()) + "[BL+RPU].mkv";
	}

}
