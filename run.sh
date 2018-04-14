#!/bin/bash

clear

PATH=$1
AMOUNT=$2

if [ $# -ne 2 ]; then
	echo "Must provide 2 parameters: [market_data_file_path] + [amount]"
	exit 1
fi

JAVA_VERSION=`echo "$(java -version 2>&1)" | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`

echo $JAVA_VERSION
$JAVA_HOME/java -jar ./build/libs/loan_provider-1.0.jar $PATH $AMOUNT


