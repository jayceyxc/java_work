#!/bin/sh

if [ $# -ne 1 ]; then
    echo "Usage: $(basename $0) <index like bidder>"
    exit 1
fi

echo $1

read -p "Do you want to force merge for index $1? [Y/N]?" choice
if [ ${choice} == "Y" -o ${choice} == 'y'  ]; then
	nohup java -cp java_work.jar com.bcdata.elk.ForceMerge -i $1 &
else
	echo "quit job"
fi