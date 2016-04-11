#!/bin/bash

echo "Accessing: $4/job/$1 for user $1"

curl -i --user $1:$2 \
     -H cipres-appkey:$3 \
      $4/job/$1 \
     -F tool='PY_TG' \
     -F input.infile_=@$5 \
     -F metadata.clientJobId=1234546 \
     -F metadata.statusEmail=true