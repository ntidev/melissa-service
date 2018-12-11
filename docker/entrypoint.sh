#!/bin/sh

# ====== Variables
APP_HOME=/app
CONFIG_DIR=$APP_HOME/etc
LOG_CONFIG_FILE=$CONFIG_DIR/log4j2.xml
CONFIG_FILE=$CONFIG_DIR/config.json

# Enabled CLUSTER BY DEFAULT
#ENABLE_CLUSTER="false"

#Java Virtual Machine Params  with -D
JVM_OPTS=""

#Vertx params
VERTX_OPTS=""

# Set Log4j as default logger
JVM_OPTS="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory"

# ====== End variables

echo "Starting ....."

# Setting the logger config file
if [ -f $LOG_CONFIG_FILE ]
then
    echo "INFO: Using a custome Log4J file"
    JVM_OPTS="$JVM_OPTS -Dlog4j.configurationFile=$LOG_CONFIG_FILE" 
else 
   if [ ! -f $LOG_CONFIG_FILE ]
   then 
        cp $APP_HOME/log4j2.xml $CONFIG_DIR/log4j2.example.xml
   fi
fi


# Setting the system config file
if [ -f $CONFIG_FILE ]
then
    VERTX_OPTS="$VERTX_OPTS -conf $CONFIG_FILE " 
else 
   if [ ! -f $CONFIG_FILE ]
   then 
        cp $APP_HOME/config.json $CONFIG_DIR/config.example.json
   fi
fi

if [ ! -z $ENABLE_CLUSTER ]
then
   VERTX_OPTS="$VERTX_OPTS -cluster "
fi

# Export variables that will be use on Java
export CONFIG_DIR

#end export

exec java \
     $JVM_OPTS \
     -jar $APP_HOME/app.jar \
     $VERTX_OPTS