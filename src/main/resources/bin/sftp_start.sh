#!/bin/bash
HOME=/APP/a2s/sftp

JAR_FILE=sftp_module-jar-with-dependencies.jar
PATH_TO_JAR=$HOME/lib/$JAR_FILE
JAVA_CONF=$HOME/config

SERVICE_NAME=SFTP_MODULE

JAVA_OPT="-Dlogback.configurationFile=$HOME/config/logback.xml"

if [ -f "$PATH_TO_JAR" ]; then
  /usr/bin/java $JAVA_OPT $DEBUG -classpath $PATH_TO_JAR media.platform.sftp.SftpMain $JAVA_CONF/ > /dev/null 2>&1 $HOME/bin/stderr &
  if [ $? -eq 0 ];then
    echo "$SERVICE_NAME started ..."
  else
    echo "(ERROR) start fail : $?"
    exit 4
  fi
else
  echo "sftp($PATH_TO_JAR) is not found"
fi
