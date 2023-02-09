package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaInfoDTO implements Serializable {
	public MediaDTO media;
}
