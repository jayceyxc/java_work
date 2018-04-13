#!/bin/sh

if [ $# -ne 1 ]; then
    echo "Usage: $(basename $0) <type like rtb_log_2018_04_01>"
    exit 1
fi

echo $1

read -p "Do you want to delete the record for type $1? [Y/N]?" choice
if [ ${choice} == "Y" -o ${choice} == 'y'  ]; then
	nohup java -cp java_work.jar com.bcdata.elk.DeleteBidderLog -d $1 &
else
	echo "quit job"
fi
