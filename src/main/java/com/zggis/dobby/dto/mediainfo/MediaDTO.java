package com.zggis.dobby.dto.mediainfo;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaDTO implements Serializable {
	public ArrayList<TrackDTO> track;
}
