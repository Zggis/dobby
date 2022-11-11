package com.zggis.dobby.batch;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.zggis.dobby.batch.processors.CleanupProcessor;
import com.zggis.dobby.batch.processors.ConvertToHevcProcessor;
import com.zggis.dobby.batch.processors.ExtractRpuProcessor;
import com.zggis.dobby.batch.processors.MKVActiveAreaProcessor;
import com.zggis.dobby.batch.processors.MediaInfoProcessor;
import com.zggis.dobby.batch.processors.MergeToMKVProcessor;
import com.zggis.dobby.batch.processors.MergeValidationProcessor;
import com.zggis.dobby.batch.processors.RPUBorderInfoProcessor;
import com.zggis.dobby.batch.processors.RPUInjectProcessor;
import com.zggis.dobby.batch.processors.ResultValidatorProcessor;
import com.zggis.dobby.batch.readers.CacheCleanupReader;
import com.zggis.dobby.batch.readers.CacheInjectorReader;
import com.zggis.dobby.batch.readers.CacheMergeReader;
import com.zggis.dobby.batch.readers.CacheReader;
import com.zggis.dobby.batch.readers.DiskTVShowReader;
import com.zggis.dobby.batch.writers.CacheFileWriter;
import com.zggis.dobby.batch.writers.CacheMergeWriter;
import com.zggis.dobby.batch.writers.NoOperationWriter;
import com.zggis.dobby.dto.batch.BLRPUHevcFileDTO;
import com.zggis.dobby.dto.batch.FileDTO;
import com.zggis.dobby.dto.batch.HevcFileDTO;
import com.zggis.dobby.dto.batch.RPUFileDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.batch.VideoInjectionDTO;
import com.zggis.dobby.dto.batch.VideoMergeDTO;
import com.zggis.dobby.services.DoviProcessBuilder;
import com.zggis.dobby.services.MediaService;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final int CHUNK = 1;

    private static final boolean EXECUTE = true;

    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${dovi.tool.location}")
    private String DOVI_TOOL;

    @Value("${mkvextract.location}")
    private String MKVEXTRACT;

    @Value("${mkvmerge.location}")
    private String MKVMERGE;

    @Value("${mp4extract.location}")
    private String MP4EXTRACT;

    @Value("${mediainfo.location}")
    private String MEDIAINFO;

    @Value("${ffmpeg.location}")
    private String FFMPEG;

    @Value("${hardware.acceleration}")
    private String hwaccel;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private DoviProcessBuilder pbservice;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job mergeVideoFilesJob(MyJobCompletionHandler listener) {
        return jobBuilderFactory.get(ConsoleColor.CYAN.value + "Merge TV Shows" + ConsoleColor.NONE.value)
                .incrementer(new RunIdIncrementer()).listener(listener).flow(fetchMedia()).next(scanActiveArea())
                .next(convertMediaToHEVC()).next(extractRPU()).next(getBorderInfo()).next(injectRPU())
                .next(validateMerge()).next(mergeResult()).next(validateResult()).next(cleanupResult()).end().build();
    }

    // Step 0 - Fetch Media Info
    @Bean
    public Step fetchMedia() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "0/9 Scan Media" + ConsoleColor.NONE.value)
                .<VideoFileDTO, VideoFileDTO>chunk(CHUNK).reader(tvShowReader()).processor(mediaInfoProcessor())
                .writer(tvShowStagedWriter()).build();
    }

    @Bean
    public ItemReader<VideoFileDTO> tvShowReader() {
        return new DiskTVShowReader(mediaService.getMediaDirectory());
    }

    @Bean
    public MediaInfoProcessor mediaInfoProcessor() {
        return new MediaInfoProcessor(pbservice, MEDIAINFO);
    }

    @Bean
    public ItemWriter<VideoFileDTO> tvShowStagedWriter() {
        return new CacheFileWriter<>(JobCacheKey.MEDIAFILE, EXECUTE);
    }

    // step 1 - Populate active area
    @Bean
    public Step scanActiveArea() {
        return stepBuilderFactory
                .get(ConsoleColor.CYAN.value + "1/9 Analyze Active Area of HDR Files" + ConsoleColor.NONE.value)
                .<VideoFileDTO, VideoFileDTO>chunk(CHUNK).reader(stdMkvReader()).processor(mkvActiveAreaProcessor())
                .writer(conversionWriter2()).build();
    }

    @Bean
    public ItemReader<VideoFileDTO> stdMkvReader() {
        return new CacheReader<>(JobCacheKey.MEDIAFILE);
    }

    @Bean
    public MKVActiveAreaProcessor mkvActiveAreaProcessor() {
        if (StringUtils.hasText(hwaccel)) {
            return new MKVActiveAreaProcessor(pbservice, FFMPEG + " " + hwaccel.trim(), EXECUTE);
        } else {
            return new MKVActiveAreaProcessor(pbservice, FFMPEG, EXECUTE);
        }
    }

    @Bean
    public ItemWriter<VideoFileDTO> conversionWriter2() {
        return new CacheFileWriter<>(JobCacheKey.MEDIAFILE, EXECUTE);
    }

    // Step 2 - Convert Mp4 and MKV to HEVC
    @Bean
    public Step convertMediaToHEVC() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "2/9 Convert media to HEVC" + ConsoleColor.NONE.value)
                .<VideoFileDTO, HevcFileDTO>chunk(CHUNK).reader(tvShowCacheReader()).processor(mp4ToHevcProcessor())
                .writer(tvShowHevcWriter()).build();
    }

    @Bean
    public ItemReader<VideoFileDTO> tvShowCacheReader() {
        return new CacheReader<>(JobCacheKey.MEDIAFILE);
    }

    @Bean
    public ConvertToHevcProcessor mp4ToHevcProcessor() {
        return new ConvertToHevcProcessor(pbservice, mediaService.getTempDirectory(), MP4EXTRACT, MKVEXTRACT, EXECUTE);
    }

    @Bean
    public ItemWriter<HevcFileDTO> tvShowHevcWriter() {
        return new CacheFileWriter<>(JobCacheKey.HEVCFILE, EXECUTE);
    }

    // Step 3 - Extract RPU from DV HEVC

    @Bean
    public Step extractRPU() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "3/9 Extract RPUs" + ConsoleColor.NONE.value)
                .<HevcFileDTO, RPUFileDTO>chunk(CHUNK).reader(dvHevcReader()).processor(extractRpuProcessor())
                .writer(dvHevcWriter()).build();
    }

    @Bean
    public ItemReader<HevcFileDTO> dvHevcReader() {
        return new CacheReader<>(JobCacheKey.HEVCFILE);
    }

    @Bean
    public ExtractRpuProcessor extractRpuProcessor() {
        return new ExtractRpuProcessor(pbservice, mediaService.getTempDirectory(), DOVI_TOOL, EXECUTE);
    }

    @Bean
    public ItemWriter<RPUFileDTO> dvHevcWriter() {
        return new CacheFileWriter<>(JobCacheKey.RPU, EXECUTE);
    }

    // Step 4 - Populate Border info
    @Bean
    public Step getBorderInfo() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "4/9 Analyze RPUs" + ConsoleColor.NONE.value)
                .<RPUFileDTO, RPUFileDTO>chunk(CHUNK).reader(rpuReader()).processor(rpuBorderInfoProcessor())
                .writer(rpuWriter()).build();
    }

    @Bean
    public ItemReader<RPUFileDTO> rpuReader() {
        return new CacheReader<>(JobCacheKey.RPU);
    }

    @Bean
    public RPUBorderInfoProcessor rpuBorderInfoProcessor() {
        return new RPUBorderInfoProcessor(pbservice, DOVI_TOOL, EXECUTE);
    }

    @Bean
    public ItemWriter<RPUFileDTO> rpuWriter() {
        return new CacheFileWriter<>(JobCacheKey.RPU, EXECUTE);
    }

    // Step 5 - Inject RPU into standard HEVC

    @Bean
    public Step injectRPU() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "5/9 Inject RPUs into HEVCs" + ConsoleColor.NONE.value)
                .<VideoInjectionDTO, BLRPUHevcFileDTO>chunk(CHUNK).reader(cacheInjectorReader())
                .processor(rpuInjectProcessor()).writer(blRPUCacheFileWriter()).build();
    }

    @Bean
    public ItemReader<VideoInjectionDTO> cacheInjectorReader() {
        return new CacheInjectorReader();
    }

    @Bean
    public RPUInjectProcessor rpuInjectProcessor() {
        return new RPUInjectProcessor(pbservice, mediaService.getTempDirectory(), DOVI_TOOL, EXECUTE);
    }

    @Bean
    public ItemWriter<BLRPUHevcFileDTO> blRPUCacheFileWriter() {
        return new CacheFileWriter<>(JobCacheKey.BLRPUHEVC, EXECUTE);
    }

    // Step 6 - Validate merge
    @Bean
    public Step validateMerge() {
        return stepBuilderFactory
                .get(ConsoleColor.CYAN.value + "6/9 Validate Merge Compatibility" + ConsoleColor.NONE.value)
                .<VideoMergeDTO, VideoMergeDTO>chunk(CHUNK).reader(cacheMergeValidationReader())
                .processor(mergeValidationProcessor()).writer(cacheMergeValidationWriter()).build();
    }

    @Bean
    public ItemReader<VideoMergeDTO> cacheMergeValidationReader() {
        return new CacheMergeReader();
    }

    @Bean
    public MergeValidationProcessor mergeValidationProcessor() {
        return new MergeValidationProcessor(EXECUTE);
    }

    @Bean
    public ItemWriter<VideoMergeDTO> cacheMergeValidationWriter() {
        return new CacheMergeWriter();
    }

    // Step 7 - Convert BL-RPU to MKV

    @Bean
    public Step mergeResult() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "7/9 Generate Results" + ConsoleColor.NONE.value)
                .<VideoMergeDTO, VideoFileDTO>chunk(CHUNK).reader(cacheMergeReader()).processor(mergeToMkvProcessor())
                .writer(blRPUMKVCacheFileWriter()).build();
    }

    @Bean
    public ItemReader<VideoMergeDTO> cacheMergeReader() {
        return new CacheReader<>(JobCacheKey.MERGE);
    }

    @Bean
    public MergeToMKVProcessor mergeToMkvProcessor() {
        return new MergeToMKVProcessor(pbservice, mediaService, MKVMERGE, EXECUTE);
    }

    @Bean
    public ItemWriter<VideoFileDTO> blRPUMKVCacheFileWriter() {
        return new CacheFileWriter<>(JobCacheKey.BLRPUMKV, EXECUTE);
    }

    // Step 8 - Validate Result

    @Bean
    public Step validateResult() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "8/9 Validate Results" + ConsoleColor.NONE.value)
                .<VideoFileDTO, VideoFileDTO>chunk(CHUNK).reader(resultReader()).processor(resultValidatorProcessor())
                .writer(resultWriter()).build();
    }

    @Bean
    public ItemReader<VideoFileDTO> resultReader() {
        return new CacheReader<>(JobCacheKey.BLRPUMKV);
    }

    @Bean
    public ResultValidatorProcessor resultValidatorProcessor() {
        return new ResultValidatorProcessor(pbservice, MEDIAINFO, EXECUTE);
    }

    @Bean
    public ItemWriter<VideoFileDTO> resultWriter() {
        return new CacheFileWriter<>(JobCacheKey.BLRPUMKV, EXECUTE);
    }

    // Step 9 - Clean Up

    @Bean
    public Step cleanupResult() {
        return stepBuilderFactory.get(ConsoleColor.CYAN.value + "9/9 Cleanup Temporary File" + ConsoleColor.NONE.value)
                .<FileDTO, FileDTO>chunk(CHUNK).reader(hevcCleanupReader()).processor(cleanupProcessor())
                .writer(cleanupWriter()).build();
    }

    @Bean
    public ItemReader<FileDTO> hevcCleanupReader() {
        return new CacheCleanupReader(mediaService);
    }

    @Bean
    public CleanupProcessor cleanupProcessor() {
        return new CleanupProcessor(mediaService);
    }

    @Bean
    public ItemWriter<FileDTO> cleanupWriter() {
        return new NoOperationWriter<>();
    }

    @PreDestroy
    public void exit() {
        logger.info(ConsoleColor.YELLOW.value + "Application will exist in 30s" + ConsoleColor.NONE.value);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

}
