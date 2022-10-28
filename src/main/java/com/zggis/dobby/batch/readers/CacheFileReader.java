package com.zggis.dobby.batch.readers;

import java.util.Collection;
import java.util.Stack;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.zggis.dobby.batch.FileDTO;

public class CacheFileReader implements ItemReader<FileDTO>, StepExecutionListener {

	private String fileType;

	private Stack<FileDTO> tempFileNames = new Stack<>();

	public CacheFileReader(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public FileDTO read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!tempFileNames.isEmpty()) {
			return tempFileNames.pop();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		tempFileNames.addAll((Collection<FileDTO>) stepExecution.getJobExecution().getExecutionContext().get(fileType));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

}
