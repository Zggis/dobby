package com.zggis.dobby.dto.mediainfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatingLibraryDTO {
	public String name;
	public String version;
	public String url;
}
