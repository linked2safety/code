#!/bin/sh

> $2_XML

cd $(dirname $0)
./setTarget $1 $2 $2_XML $3

cd ../KnowledgeExtractionAndFilteringMechanism
python workflowLogging.py $4 $1 $2 $2_XML 0

rm -f $2_XML
