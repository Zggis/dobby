package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.JobUtils;

public class CacheFileWriter implements ItemWriter<String>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheFileWriter.class);

	private List<String> dolbyVisionFileNames = new ArrayList<>();

	private String fileType;

	public CacheFileWriter(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public void write(List<? extends String> items) throws Exception {
		for (String dolbyVisionHevcFileName : items) {
			dolbyVisionFileNames.add(dolbyVisionHevcFileName);
			logger.info("Writing {} : {}", fileType, dolbyVisionHevcFileName);
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (String fileName : dolbyVisionFileNames) {
			if (!JobUtils.doesMediaFileExists(fileName)) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put(fileType, dolbyVisionFileNames);
		return ExitStatus.COMPLETED;
	}

}
