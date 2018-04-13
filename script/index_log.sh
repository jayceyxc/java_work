#!/bin/sh

if [ $# -eq 1 ]; then
    day=${1}
else
    day=`date -d "1 day ago" "+%Y%m%d"`
fi

log_dir=/data/logs/sys
#log_dir=/home/adpush/java_ELK/temp

echo ${log_dir}
echo ${day}

if [ $# -eq 1 ]; then
    read -p "Do you want to index the log for day $1? [Y/N]?" choice
    if [ ${choice} != "Y" -a ${choice} != 'y' ]; then
        echo "quit the task"
        exit -1
    fi
fi

nohup java -cp java_work.jar com.bcdata.elk.IndexBidderLog -d ${log_dir} -t ${day} &