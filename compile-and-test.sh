#!/usr/bin/env bash
#
# The MIT License
# Copyright © 2018
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

#
# script to start influxdb and compile influxdb-java with all tests.
#
set -e

DEFAULT_INFLUXDB_VERSION="1.6"
DEFAULT_MAVEN_JAVA_VERSION="3-jdk-8-slim"
DEFAULT_RUN_NIGHTLY_BINARY="false"

INFLUXDB_VERSION="${INFLUXDB_VERSION:-$DEFAULT_INFLUXDB_VERSION}"
MAVEN_JAVA_VERSION="${MAVEN_JAVA_VERSION:-$DEFAULT_MAVEN_JAVA_VERSION}"
RUN_NIGHTLY_BINARY="${RUN_NIGHTLY_BINARY:-$DEFAULT_RUN_NIGHTLY_BINARY}"

if [ ! "$RUN_NIGHTLY_BINARY" == "true" ]; then

    echo "Run tests on InfluxDB-${INFLUXDB_VERSION}"

    docker kill influxdb || true
    docker rm influxdb || true
    docker network remove influxdb || true

    #
    # Create network
    #
    docker network create influxdb
    INFLUXDB_IP=influxdb
    PLATFORM_IP=influxd
    FLUX_IP=influxdb
    DOCKER_NET=influxdb

    #
    # InfluxDB
    #
    docker pull influxdb:${INFLUXDB_VERSION}-alpine || true
    docker run \
              --detach \
              --name influxdb \
              --net=influxdb \
              --publish 8086:8086 \
              --publish 8082:8082 \
              --publish 8089:8089/udp \
              --volume ${PWD}/config/influxdb.conf:/etc/influxdb/influxdb.conf \
          influxdb:${INFLUXDB_VERSION}-alpine

    # wait for InfluxDB
    sleep 3
fi

case "$OSTYPE" in
  darwin*)
    archive='darwin_amd64';
    conf='influxdb_mac';
     ;;
  linux*)
    archive="linux_amd64";
    conf='influxdb_travis';
esac

if [ "$RUN_NIGHTLY_BINARY" == "true" ]; then

    echo "Run tests on InfluxDB nightly binary"

    rm -rf ./influxdb-*

    wget https://dl.influxdata.com/influxdb/nightlies/influxdb-nightly_${archive}.tar.gz -O influxdb-nightly.tar.gz
    tar zxvf influxdb-nightly.tar.gz
    mv `find . -name influxdb-1.7.*` influxdb-nightly

    killall influxd || true

    ./influxdb-nightly/usr/bin/influxd -config ./config/${conf}.conf &>./influxdb-nightly.log &

    # Wait for start InfluxDB
    echo "Wait 5s to start InfluxDB"
    sleep 5

    ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'
    INFLUXDB_IP=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | grep 10`
    FLUX_IP=${INFLUXDB_IP}
    PLATFORM_IP=${INFLUXDB_IP}
    DOCKER_NET=host
fi

#
# Platform
#
rm -rf ./platform-nightly*
wget http://167.114.231.105/nightlies/influxd_nightly_${archive}.tar.gz -O platform-nightly.tar.gz
mkdir platform-nightly/ || true
rm ./influxd.bolt || true
tar zxvf platform-nightly.tar.gz -C platform-nightly/
./platform-nightly/influxd  &>./platform-nightly.log &

echo "Wait 5s to start Platform"
sleep 5

echo "INFLUXDB_IP: " ${INFLUXDB_IP} " FLUX_IP: " ${FLUX_IP} " PLATFORM_IP: " ${PLATFORM_IP}

test -t 1 && USE_TTY="-t"

docker run -i ${USE_TTY} --rm \
       --volume ${PWD}:/usr/src/mymaven \
       --volume ${PWD}/.m2:/root/.m2 \
       --workdir /usr/src/mymaven \
       --net=${DOCKER_NET} \
       --env INFLUXDB_VERSION=${INFLUXDB_VERSION} \
       --env INFLUXDB_IP=${INFLUXDB_IP} \
       --env FLUX_IP=${FLUX_IP} \
       --env PLATFORM_IP=${PLATFORM_IP} \
       maven:${MAVEN_JAVA_VERSION} mvn clean install -U

docker kill influxdb || true
