<div align="center"><img height="400px" alt="logo" src="/logo.png?raw=true"/></div>

## <div align="right"><a href="https://www.buymeacoffee.com/zggis" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a></div>

### Description
Dobby will scan a media directory and look for MKV/MP4 Dolby Vision files along side MKV HDR video files for the same TV Show episode (Support for movies is comming soon, see FAQ). The application will proceed to merge the files to create BL+RPU MKV files compatible with HDR and Dolby Vision. This process can allow you to enhance your media library by adding Dolby Vision at low cost of disk space and without the need to manage multiple file versions.

### Installing
#### Unraid
To install Dobby on Unraid you can install the docker container through the community applications.
#### Docker Desktop
To install Dobby on Docker desktop you can pull the latest image from dockerhub: https://hub.docker.com/r/zggis/dobby
#### Windows
You can run Dobby as a Java program from command prompt. Java JRE 11 required. Grab the latest JAR and run:
```
$ java -jar -Dspring.profiles.active=local dobby.jar --media.dir=C:/temp
```

### Building the Code
Clone the repo and update application-local.properties as you need, then build with gradle. JAR should be placed in /build/libs
```
$ gradlew assemble
```

### Usage
Dobby is a Spring Batch application and currently has no GUI. You place your media files in the configured directory, start Dobby and let it do its job. Watching the logs will give you an indication of how the job is progressing, and when it completes Dobby will place your results in the configured directory and shutdown to save resources until you summon him again.<br>
Example media directory content:<br>
* Andor.S01E06.2160p.DSNP.WEB-DL.DDP5.1.Atmos.DV.MP4.x265.mp4<br>
* Andor.S01E06.HDR.2160p.WEB.h265.mkv<br>
* Andor.S01E08.Narkina.5.2160p.DSNP.WEB-DL.DDP5.1.Atmos.DV.MP4.x265.mp4<br>
* Andor.S01E08.Narkina.5.2160p.DSNP.WEB-DL.x265.10bit.HDR.DDP5.1.Atmos.mkv<br>
<br>
In this example the SXXEXX portion of the filenames will be used to match episodes. The name of the TV Show 'Andor' is not considered, <strong>so for now you can only load the directory with one TV Show at a time.</strong> I have plans to improve upon this in the future.<br>
Once the application completes you should have two BL+RPU MKV files in the configured RESULTS directory one for each episode. The originals will remain untouched. Temporary files will be created during the job in a configured TEMP directory.<br>
The operations are disk space intensive, expect it to use 3x the disk space required by the directory prior to the job. Temporary files are cleaned up upon job completion by default. Resulting files should be similar in size to the original HDR files, allowing you to save space by discarding the Dolby Vision MKV/MP4 files afterward.<br>
 
### Configuration

Name | Optional | Type | Default/Example | Description
--- | --- | --- | --- | ---
/data/media | NO | PATH | None | Map this to the directory you want to use for your media files.
HWACC | YES | VARIABLE | Disabled | Set to the FFMPEG harware acceleration flag such as '-hwaccel cuda' for NVIDA GPUs to use hardware acceleration. See Hardware Acceleration below.
Runtime | YES | Extra Docker Run Parameter | --runtime=nvidia | Used with HWACC. See Hardware Acceleration below.
UID/GUID | YES | Extra Docker Run Parameter | --user=99:100 | Controls the user/group the container runs as.
RESULTS | YES | VARIABLE | /data/media/dolbyResults | Sets the directory relative to the container to save result files in.
TEMP | YES | VARIABLE | /data/media/dolbyTemp | Sets the directory relative to the container to save result temporary in.
LOG | YES | VARIABLE | /data/media | Sets the directory relative to the container to save the dobby.log file in. Logs are not appended, a new file is created and will overwrite an existing file each time.
CLEANUP | YES | VARIABLE | true | Set to false if you would like the TEMP directory to be left alone after the job completes. This may be useful when debugging.

### Hardware Acceleration
Most of Dobby's operations are done using the MKVToolNix suite which cannot be hardware accelerated. There is one operation that uses FFMPEG to scan the active area of a video file in 4 sample locations. This operation is compatible with hardware acceleration. I have tested it using NVIDA GPU by setting HWACC to '-hwaccel cuda' and including '--runtime=nvidia' in the extra docker run parameters.

### FAQ
**Question:** Why is it called Dobby?

**Answer:** Because my Unraid server is named 'Dumbledore' and my WiFi is named 'Hogwarts' I have a theme to maintain.

##
**Q:** How does it create Dolby Vision data?

**A:** It does not create Dolby Vision data, it rips the Dolby Vision data from one file and merges it into a standard HDR file of the same content creating a result that has Dolby Vision but remains HDR10 compatible for devices that don't support Dolby Vision. The raw Dolby Vision data is relatively small in size, around 10MB per 30m of video, so your end result will only be slightly larger than your original HDR file.
##
**Q:** I started it, but there is no option for a WebGUI.

**A:** Dobby is a Spring Batch application, it has no GUI. When you start the app it will run the merge job and shutdown when it completes. The console logs should be more than sufficient to track the progress and status of a job.
##
**Q:** Does it work for movies?

**A:** Right now Dobby only officially supports TV Shows, though if you get clever and rename your movies with matching SXXEXX keys in the title Dobby will attempt to merge them. I have found there is far more inconsistency with movie files since you often have versions that are different lengths, or different borders (IMAX). Dobby checks for these discrepancies between the Dolby Vision and HDR files and will fail a job if it cannot validate the files are the same. So if you attempt this do it with caution. I will try to add movie support in the future.
##
**Q:** Is the merge lossless?

**A:** Yes, Dobby does not do any transcoding (The FFMPEG library is only used for validation). The audio and subtitle tracks of your original HDR file should be coppied over into the BL+RPU result. The original Dolby vision file is only used for the Dolby Vision metadata, its other tracks are ignored. 
##

### Additional Links
These additional references should also help you:

* [FFMPEG Hardware Acceleration](https://docs.nvidia.com/video-technologies/video-codec-sdk/ffmpeg-with-nvidia-gpu/#basic-testing)

