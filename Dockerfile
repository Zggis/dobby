FROM linuxserver/ffmpeg:version-7.1-cli
###Base Image is Ubuntu Noble

###Install Required Linux Operations
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install subversion zlib1g-dev gcc make wget dpkg libcurl3-gnutls libmms0 dos2unix unzip python3

###Install JAVA and required dependencies
RUN apt-get -y install libc6-x32 libc6-i386 libasound2t64 libxi6 libxrender1 libxtst6 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN wget https://download.oracle.com/java/17/archive/jdk-17.0.12_linux-x64_bin.deb
RUN dpkg -i jdk-17.0.12_linux-x64_bin.deb

###Copy Dependencies
#MP4Box - Download source zip here: https://github.com/gpac/gpac/releases (Don't forget to update version below, NEED TO RE-ZIP to match existing structure)
COPY /linux/gpac-2.4.0.zip /install/gpac.zip
RUN unzip /install/gpac.zip -d /install

#MediaInfo - https://mediaarea.net/en/MediaInfo/Download/Ubuntu
COPY /linux/libzen0v5_0.4.39-1_amd64.xUbuntu_20.04.deb /install/libzen.deb
COPY /linux/libmediainfo0v5_22.09-1_amd64.xUbuntu_20.04.deb /install/libmediainfo.deb
COPY /linux/mediainfo_22.09-1_amd64.xUbuntu_20.04.deb /install/mediainfo.deb

#DOVI_TOOL - https://github.com/quietvoid/dovi_tool
#Current Version 2.1.0
COPY /linux/dovi_tool /data/dovi_tool

###Open permissions for install
RUN chmod -R 777 /install
RUN chmod 777 /data/dovi_tool

###Install MP4Box
RUN cd /install/gpac ; ./configure --static-mp4box --use-zlib=no
RUN cd /install/gpac ; make -j4
RUN cd /install/gpac ; make install

###Install MKVToolNix
RUN wget -O /usr/share/keyrings/gpg-pub-moritzbunkus.gpg https://mkvtoolnix.download/gpg-pub-moritzbunkus.gpg
RUN echo 'deb [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ noble main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN echo 'deb-src [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ noble main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN apt-get update && \
    apt-get -y install -f mkvtoolnix && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

###Install MediaInfo
RUN dpkg -i /install/libzen.deb
RUN dpkg -i /install/libmediainfo.deb
RUN dpkg -i /install/mediainfo.deb

###Cleanup
RUN rm -rf /install

RUN chmod -R 777 /root

ADD /scripts/start-dobby.sh /files/start-dobby.sh
ADD /scripts/runas.sh /files/runas.sh
ADD /scripts/setuser /sbin/setuser
RUN dos2unix /files/start-dobby.sh
RUN dos2unix /files/runas.sh
RUN dos2unix /sbin/setuser
RUN chmod +x /sbin/setuser
RUN chmod +x /files/runas.sh
RUN chmod a+x /files/start-dobby.sh

# Remove packages not needed for runtime
RUN apt -y purge subversion
RUN apt -y purge gcc
RUN apt -y purge make
RUN apt -y purge wget
RUN apt -y purge dos2unix
RUN apt -y purge unzip

# Run as root by default
ENV PUID 0
ENV PGID 0
ENV UMASK 0000

###Install Dobby App
COPY /build/libs/*.jar app.jar

ENTRYPOINT ["sh","-c","/files/runas.sh $PUID $PGID $UMASK /files/start-dobby.sh"]
#ENTRYPOINT [ "bash" ]