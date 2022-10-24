package com.zggis.dobby.dto.mediainfo;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaDTO {
	@JsonProperty("@ref")
	public String ref;
	public ArrayList<TrackDTO> track;
}
