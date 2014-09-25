#!/bin/sh

> $6_varMeaning
> $6_TREE
> $6_TREE_VIZ
> $6_XML
> $6_no_duplicate_rules


cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $2 $6_varMeaning

cd ../DataMining
java -Xmx6g -cp DataMining.jar RandomForest train_test_split $1 $2 $6_varMeaning $3 $4 $5 $6_TREE $6 $6_XML
VAL=$?

rm -f $6_varMeaning

if [ $VAL = '0' ] 
then 
      java -Xmx6g -cp DataMining.jar RandomForestGraph $6_TREE $6_TREE_VIZ
      dot -Tpdf $6_TREE_VIZ -o $7
      rm -f $6_TREE $6_TREE_VIZ
      cd ../KnowledgeExtractionAndFilteringMechanism
      python workflowLogging.py $8 $2 $6 $6_XML 1
      rm -f $6_XML
      #remove any duplicate rules from association rules file
      cd ../DataMining
      python removeDuplicateRules.py $6 $6_no_duplicate_rules
      mv $6_no_duplicate_rules $6  
elif [ $VAL = '9' ] 
then
      java -Xmx6g -cp DataMining.jar DecisionTreeGraph $6_TREE $6_TREE_VIZ 
      dot -Tpdf $6_TREE_VIZ -o $7
      rm -f $6_TREE $6_TREE_VIZ
      cd ../KnowledgeExtractionAndFilteringMechanism
      python workflowLogging.py $8 $2 $6 $6_XML 1
      rm -f $6_XML
      #remove any duplicate rules from association rules file
      cd ../DataMining
      python removeDuplicateRules.py $6 $6_no_duplicate_rules
      mv $6_no_duplicate_rules $6
elif [ $VAL = '1' ] 
then
     echo "Exceptions are thrown by RandomForest.class"
elif [ $VAL = '2' ] 
then
     echo -e "The target variable of the input data cube contains one value.\n This tool requires that the target variable contains at least two values. To use this tool, set a different variable as the target variable."
elif [ $VAL = '3' ]
then
     echo 'The data mining results file is not created by weka.'
elif [ $VAL = '4' ]
then
     echo -e "A random forest can not be built using the input data cube. \nThe input data cube is unsuitable for building a random forest."
else
     echo "$VAL is unrecognized." 
fi
