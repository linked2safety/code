#!/bin/sh

> $5_varMeaning
> $5_TREE
> $5_TREE_VIZ
> $5_XML

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $2 $5_varMeaning

cd ../DataMining

java -Xmx6g -cp DataMining.jar DecisionTrees cross_validation $1 $2 $5_varMeaning $3 $4 $5_TREE $5 $5_XML
VAL=$?

rm -f $5_varMeaning

if [ $VAL = '0' ] 
then 
  java -Xmx6g -cp DataMining.jar DecisionTreeGraph $5_TREE $5_TREE_VIZ
  dot -Tpdf $5_TREE_VIZ -o $6
  rm -f $5_TREE $5_TREE_VIZ
  cd ../KnowledgeExtractionAndFilteringMechanism
  python workflowLogging.py $7 $2 $5 $5_XML 1
  rm -f $5_XML
elif [ $VAL = '1' ] 
then
     echo "Exceptions are thrown by DecisionTrees.class"
elif [ $VAL = '2' ] 
then
     echo -e "The target variable of the input data cube contains one value.\n This tool requires that the target variable contains at least two values. To use this tool, set a different variable as the target variable."
elif [ $VAL = '3' ]
then
     echo 'The data mining results file is not created by weka.'
elif [ $VAL = '4' ]
then
     echo -e "A decision tree can not be built using the input data cube.\nThe input data cube is unsuitable for building a decision tree."
else
     echo "$VAL is unrecognized." 
fi
  
