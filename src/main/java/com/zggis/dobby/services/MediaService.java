package com.zggis.dobby.services;

public interface MediaService {

    String getMediaDirectory();

    String getTempDirectory();

    String getResultsDirectory();

    void createTempDirectory();

    void deleteTempDirectory();

    boolean isCleanup();

}
