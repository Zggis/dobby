<div align="center"><img height="400px" alt="logo" src="/logo.png?raw=true"/></div>

## <div align="right"><a href="https://www.buymeacoffee.com/zggis" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a></div>

### Description
Dobby will scan a media directory and look for MKV/MP4 Dolby Vision files along side MKV HDR video files for the same content. The application will merge the files to create BL+RPU MKV files compatible with both HDR and Dolby Vision. This allows you to enhance your media library by adding Dolby Vision at low cost of disk space and without the need to manage multiple file versions.

### Installing
#### Unraid
To install Dobby on Unraid you can install the docker container through the community applications. You will need to map the WORKSPACE in the application template.
#### Docker Desktop
You can run Dobby on Docker locally by using the following command. Replace MEDIA_DIR with the directory you have the files to be merged in.
```
$ docker run -v MEDIA_DIR:/data/media zggis/dobby:latest
```
Windows Example
```
$ docker run -v "C:/temp":/data/media zggis/dobby:latest
```

### Building the Code
Clone the repo and update application-local.properties as you need, then build with gradle. JAR should be placed in /build/libs
```
$ gradlew assemble
```

### Usage
Dobby is a Spring Batch application and currently has no GUI. You place your media files in the configured directory, start Dobby and let it do its job. Watching the logs will give you an indication of how the job is progressing, and when it completes Dobby will place your results in the configured directory and shutdown to save resources until you summon him again.<br>
I recommend you map Dobby to an empty directory to be used as its workspace. Move files to this directory as you need and start Dobby to process those files.
#### Example media directory content:
```
/data/media
├── Andor.S01E06.2160p.DSNP.WEB-DL.DDP5.1.Atmos.DV.MP4.x265.mp4 (Dolby Vision only MP4 file)
├── Andor.S01E06.HDR.2160p.WEB.h265.mkv (HDR only file)
├── Andor.S01E08.2160p.DSNP.WEB-DL.DDP5.1.Atmos.DV.MP4.x265.mkv (Dolby Vision only MKV file)
├── Andor.S01E08.2160p.DSNP.WEB-DL.x265.10bit.HDR.DDP5.1.Atmos.mkv (HDR only file)
├── dobbyResults (Created by Dobby)
│   ├── Andor.S01E06.HDR.2160p.WEB.h265[BL+RPU].mkv (Dolby Vision + HDR compatible file)
│   └── Andor.S01E08.Narkina.5.2160p.DSNP.WEB-DL.x265.10bit.HDR.DDP5.1.Atmos[BL+RPU].mkv (Dolby Vision + HDR compatible file)
├── dobbyTemp (Created and cleaned up afterward by Dobby)
│   └── temp files
└── dobby.log
```
<br>
In this example the SXXEXX portion of the filenames will be used to match TV show episodes, but Dobby will also look at frame count and title to match movies or other media.
<br>
Once the application completes you should have two BL+RPU MKV files in the configured RESULTS directory one for each episode. The originals will remain untouched. Temporary files will be created during the job in a configured TEMP directory.<br>
The operations are disk space intensive, expect it to use 3x the disk space required by the directory prior to the job. By default temporary files are cleaned up after the job completes. Resulting files should be similar in size to the original HDR files, allowing you to save space by discarding the original Dolby Vision MKV/MP4 files afterward.<br>
 
### Configuration

#### Required Path
Name | Type | Container Path | Description
--- | --- | --- | ---
WORKSPACE | PATH | /data/media | Map this to the directory where you can load the media files you want to merge.

