package com.zggis.dobby.batch.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobCacheKey;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.BLRPUHevcFileDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.batch.VideoMergeDTO;

public class CacheMergeReader implements ItemReader<VideoMergeDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheMergeReader.class);

	private static final String DOLBY_VISION = "Dolby Vision";

	private Stack<VideoMergeDTO> availableMerges = new Stack<>();

	@Override
	public VideoMergeDTO read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!availableMerges.isEmpty()) {
			return availableMerges.pop();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		List<BLRPUHevcFileDTO> blrpus = (List<BLRPUHevcFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get(JobCacheKey.BLRPUHEVC.value);
		List<VideoFileDTO> stdFiles = (List<VideoFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get(JobCacheKey.MEDIAFILE.value);
		Map<String, BLRPUHevcFileDTO> blrpuMap = new HashMap<>();
		for (BLRPUHevcFileDTO rpu : blrpus) {
			blrpuMap.put(rpu.getKey(), rpu);
		}
		for (VideoFileDTO stdFile : stdFiles) {
			if (!DOLBY_VISION.equals(JobUtils.getHDRFormat(stdFile.getMediaInfo()))) {
				BLRPUHevcFileDTO blrpuFileName = blrpuMap.get(stdFile.getKey());
				if (blrpuFileName != null) {
					blrpuMap.remove(stdFile.getKey());
					VideoMergeDTO newInjectionDTO = new VideoMergeDTO(stdFile, blrpuFileName);
					availableMerges.push(newInjectionDTO);
				} else {
					logger.warn(ConsoleColor.YELLOW.value
							+ "Unable to find a BLRPU episode match for {}, check the logs above. This may be caused by a missing file, or a file that failed validation and was skipped as a result."
							+ ConsoleColor.YELLOW.value, stdFile.getName());
				}
			}
		}
		for (String key : blrpuMap.keySet()) {
			BLRPUHevcFileDTO blrpuHevcFile = blrpuMap.get(key);
			logger.warn(ConsoleColor.YELLOW.value
					+ "Unable to find a HDR episode match for {}, check the logs above. This may be caused by a missing file, or a file that failed validation and was skipped as a result."
					+ ConsoleColor.YELLOW.value, blrpuHevcFile.getName());
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
