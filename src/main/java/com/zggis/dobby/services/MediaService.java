package com.zggis.dobby.services;

public interface MediaService {

	public String getMediaDirectory();

	public String getTempDirectory();

	public String getResultsDirectory();

	public void createResultsDirectory();

	public void createTempDirectory();

	public void deleteTempDirectory();

	public boolean isCleanup();

}
