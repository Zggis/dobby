FROM linuxserver/ffmpeg
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive \
    apt-get -y install default-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
RUN apt-get update && \
    apt-get -y install -f mkvtoolnix-gui && \
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
#ENTRYPOINT [ "bash" ]
ADD /linux/dovi_tool /data/dovi_tool
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
USER 99:100
ENTRYPOINT ["java","-jar","./app.jar"]