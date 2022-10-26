package com.zggis.dobby.dto.mediainfo;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaDTO {
	public ArrayList<TrackDTO> track;
}
