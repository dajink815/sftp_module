#!/bin/bash

#dir_base=/a2s_cdr
cdr_base=/a2s_cdr
bk_dir=$cdr_base/backup

echo "cd $bk_dir"

tgt_cdr_list_file=""
tgt_cdr_file=""

YESTERDAY=$(date +%Y%m%d -d "1 days ago")

cd $bk_dir
cdr_info_file=$(find . -maxdepth 1 -type f -name "CACS.D$YESTERDAY.*.B01.INFO")
if [ ! -z "$cdr_info_file" ]; then
	echo "origin_file cdr info file=$cdr_info_file"
	tgt_cdr_file=$(ls $cdr_info_file | awk -F '/' '{print $(NF)}' | awk -F "." '{print $1"."$2"."$3"."$4"."$5}')
	tgt_cdr_list_file=$tgt_cdr_file".LIST"
	if [ ! -f $tgt_cdr_list_file ]; then
		echo "LIST not found=$tgt_cdr_list_file"
		mv $cdr_info_file $cdr_base/.
		mv $tgt_cdr_file $cdr_base/.
	else
		echo "LIST existed=$tgt_cdr_list_file"
	fi
	echo ""
fi

cd $cdr_base
cdr_list_file=$(find . -maxdepth 1 -type f -name "CACS.D$YESTERDAY.*.B01.LIST")
if [ ! -z "$cdr_list_file" ]; then
	echo "origin cdr list file=$cdr_list_file"
	tgt_cdr_file=$(ls $cdr_list_file | awk -F '/' '{print $(NF)}' | awk -F "." '{print $1"."$2"."$3"."$4"."$5}')
	tgt_cdr_info_file=$tgt_cdr_file".INFO"
	mv $cdr_list_file $bk_dir/.
	mv $tgt_cdr_info_file $bk_dir/.
	mv $tgt_cdr_file $bk_dir/.
	echo ""
fi

