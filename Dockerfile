FROM linuxserver/ffmpeg:amd64-latest
###Base Image is Ubuntu Focal

###Copy Dependencies
#MP4Box - Clone SVO Repo: https://svn.code.sf.net/p/gpac/code/trunk/gpac
COPY /linux/gpac /install/gpac

#MediaInfo - https://mediaarea.net/en/MediaInfo/Download/Ubuntu
COPY /linux/libzen0v5_0.4.39-1_amd64.xUbuntu_20.04.deb /install/libzen.deb
COPY /linux/libmediainfo0v5_22.09-1_amd64.xUbuntu_20.04.deb /install/libmediainfo.deb
COPY /linux/mediainfo_22.09-1_amd64.xUbuntu_20.04.deb /install/mediainfo.deb

#DOVI_TOOL - https://github.com/quietvoid/dovi_tool
COPY /linux/dovi_tool /data/dovi_tool

RUN chmod -R 777 /install

###Install Required Linux Operations
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install default-jre-headless subversion zlib1g-dev gcc make wget dpkg libcurl3-gnutls libmms0 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

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

RUN chown 99 /root
RUN chgrp 100 /root
USER 99:100
#ENTRYPOINT [ "bash" ]
ENTRYPOINT ["java","-jar","./app.jar"]