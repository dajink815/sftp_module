#!/bin/bash
HOME=/APP/a2s/sftp

PATH_TO_JAR=$HOME/lib/sftp_module-jar-with-dependencies.jar


/usr/bin/java -jar $PATH_TO_JAR key ${1}