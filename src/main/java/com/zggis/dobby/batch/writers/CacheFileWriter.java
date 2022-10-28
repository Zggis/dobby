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
import com.zggis.dobby.batch.FileDTO;

public class CacheFileWriter implements ItemWriter<FileDTO>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheFileWriter.class);

	private List<FileDTO> files = new ArrayList<>();

	private String fileType;

	public CacheFileWriter(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public void write(List<? extends FileDTO> items) throws Exception {
		for (FileDTO conversion : items) {
			files.add(conversion);
			logger.info("Writing {} : {}", fileType, conversion.getName());
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (FileDTO fileName : files) {
			if (!JobUtils.doesMediaFileExists(fileName.getName())) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put(fileType, files);
		return ExitStatus.COMPLETED;
	}

}
