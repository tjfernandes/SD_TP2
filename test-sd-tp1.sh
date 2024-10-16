#!/bin/bash

[ ! "$(docker network ls | grep sdnet )" ] && \
	docker network create --driver=bridge sdnet


if [  $# -le 1 ] 
then 
		echo "usage: $0 -image <img> [ -test <num> ] [ -log OFF|ALL|FINE ] [ -sleep <seconds> ]"
		exit 1
fi 

# get the latest version
docker pull nunopreguica/sd2122-tester-tp2-alpha

# execute the client with the given command line parameters
docker run --rm --network=sdnet -it -v /var/run/docker.sock:/var/run/docker.sock nunopreguica/sd2122-tester-tp2-alpha $*

