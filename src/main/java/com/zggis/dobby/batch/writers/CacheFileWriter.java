package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.JobCacheKey;
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.IFile;

public class CacheFileWriter<T extends IFile> implements ItemWriter<T>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(CacheFileWriter.class);

	private List<T> files = new ArrayList<>();

	private JobCacheKey fileType;

	public CacheFileWriter(JobCacheKey fileType) {
		this.fileType = fileType;
	}

	@Override
	public void write(List<? extends T> files) throws Exception {
		for (T file : files) {
			this.files.add(file);
			logger.debug("Writing {} : {}", fileType.value, file.getName());
		}

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		for (T file : files) {
			if (!JobUtils.doesMediaFileExists(file.getName())) {
				return ExitStatus.FAILED;
			}
		}
		stepExecution.getJobExecution().getExecutionContext().put(fileType.value, files);
		return ExitStatus.COMPLETED;
	}

}
