#!/bin/sh

> $4_varMeanings
> $4_XML

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $1 $4_varMeanings

cd ../QualityControl
./hardyWeinbergEquilibrium $1 $2 $3 $4 $4_XML $4_varMeanings

rm -f $4_varMeanings

cd ../KnowledgeExtractionAndFilteringMechanism
python workflowLogging.py $5 $1 $4 $4_XML 0

rm -f $4_XML
