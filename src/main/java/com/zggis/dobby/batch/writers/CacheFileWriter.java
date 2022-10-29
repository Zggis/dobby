package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.IFile;
import com.zggis.dobby.batch.JobUtils;

public class CacheFileWriter<T extends IFile> implements ItemWriter<T>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheFileWriter.class);

	private List<T> files = new ArrayList<>();

	private String fileType;

	public CacheFileWriter(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		for (T conversion : items) {
			files.add(conversion);
			logger.info("Writing {} : {}", fileType, conversion.getName());
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (T fileName : files) {
			if (!JobUtils.doesMediaFileExists(fileName.getName())) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put(fileType, files);
		return ExitStatus.COMPLETED;
	}

}
