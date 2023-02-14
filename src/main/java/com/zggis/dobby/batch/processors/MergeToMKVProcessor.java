package com.zggis.dobby.batch.processors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.batch.VideoMergeDTO;
import com.zggis.dobby.services.DoviProcessBuilder;
import com.zggis.dobby.services.MediaService;

public class MergeToMKVProcessor implements ItemProcessor<VideoMergeDTO, VideoFileDTO> {

	private static final Logger logger = LoggerFactory.getLogger(MergeToMKVProcessor.class);

	private static final String DEFAULT_RESOLUTION = "24.000";

	private final String MKVMERGE;

	private final MediaService mediaService;

	private final DoviProcessBuilder pbservice;

	private final boolean execute;

	private static final Map<String, String> fpsMap;

	static {
		fpsMap = new HashMap<>();
		fpsMap.put("23.976", "--default-duration 0:23.97602397602p --fix-bitstream-timing-information 0:1");
		fpsMap.put(DEFAULT_RESOLUTION, "--default-duration 0:24p --fix-bitstream-timing-information 0:1");
		fpsMap.put("25.000", "--default-duration 0:25p --fix-bitstream-timing-information 0:1");
		fpsMap.put("30.000", "--default-duration 0:30p --fix-bitstream-timing-information 0:1");
		fpsMap.put("48.000", "--default-duration 0:48p --fix-bitstream-timing-information 0:1");
		fpsMap.put("50.000", "--default-duration 0:50p --fix-bitstream-timing-information 0:1");
		fpsMap.put("60.000", "--default-duration 0:60p --fix-bitstream-timing-information 0:1");
	}

	public MergeToMKVProcessor(DoviProcessBuilder pbservice, MediaService mediaService, String MKVMERGE,
			boolean execute) {
		this.MKVMERGE = MKVMERGE;
		this.mediaService = mediaService;
		this.pbservice = pbservice;
		this.execute = execute;
	}

	@Override
	public VideoFileDTO process(VideoMergeDTO merge) throws IOException {
		String frameRate = JobUtils.getFrameRate(merge.getStandardFile().getMediaInfo());
		String duration;
		if (fpsMap.containsKey(frameRate)) {
			duration = fpsMap.get(frameRate);
		} else {
			duration = fpsMap.get(DEFAULT_RESOLUTION);
		}
		logger.info("Generating MKV file from {}...", JobUtils.getWithoutPath(merge.getBlRPUFile().getName()));
		String cmd = MKVMERGE + " --output \"" + mediaService.getResultsDirectory()
				+ JobUtils.getWithoutPathAndExtension(merge.getStandardFile().getName()) + "[BL+RPU].mkv\""
				+ " --no-video \"" + merge.getStandardFile().getName()
				+ "\" --language 0:und --track-order 1:0 --compression 0:none " + duration + " \""
				+ merge.getBlRPUFile().getName() + "\"";
		logger.debug(cmd);
		ProcessBuilder pb = pbservice.get(cmd);
		pb.redirectErrorStream(true);
		if (execute) {
			Process p = pb.start();
			JobUtils.printOutput(p);
		} else {
			logger.info("===EXECUTION SKIPPED===");
		}
		return new VideoFileDTO(
				mediaService.getResultsDirectory()
						+ JobUtils.getWithoutPathAndExtension(merge.getStandardFile().getName()) + "[BL+RPU].mkv",
				merge.getStandardFile().getKey());
	}

}