#### Optional Variables
Container Variable | Default Value | Description
--- | --- | ---
PUID | 0 (99 in Unraid template) | Controls the user the container runs as.
PGID | 0 (100 in Unraid template) | Controls the group the container runs as.
UMASK | 0000 | Controls the UMASK the container uses.
RESULTS | /data/media/dobbyResults | Sets the directory relative to the container to save result files in. By default results are placed in a subdirectory of the configured WORKSPACE.
TEMP | /data/media/dobbyTemp | Sets the directory relative to the container to save temporary files in. By default temporary files are placed in a subdirectory of the configured WORKSPACE.
LOG | /data/media | Sets the directory relative to the container to save the dobby.log file in. Logs are not appended, a new file is created and will overwrite an existing file each time.
CLEANUP | true | Set to false if you would like the TEMP directory to be left alone after the job completes. This may be useful when debugging.
AAVALIDATE | true | Set to false if you would like to skip the active area validation step.
ACCEPTBLRPUINPUT | false | Set to true if you want to accept input files that already have DV+HDR. This is helpful if you are converting your original DV+HDR files and want to add the DV data back to the converted result from your original DV+HDR file. This process will likely require you to set AAVALIDATE to false.
RESULTSUFFIX | [BL+RPU] | Value that is appended at the end of the result file.
LOGLEVEL | INFO | Set to DEBUG or TRACE to inclease the logging level for debugging.

### Notifications
Dobby does not natively support notifications, <strong>however</strong> it can be used in conjunction with my other application <a href="https://github.com/Zggis/howler">Howler</a>, which montiors log files and sends out notifications for specific events. Howler works great with Dobby, almost like they are from the same world. You can use Dobby's log file directory as a <a href="https://github.com/Zggis/howler">Howler</a> data source, and setup an alert with trigger event [COMPLETED] to be notified when a job completes.

### Hardware Acceleration
Most of Dobby's operations are done using the MKVToolNix suite which cannot be hardware accelerated. There is one operation that uses FFMPEG to scan the active area of a video file in 4 sample locations. This operation is compatible with hardware acceleration. I have tested it using NVIDA GPU by setting container variable 'HWACC' to '-hwaccel cuda' and including '--runtime=nvidia' in the extra docker run parameters.

### FAQ
**Question:** Does it work for movies?

**Answer:** Yes, but I have found there can be inconsistency with movie files since you often have versions that are different lengths (theatrical, extended), or have different borders (IMAX). Dobby checks for these discrepancies between the Dolby Vision and HDR files and will fail a job if it cannot validate the files are the same content. The matching algorithm will take frame count into consideration along with title. For files that don't have a SXX0XX episode key in the title, you should ensure there are matching portions in the filenames prior to the resolution (.2160p.) to get a match.
##
**Q:** Why is it called Dobby?

**A:** Because my Unraid server is named 'Dumbledore' and my WiFi is named 'Hogwarts' I have a theme to maintain.

##
**Q:** How does it create Dolby Vision data?

**A:** It does not create Dolby Vision data, it rips the Dolby Vision data from one file and merges it into a standard HDR file of the same content creating a result that has Dolby Vision but remains HDR10 compatible for devices that don't support Dolby Vision. The raw Dolby Vision data is relatively small in size, around 10MB per 30m of video, so your end result will only be slightly larger than your original HDR file.
##
**Q:** I started it, but there is no option for a WebGUI.

**A:** Dobby has no GUI. When you start the app it will run the merge job and shutdown when it completes. The console logs should be more than sufficient to track the progress and status of a job. I will be looking at adding a GUI, but given the architecture of Dobby I would probably publish that as a seprate container so users can choose which to use.
##
**Q:** Is the merge lossless?

**A:** Yes, Dobby does not do any transcoding (The FFMPEG library is only used for validation). The audio and subtitle tracks of your original HDR file should be coppied over into the BL+RPU result. The original Dolby vision file is only used for the Dolby Vision metadata, its other tracks are ignored. 
##
**Q:** Can it output results in other formats, or just MKV?

**A:** Right now Dobby only creates result files in MKV. While I could (and still might) add support to convert the final MKV result to MP4, there are plenty of other tools available that would do a better job of that.
##
**Q:** What about 1080p content?

**A:** I have not done as much testing with 1080p files as I have with 2160p, but what limited testing I have done has shown 1080p files work provided both your HDR and Dolby Vision file are 1080p.
##

### Additional Links
These additional references should also help you:

* [FFMPEG Hardware Acceleration](https://docs.nvidia.com/video-technologies/video-codec-sdk/ffmpeg-with-nvidia-gpu/#basic-testing)

