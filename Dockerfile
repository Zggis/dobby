FROM linuxserver/ffmpeg:amd64-latest
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install default-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f subversion && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f zlib1g-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f gcc  && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f make && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f mediainfo && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
COPY /linux/gpac gpac
RUN cd gpac ; ./configure --static-mp4box --use-zlib=no
RUN cd gpac ; make -j4
RUN cd gpac ; make install
ADD /linux/dovi_tool /data/dovi_tool
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN apt-get update && \
    apt-get -y install -f wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN wget -O /usr/share/keyrings/gpg-pub-moritzbunkus.gpg https://mkvtoolnix.download/gpg-pub-moritzbunkus.gpg
RUN echo 'deb [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ focal main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN echo 'deb-src [signed-by=/usr/share/keyrings/gpg-pub-moritzbunkus.gpg] https://mkvtoolnix.download/ubuntu/ focal main' >> /etc/apt/sources.list.d/mkvtoolnix.download.list
RUN apt-get update && \
    apt-get -y install -f mkvtoolnix mkvtoolnix-gui && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN chown 99 /root
RUN chgrp 100 /root
USER 99:100
#ENTRYPOINT [ "bash" ]
ENTRYPOINT ["java","-jar","./app.jar"]