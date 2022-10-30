package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.dto.VideoMergeDTO;

public class CacheMergeWriter implements ItemWriter<VideoMergeDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheMergeWriter.class);

	private List<VideoMergeDTO> mergers = new ArrayList<>();

	@Override
	public void write(List<? extends VideoMergeDTO> items) throws Exception {
		for (VideoMergeDTO conversion : items) {
			mergers.add(conversion);
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (VideoMergeDTO merge : mergers) {
			if (merge.getBlRPUFile() == null || merge.getStandardFile() == null) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put("BLRPUMerge", mergers);
		return ExitStatus.COMPLETED;
	}

}
