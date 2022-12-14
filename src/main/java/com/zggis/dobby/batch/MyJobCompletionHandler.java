package com.zggis.dobby.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zggis.dobby.services.MediaService;

@Component
public class MyJobCompletionHandler extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(MyJobCompletionHandler.class);

    @Autowired
    private MediaService mediaService;

    @Value("${logging.file.name}")
    private String logFileLocation;

    @Autowired
    public MyJobCompletionHandler() {
        super();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info(ConsoleColor.GREEN.value + "Job Completed Successfully!" + ConsoleColor.NONE.value);
            log.info(ConsoleColor.MAGENTA.value + "Looking for a way to be notified when this job completes? Check out my other app Howler (https://github.com/Zggis/howler), it works great with Dobby." + ConsoleColor.NONE.value);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.info(ConsoleColor.RED.value + "Job Failed!" + ConsoleColor.NONE.value);
        }
        log.info("Logs for this job can be found at {}", logFileLocation);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        mediaService.createTempDirectory();
    }

}
