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
import com.zggis.dobby.batch.JobUtils;
import com.zggis.dobby.dto.batch.IFile;
import org.springframework.util.StringUtils;

public class CacheFileWriter<T extends IFile> implements ItemWriter<T>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CacheFileWriter.class);

    private final List<T> files = new ArrayList<>();

    private final JobCacheKey fileType;
    private final boolean validate;

    public CacheFileWriter(JobCacheKey fileType, boolean validate) {
        this.fileType = fileType;
        this.validate = validate;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        for (T file : files) {
            if (validate && !StringUtils.hasText(file.getName())) {
                return ExitStatus.FAILED;
            }
            if (validate && !JobUtils.doesMediaFileExists(file.getName())) {
                logger.error(ConsoleColor.RED.value + "Could not find processor result {}" + ConsoleColor.NONE.value,
                        file.getName());
                return ExitStatus.FAILED;
            }
        }
        stepExecution.getJobExecution().getExecutionContext().put(fileType.value, files);
        return ExitStatus.COMPLETED;
    }

    @Override
    public void write(Chunk<? extends T> chunk) {
        for (T file : chunk) {
            this.files.add(file);
            logger.debug("Writing {} : {}", fileType.value, file.getName());
        }
    }
}
