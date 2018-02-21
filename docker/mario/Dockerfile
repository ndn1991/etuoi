FROM openjdk:8-jdk

#install maven
WORKDIR /root
RUN wget http://mirror.downloadvn.com/apache/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
RUN unzip apache-maven-3.5.0-bin.zip
RUN echo $JAVA_HOME
ENV PATH=/root/apache-maven-3.5.0/bin:$PATH

#get mario
RUN mkdir -p /root/marios
ENV MARIO_VERSION=3.18.1
RUN git clone https://github.com/bachden/mario3.git
RUN cd mario3; git checkout tags/v$MARIO_VERSION

#setup maven config
RUN mkdir -p /root/.m2
COPY settings.xml /root/.m2/settings.xml

#install mario
RUN cd mario3/Mario; mvn clean package
RUN cd mario3/Mario; mv target/mario-$MARIO_VERSION /root/marios/
RUN ln -sfn /root/marios/mario-$MARIO_VERSION /root/mario
RUN mkdir -p /root/mario/extensions
RUN chmod +x /root/mario/*.sh

#change heap size
ENV MARIO_HEAP=128m
RUN sed -i -- "s/Xmx512M/Xmx$MARIO_HEAP/g" /root/mario/mario.vmoptions
RUN sed -i -- "s/Xms512M/Xms$MARIO_HEAP/g" /root/mario/mario.vmoptions
# RUN sed -i -- "s/#!\/bin\/sh/#!\/bin\/bash/g" /root/mario/start-mario-service.sh
# RUN sed -i -- "s/#!\/bin\/sh/#!\/bin\/bash/g" /root/mario/start-mario.sh

COPY mario-entrypoint.sh /usr/bin/mario-entrypoint.sh
RUN chmod +x /usr/bin/mario-entrypoint.sh

COPY wait-for-it.sh /usr/bin/wait-for-it.sh
RUN chmod +x /usr/bin/wait-for-it.sh