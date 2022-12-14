package com.zggis.dobby.batch.processors;

import java.io.File;
import java.io.IOException;

import org.springframework.batch.item.ItemProcessor;

import com.zggis.dobby.dto.batch.FileDTO;
import com.zggis.dobby.services.MediaService;

public class CleanupProcessor implements ItemProcessor<FileDTO, FileDTO> {

	private MediaService mediaService;

	public CleanupProcessor(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	@Override
	public FileDTO process(FileDTO videoFile) throws IOException {
		if (mediaService.isCleanup()) {
			File file = new File(videoFile.getName());
			file.delete();
		}
		return videoFile;
	}

}
