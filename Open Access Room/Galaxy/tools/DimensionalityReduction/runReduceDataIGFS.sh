#!/bin/sh

> $2_XML
> $2_varMeaning

cd $(dirname $0)
cd ../KnowledgeExtractionAndFilteringMechanism
python getCategoriesMeaning.py $2 $2_varMeaning

cd ../DimensionalityReduction

java -Xmx1024m -cp DimensionalityReduction.jar ReduceDataInformationGainFeatureSelection $2_varMeaning $1 $2 $3 $4 $5 $2_XML
VAL=$?

if [ $VAL = '0' ] 
then
 cd ../KnowledgeExtractionAndFilteringMechanism
 python workflowLogging.py $6 $2 $5 $2_XML 0
 rm -f $2_XML
elif [ $VAL = '1' ]
then
   echo "Exceptions are thrown by ReduceDataInformationGainFeatureSelection.class"  
elif [ $VAL = '2' ]
then
   echo -e "The target variable of the input data cube contains one value.\n This tool requires that the target variable contains at least two values. To use this tool, set a different variable as the target variable."
else
   echo "$VAL is unrecognized." 
fi
