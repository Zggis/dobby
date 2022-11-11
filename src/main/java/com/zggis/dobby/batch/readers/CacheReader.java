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

import com.zggis.dobby.batch.JobCacheKey;

public class CacheReader<T> implements ItemReader<T>, StepExecutionListener {

    private final JobCacheKey fileType;

    private final Stack<T> items = new Stack<>();

    public CacheReader(JobCacheKey cacheKey) {
        this.fileType = cacheKey;
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!items.isEmpty()) {
            return items.pop();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeStep(StepExecution stepExecution) {
        items.addAll((Collection<T>) stepExecution.getJobExecution().getExecutionContext().get(fileType.value));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

}
