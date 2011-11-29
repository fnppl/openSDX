#!/bin/bash

cp=bin
for i in lib/*.jar ; do
cp=${cp}:${i}
done

java -cp ${cp} $@

#ret=$?
#echo exec.sh :: ret ${ret}
#exit ${ret}

