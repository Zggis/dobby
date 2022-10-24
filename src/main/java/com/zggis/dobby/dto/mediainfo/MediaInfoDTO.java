package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaInfoDTO {
	public CreatingLibraryDTO creatingLibrary;
	public MediaDTO media;
}
