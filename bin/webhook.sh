#!/usr/bin/env bash

mydir=`dirname $0`
cd $mydir
mydir=`pwd`

cd $mydir/..
projdir=`pwd`

mvn -DskipTests clean package

java -cp $projdir/target/avi-docs-snarfer-1.1-jar-with-dependencies.jar com.avinetworks.docs.web.PushHandler