#!/bin/sh

> $4_varMeanings
> $4_XML

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $1 $4_varMeanings

cd ../SingleHypothesisTesting
R --slave --args $1 $2 $3 $4 $4_XML $4_varMeanings < BinomialLogisticRegression.R
rm -f $4_varMeanings

cd ../KnowledgeExtractionAndFilteringMechanism
python workflowLogging.py $5 $1 $4 $4_XML 1
rm -f $4_XML
