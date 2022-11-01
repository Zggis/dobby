package com.zggis.dobby.batch;

import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.zggis.dobby.batch.processors.ExtractRpuProcessor;
import com.zggis.dobby.batch.processors.MKVActiveAreaProcessor;
import com.zggis.dobby.batch.processors.MKVToHevcProcessor;
import com.zggis.dobby.batch.processors.MP4ToHevcProcessor;
import com.zggis.dobby.batch.processors.MediaInfoProcessor;
import com.zggis.dobby.batch.processors.MergeToMKVProcessor;
import com.zggis.dobby.batch.processors.MergeValidationProcessor;
import com.zggis.dobby.batch.processors.RPUBorderInfoProcessor;
import com.zggis.dobby.batch.processors.RPUInjectProcessor;
import com.zggis.dobby.batch.readers.CacheInjectorReader;
import com.zggis.dobby.batch.readers.CacheMergeReader;
import com.zggis.dobby.batch.readers.CacheReader;
import com.zggis.dobby.batch.readers.DiskTVShowReader;
import com.zggis.dobby.batch.writers.CacheConversionWriter;
import com.zggis.dobby.batch.writers.CacheFileWriter;
import com.zggis.dobby.batch.writers.CacheMergeWriter;
import com.zggis.dobby.batch.writers.CacheTVShowWriter;
import com.zggis.dobby.dto.batch.BLRPUHevcFileDTO;
import com.zggis.dobby.dto.batch.HevcFileDTO;
import com.zggis.dobby.dto.batch.RPUFileDTO;
import com.zggis.dobby.dto.batch.TVShowConversionDTO;
import com.zggis.dobby.dto.batch.VideoFileDTO;
import com.zggis.dobby.dto.batch.VideoInjectionDTO;
import com.zggis.dobby.dto.batch.VideoMergeDTO;
import com.zggis.dobby.services.DoviProcessBuilder;
import com.zggis.dobby.services.MediaServiceImpl;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private static final int CHUNK = 1;

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
	private MediaServiceImpl mediaService;

	@Autowired
	private DoviProcessBuilder pbservice;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job mergeVideoFilesJob(MyJobCompletionHandler listener) {
		return jobBuilderFactory.get("Merge TV Shows").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(fetchMedia()).next(scanActiveArea()).next(convertMediaToHEVC()).next(extractRPU())
				.next(getBorderInfo()).next(injectRPU()).next(validateMerge()).next(mergeResult()).end().build();
	}

	// Step 0 - Fetch Media Info
	@Bean
	public Step fetchMedia() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "0/7 Scan Media" + ConsoleColor.NONE.value)
				.<TVShowConversionDTO, TVShowConversionDTO>chunk(CHUNK).reader(tvShowReader())
				.processor(mediaInfoProcessor()).writer(tvShowStagedWriter()).build();
	}

	@Bean
	public ItemReader<TVShowConversionDTO> tvShowReader() {
		return new DiskTVShowReader(mediaService.getMediaDirectory());
	}

	@Bean
	public MediaInfoProcessor mediaInfoProcessor() {
		return new MediaInfoProcessor(pbservice, mediaService.getTempDirectory(), MEDIAINFO, true);
	}

	@Bean
	public ItemWriter<TVShowConversionDTO> tvShowStagedWriter() {
		return new CacheTVShowWriter();
	}

	// step 1 - Populate active area
	@Bean
	public Step scanActiveArea() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "1/7 Analyze HDR File" + ConsoleColor.NONE.value)
				.<VideoFileDTO, VideoFileDTO>chunk(CHUNK).reader(stdMkvReader()).processor(mkvActiveAreaProcessor())
				.writer(stdMkvWriter()).build();
	}

	@Bean
	public ItemReader<VideoFileDTO> stdMkvReader() {
		return new CacheReader<VideoFileDTO>(JobCacheKey.STDMKV);
	}

	@Bean
	public MKVActiveAreaProcessor mkvActiveAreaProcessor() {
		if (StringUtils.hasText(hwaccel)) {
			return new MKVActiveAreaProcessor(pbservice, FFMPEG + " " + hwaccel.trim(), true);
		} else {
			return new MKVActiveAreaProcessor(pbservice, FFMPEG, true);
		}
	}

	@Bean
	public ItemWriter<VideoFileDTO> stdMkvWriter() {
		return new CacheFileWriter<VideoFileDTO>(JobCacheKey.STDMKV);
	}

	// Step 2 - Convert Mp4 and MKV to HEVC
	@Bean
	public Step convertMediaToHEVC() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "2/7 Convert media to HEVC" + ConsoleColor.NONE.value)
				.<TVShowConversionDTO, TVShowConversionDTO>chunk(CHUNK).reader(tvShowCacheReader())
				.processor(compositeItemProcessor()).writer(tvShowConvertedWriter()).build();
	}

	@Bean
	public ItemProcessor<TVShowConversionDTO, TVShowConversionDTO> compositeItemProcessor() {
		CompositeItemProcessor<TVShowConversionDTO, TVShowConversionDTO> compositeItemProcessor = new CompositeItemProcessor<>();
		compositeItemProcessor.setDelegates(Arrays.asList(mp4ToHevcProcessor(), mkvToHevcProcessor()));
		return compositeItemProcessor;
	}

	@Bean
	public ItemReader<TVShowConversionDTO> tvShowCacheReader() {
		return new CacheReader<TVShowConversionDTO>(JobCacheKey.CONVERSION);
	}

	@Bean
	public MP4ToHevcProcessor mp4ToHevcProcessor() {
		return new MP4ToHevcProcessor(pbservice, mediaService.getTempDirectory(), MP4EXTRACT, true);
	}

	@Bean
	public MKVToHevcProcessor mkvToHevcProcessor() {
		return new MKVToHevcProcessor(pbservice, mediaService.getTempDirectory(), MKVEXTRACT, true);
	}

	@Bean
	public ItemWriter<TVShowConversionDTO> tvShowConvertedWriter() {
		return new CacheConversionWriter();
	}

	// Step 3 - Extract RPU from DV HEVC

	@Bean
	public Step extractRPU() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "3/7 Extract RPU" + ConsoleColor.NONE.value)
				.<HevcFileDTO, RPUFileDTO>chunk(CHUNK).reader(dvHevcReader()).processor(extractRpuProcessor())
				.writer(dvHevcWriter()).build();
	}

	@Bean
	public ItemReader<HevcFileDTO> dvHevcReader() {
		return new CacheReader<HevcFileDTO>(JobCacheKey.DVHEVC);
	}

	@Bean
	public ExtractRpuProcessor extractRpuProcessor() {
		return new ExtractRpuProcessor(pbservice, mediaService.getTempDirectory(), DOVI_TOOL, true);
	}

	@Bean
	public ItemWriter<RPUFileDTO> dvHevcWriter() {
		return new CacheFileWriter<RPUFileDTO>(JobCacheKey.RPU);
	}

	// Step 4 - Populate Border info
	@Bean
	public Step getBorderInfo() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "4/7 Analyze RPU" + ConsoleColor.NONE.value)
				.<RPUFileDTO, RPUFileDTO>chunk(CHUNK).reader(rpuReader()).processor(rpuBorderInfoProcessor())
				.writer(rpuWriter()).build();
	}

	@Bean
	public ItemReader<RPUFileDTO> rpuReader() {
		return new CacheReader<RPUFileDTO>(JobCacheKey.RPU);
	}

	@Bean
	public RPUBorderInfoProcessor rpuBorderInfoProcessor() {
		return new RPUBorderInfoProcessor(pbservice, DOVI_TOOL, true);
	}

	@Bean
	public ItemWriter<RPUFileDTO> rpuWriter() {
		return new CacheFileWriter<RPUFileDTO>(JobCacheKey.RPU);
	}

	// Step 5 - Inject RPU into standard HEVC

	@Bean
	public Step injectRPU() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "5/7 Inject RPU into HEVC" + ConsoleColor.NONE.value)
				.<VideoInjectionDTO, BLRPUHevcFileDTO>chunk(CHUNK).reader(cacheInjectorReader())
				.processor(rpuInjectProcessor()).writer(blRPUCacheFileWriter()).build();
	}

	@Bean
	public ItemReader<VideoInjectionDTO> cacheInjectorReader() {
		return new CacheInjectorReader();
	}

	@Bean
	public RPUInjectProcessor rpuInjectProcessor() {
		return new RPUInjectProcessor(pbservice, mediaService.getTempDirectory(), DOVI_TOOL, true);
	}

	@Bean
	public ItemWriter<BLRPUHevcFileDTO> blRPUCacheFileWriter() {
		return new CacheFileWriter<BLRPUHevcFileDTO>(JobCacheKey.BLRPUHEVC);
	}

	// Step 6 - Validate merge
	@Bean
	public Step validateMerge() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "6/7 Validate Merge" + ConsoleColor.NONE.value)
				.<VideoMergeDTO, VideoMergeDTO>chunk(CHUNK).reader(cacheMergeValidationReader())
				.processor(mergeValidationProcessor()).writer(cacheMergeValidationWriter()).build();
	}

	@Bean
	public ItemReader<VideoMergeDTO> cacheMergeValidationReader() {
		return new CacheMergeReader();
	}

	@Bean
	public MergeValidationProcessor mergeValidationProcessor() {
		return new MergeValidationProcessor(true);
	}

	@Bean
	public ItemWriter<VideoMergeDTO> cacheMergeValidationWriter() {
		return new CacheMergeWriter();
	}

	// Step 7 - Convert BL-RPU to MKV

	@Bean
	public Step mergeResult() {
		return stepBuilderFactory.get(ConsoleColor.CYAN.value + "7/7 Generate Result" + ConsoleColor.NONE.value)
				.<VideoMergeDTO, VideoFileDTO>chunk(CHUNK).reader(cacheMergeReader()).processor(mergeToMkvProcessor())
				.writer(blRPUMKVCacheFileWriter()).build();
	}

	@Bean
	public ItemReader<VideoMergeDTO> cacheMergeReader() {
		return new CacheReader<VideoMergeDTO>(JobCacheKey.MERGE);
	}

	@Bean
	public MergeToMKVProcessor mergeToMkvProcessor() {
		return new MergeToMKVProcessor(pbservice, mediaService.getResultsDirectory(), MKVMERGE, true);
	}

	@Bean
	public ItemWriter<VideoFileDTO> blRPUMKVCacheFileWriter() {
		return new CacheFileWriter<VideoFileDTO>(JobCacheKey.BLRPUMKV);
	}

}
