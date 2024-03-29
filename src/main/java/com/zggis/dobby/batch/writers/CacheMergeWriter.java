package com.zggis.dobby.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.zggis.dobby.batch.ConsoleColor;
import com.zggis.dobby.batch.JobCacheKey;
import com.zggis.dobby.dto.batch.VideoMergeDTO;

public class CacheMergeWriter implements ItemWriter<VideoMergeDTO>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CacheMergeWriter.class);

    private final List<VideoMergeDTO> mergers = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (VideoMergeDTO merge : mergers) {
            if (merge.getStandardFile() != null) {
                logger.debug("Writing merger {} to ...", merge.getStandardFile().getKey());
            }
            if (!merge.isValid()) {
                logger.error(ConsoleColor.RED.value + "Merge failed, job cannot proceed"
                        + ConsoleColor.NONE.value);
                return ExitStatus.FAILED;
            }
        }
        stepExecution.getJobExecution().getExecutionContext().put(JobCacheKey.MERGE.value, mergers);
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(Chunk<? extends VideoMergeDTO> chunk) {
        mergers.addAll(chunk.getItems());
    }
}
