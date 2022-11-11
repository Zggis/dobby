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
import com.zggis.dobby.dto.batch.FileDTO;
import com.zggis.dobby.services.MediaService;

public class CacheCleanupReader implements ItemReader<FileDTO>, StepExecutionListener {

    private final Stack<FileDTO> items = new Stack<>();

    private final MediaService mediaService;

    public CacheCleanupReader(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public FileDTO read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!items.isEmpty()) {
            return items.pop();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeStep(StepExecution stepExecution) {
        items.addAll((Collection<FileDTO>) stepExecution.getJobExecution().getExecutionContext()
                .get(JobCacheKey.HEVCFILE.value));
        items.addAll((Collection<FileDTO>) stepExecution.getJobExecution().getExecutionContext()
                .get(JobCacheKey.BLRPUHEVC.value));
        items.addAll(
                (Collection<FileDTO>) stepExecution.getJobExecution().getExecutionContext().get(JobCacheKey.RPU.value));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        mediaService.deleteTempDirectory();
        return ExitStatus.COMPLETED;
    }

}
