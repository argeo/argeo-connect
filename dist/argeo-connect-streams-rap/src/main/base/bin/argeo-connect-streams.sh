#!/bin/bash

JAVA_CMD=java
JAVA_OPTS=-Xmx512m

APP_HOME_DIR=${HOME}/.argeo/connect/streams/
if [ -f $APP_HOME_DIR/settings.sh ];then
	. $APP_HOME_DIR/settings.sh
fi

# Find home
if [ -z "$APP_INSTALL_DIR" -o ! -d "$APP_INSTALL_DIR" ] ; then
  ## resolve links - $0 may be a link to home
  PRG="$0"
  progname=`basename "$0"`

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  APP_INSTALL_DIR=`dirname "$PRG"`/..

  # make it fully qualified
  APP_INSTALL_DIR=`cd "$APP_INSTALL_DIR" && pwd`
fi

cd "$APP_INSTALL_DIR" \
 && $JAVA_CMD $JAVA_OPTS \
 -jar modules/org.eclipse.osgi-3.9.0.v20130305-2200.jar \
 -configuration conf \
 -data data \
 -console 3030 \
 -clear