#!/bin/sh

> $3_varMeanings
> $3_XML

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $1 $3_varMeanings

cd ../QualityControl
./nonApplicableDataQC $1 $2 $3 $3_XML $3_varMeanings

rm -f $3_varMeanings

cd ../KnowledgeExtractionAndFilteringMechanism
python workflowLogging.py $4 $1 $3 $3_XML 0

rm -f $3_XML
