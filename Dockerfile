FROM linuxserver/ffmpeg:version-4.4-cli
###Base Image is Ubuntu Focal

###Install Required Linux Operations
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install default-jre-headless subversion zlib1g-dev gcc make wget dpkg libcurl3-gnutls libmms0 dos2unix unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

###Copy Dependencies
#MP4Box - Clone SVO Repo: https://svn.code.sf.net/p/gpac/code/trunk/gpac and zip it up
COPY /linux/gpac.zip /install/gpac.zip
RUN unzip /install/gpac.zip -d /install

#MediaInfo - https://mediaarea.net/en/MediaInfo/Download/Ubuntu
COPY /linux/libzen0v5_0.4.39-1_amd64.xUbuntu_20.04.deb /install/libzen.deb
COPY /linux/libmediainfo0v5_22.09-1_amd64.xUbuntu_20.04.deb /install/libmediainfo.deb
COPY /linux/mediainfo_22.09-1_amd64.xUbuntu_20.04.deb /install/mediainfo.deb

#DOVI_TOOL - https://github.com/quietvoid/dovi_tool
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
RUN echo 'deb [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ focal main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN echo 'deb-src [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ focal main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN apt-get update && \
    apt-get -y install -f mkvtoolnix mkvtoolnix-gui && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

###Install MediaInfo
RUN dpkg -i /install/libzen.deb
RUN dpkg -i /install/libmediainfo.deb
RUN dpkg -i /install/mediainfo.deb

###Install Dobby App
COPY /build/libs/*.jar app.jar

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

ENTRYPOINT ["sh","-c","/files/runas.sh $PUID $PGID $UMASK /files/start-dobby.sh"]