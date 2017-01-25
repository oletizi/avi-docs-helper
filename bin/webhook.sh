#!/usr/bin/env bash

mydir=`dirname $0`
cd $mydir
mydir=`pwd`

cd $mydir/..
projdir=`pwd`

git pull
mvn -DskipTests clean compile exec:java
