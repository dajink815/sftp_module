#!/bin/bash
HOME=/APP/a2s/SFTP

JAR_FILE=sftp_module-jar-with-dependencies.jar
PATH_TO_JAR=$HOME/lib/$JAR_FILE
JAVA_CONF=$HOME/config

SERVICE_NAME=SFTP_MODULE

JAVA_OPT="-Dlogback.configurationFile=$HOME/config/logback.xml"

#/usr/bin/java $JAVA_OPT $DEBUG -classpath $HOME/a2s/lib/$PATH_TO_JAR media.platform.a2s.ConvertibleCall_A2sMain $JAVA_CONF/ > /dev/null 2>&1 &

if [ -f "$PATH_TO_JAR" ]; then
  /usr/bin/java $DEBUG -classpath $PATH_TO_JAR media.platform.sftp.SftpMain $JAVA_CONF/ > /dev/null 2>&1 &
  if [ $? -eq 0 ];then
    echo "$SERVICE_NAME started ..."
  else
    echo "(ERROR) start fail : $?"
    exit 4
  fi
else
  echo "sftp($PATH_TO_JAR) is not found"
fi
