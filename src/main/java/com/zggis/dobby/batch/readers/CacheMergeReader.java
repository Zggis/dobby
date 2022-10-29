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

import com.zggis.dobby.batch.HevcFileDTO;
import com.zggis.dobby.batch.VideoFileDTO;
import com.zggis.dobby.batch.VideoMergeDTO;

public class CacheMergeReader implements ItemReader<VideoMergeDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheMergeReader.class);

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
		List<HevcFileDTO> blrpus = (List<HevcFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get("BLRPUHevc");
		Map<String, HevcFileDTO> blrpuMap = new HashMap<>();
		for (HevcFileDTO rpu : blrpus) {
			blrpuMap.put(rpu.getKey(), rpu);
		}
		List<VideoFileDTO> stdFiles = (List<VideoFileDTO>) stepExecution.getJobExecution().getExecutionContext()
				.get("STDMKV");
		for (VideoFileDTO stdFile : stdFiles) {
			HevcFileDTO blrpuFileName = blrpuMap.get(stdFile.getKey());
			if (blrpuFileName != null) {

				VideoMergeDTO newInjectionDTO = new VideoMergeDTO(stdFile, blrpuFileName);
				availableMerges.push(newInjectionDTO);
			} else {
				logger.warn("Unable to find a BLRPU episode match for {}", stdFile.getName());
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
