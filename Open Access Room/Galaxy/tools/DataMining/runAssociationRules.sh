#!/bin/sh

> $6_varMeaning
> $6_XML

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $2 $6_varMeanings

cd ../DataMining
java -Xmx6g -cp DataMining.jar AssociationRules multi_adverse_events $1 $2 $6_varMeanings $3 $4 $5 $6 $6_XML

rm -f $6_varMeanings

cd ../KnowledgeExtractionAndFilteringMechanism
python workflowLogging.py $7 $2 $6 $6_XML 1

rm -f $6_XML

