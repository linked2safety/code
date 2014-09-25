 # # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
 #
 #   BinomialLogisticRegression.R - Uses the logistic regression model to identify associations
 #   between the predictors and the target variable and their odds ratios
 #   
 #   Copyright (C) 2014  -  University of Cyprus  -  aristodimou.aristos@ucy.ac.cy
 # 
 #   This program is free software: you can redistribute it and/or modify
 #   it under the terms of the GNU General Public License as published by
 #   the Free Software Foundation, either version 3 of the License, or
 #   (at your option) any later version.
 #
 #   This program is distributed in the hope that it will be useful,
 #   but WITHOUT ANY WARRANTY; without even the implied warranty of
 #   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 #   GNU General Public License for more details.
 #
 #   You should have received a copy of the GNU General Public License
 #   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 # 
 #   You may download the source code and associated license at  https://github.com/linked2safety/code
 #
 # # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

#Define the value that will be used for missing values
MISSING_VAL = NA;

#Function for finding the mode of a variable (most frequent value)
modeFun <- function(x) {
	x <- na.omit(x)
	ux <- unique(x)
	return(ux[which.max(tabulate(match(x, ux)))])
}

#LOAD THE DATA and get the dimensions
inpData <- read.table(commandArgs()[4], header=T, sep=",");
rows <- nrow(inpData);
columns <- ncol(inpData);
varNames <- colnames(inpData);

#User choice about the deletion of missing values from the predictor variables
removeMissing<-commandArgs()[5]

#User choice about the iterations of glm
Iterations<-as.integer(commandArgs()[6])

#Convert missing values to MISSING_VAL
con <- file(commandArgs()[9], "r")
varMeanings<-readLines(con)
close(con)
varMeaningsVarNames = varNames;
for(varIndx in 1:(columns-1))
{
	tokens = unlist(strsplit(varMeanings[varIndx], "[|]"))
  varMeanings[varIndx] <- paste(tokens[1],tokens[2],sep=" => ")
	varName = tokens[1];
  varMeaningsVarNames[varIndx] = varName;
	#print(tokens[2])
	#print(inpData[varName])
	tokens = unlist(strsplit(as.character(tokens[2]), "[,]"))
	tokens = unlist(strsplit(as.character(tokens),"[:]"))
	for(valIndx in seq(2, length(tokens), by=2))
	{
		if(tokens[valIndx] == "888")
		{
			missingVal = as.numeric(tokens[valIndx-1])
			inpData[,varName][inpData[,varName]==missingVal] <- MISSING_VAL
			break
		}
	}
}

#Calculate the total instances in the data cube
totalInstances <- sum(inpData[,columns])
#Calculate the instances with missing values
instancesWithMissing <- sum(inpData[!complete.cases(inpData),columns])

#The number and the name of the target variable that user choοses
targetVar = columns-1;
targetVarName = varNames[targetVar];

#Check the dimensions of the data cube
if(targetVar <= 1)
{
	errMessage = " The data cube must have at least 2 variables.\n Process terminated. ";
	stop(errMessage);
}

targetValuesUnique<-unique(inpData[,targetVar])

#Check how many possible values has the targetVar
#If it has 2 values->change them to 0 and 1
if(length(targetValuesUnique) == 2) {
	#if one of the values is MISSING_VAL terminate
	if(sum(is.na(targetValuesUnique))>0){
		errMessage = "The target variable is not binary";
		stop(errMessage);
	#If it is a missing value, delete the rows with MISSING value
	}
	if(min(targetValuesUnique) != 0 && max(targetValuesUnique) != 1) {
		for(i in 1:rows) {
			if(inpData[i,targetVar] == min(targetValuesUnique))
				inpData[i,targetVar]<-0
			else
				inpData[i,targetVar]<-1
		}
	}
#If it has 3 values
} else if (length(targetValuesUnique) == 3) {	
	#Check if the third value is for MISSING or not
	if(sum(is.na(targetValuesUnique))==0){
		errMessage = "The target variable is not binary";
		stop(errMessage);
	#If it is a missing value, delete the rows with MISSING value
	} else {
		
		inpData <- inpData[!is.na(inpData[,targetVar]),]
		#Change the other 2 values in 0 and 1 
		rows <- nrow(inpData);
		targetValuesUnique<-na.omit(targetValuesUnique)
		for(i in 1:rows) {
			if(inpData[i,targetVar] == min(targetValuesUnique)) {
				inpData[i,targetVar]<-0
			} else {
				inpData[i,targetVar]<-1
			}
		}
	}
#If it has more than 3 values
} else {
	errMessage = "The target variable should only have two possible values (and an extra one for missing values)";
	stop(errMessage);
}

#Check if the inpData has values (after the deletions)
if(nrow(inpData) == 0) {
	errMessage = "Removal of instances with missing values, resulted in an empty data cube";
	stop(errMessage);
}

#Check if there are two possible values for the target variable (after the deletions)
targetValues<-unique(inpData[,targetVar])
if (length(targetValues) != 2){
	errMessage = "Removal of instances with missing values, resulted in a single class target variable";
	stop(errMessage);
}

#Create the formula string that will be tested in mlogit (targetVariable ~ variable1 + ...)
formulaStr <- paste(varNames[targetVar],"~.")


#Create the dataset
totalRows <- 0
rows <- nrow(inpData);
for(i in 1:rows)
	totalRows <- totalRows + inpData[i,columns]

dataset <- matrix(nrow=totalRows,ncol=columns-1)

#Put the column names
colnames(dataset) <- c(varNames[1:(columns-1)])

i<-1
inpCount <- 1
while(i <= totalRows)
{
	repeatNum <- inpData[inpCount,columns]
	
	for(j in 1:repeatNum)
	{	
		for(k in 1:(columns-1))
			dataset[i,k] <- inpData[inpCount,k]
		i<-i+1
	}
	inpCount <- inpCount + 1
}


#Deletion or not of missing values from the predictor variables
if (removeMissing == 1) {
	dataset <- na.omit(dataset)
} else if (any(is.na(dataset))){
	for(i in 1:(targetVar-1))
	{
		varData = dataset[,varNames[i]]
		varData[is.na(varData)] = modeFun(varData)
		dataset[,varNames[i]] <- varData
	}
}

#Calculate the number of remaining instances
remainingInstances <- nrow(dataset)

dataset <- as.data.frame(dataset)

#Binomial Logistic Regression
#Call the glm (Also control the number of iterations of glm)
Result <- glm(formulaStr, data=dataset, family = binomial, maxit = Iterations)

#Get and save in a text file the pValues from the coefficients of the result of binomail Logistic Regression
pValues<-summary(Result)$coefficients[,4];
varNames<-rownames(summary(Result)$coefficients);

#Also get the odds ratios
odds_ratios = exp(coef(Result))

numOfVars<-length(pValues);
outputString <- "Associations\n";
varMeaningsString <- "";

if(numOfVars > 1){

	#Output the XML contents describing the algorithm and the association	
	xmlString <- "<log>\n<SingleHypothesisTesting algorithm_name=\"Binomial Logistic Regression\" algorithmParamsSetting=\"";
	xmlString <- paste(xmlString,"remove_missing=",sep = "");
	xmlString <- paste(xmlString,removeMissing,sep = "");
	xmlString <- paste(xmlString,"num_iterations=",sep = " ");
	xmlString <- paste(xmlString,Iterations,sep = "");
	xmlString <- paste(xmlString,"\"/>",sep = "");
	
	for(i in 2:numOfVars)
	{
		xmlString <- paste(xmlString,"<Association Description=\"",sep = "\n");
		xmlString <- paste(xmlString,varNames[i],sep = "");
		xmlString <- paste(xmlString,targetVarName,sep = " =&gt; ");
		xmlString <- paste(xmlString,"\" p_value=\"",sep = "");
		xmlString <- paste(xmlString,pValues[i],sep = "");
		xmlString <- paste(xmlString,"\" odds_ratio=\"",sep = "");
		xmlString <- paste(xmlString,odds_ratios[[i]],sep = "");
		xmlString <- paste(xmlString,"\"/>",sep = "");
		outputString <- paste(outputString,varNames[i],sep = "");
		outputString <- paste(outputString,targetVarName,sep = " => ");
		outputString <- paste(outputString,"    pValue = ",sep = "");
		outputString <- paste(outputString,pValues[i],sep = "");
		outputString <- paste(outputString,"    odds_ratio = ",sep = "");
		outputString <- paste(outputString,odds_ratios[[i]],sep = "");
		outputString <- paste(outputString,"\n",sep = "");
    
    varMeaningsString <- paste(varMeaningsString,varMeanings[which(varMeaningsVarNames==varNames[i])],sep="\n");
    
	}
	xmlString <- paste(xmlString,"</log>",sep = "\n");
  
  
	varMeaningsString <- paste(varMeaningsString,varMeanings[which(varMeaningsVarNames==targetVarName)],sep="\n");
	varMeaningsString <- gsub(",",", ",varMeaningsString)
	varMeaningsString <- gsub(":","=",varMeaningsString)
  if(removeMissing == 1){
    varMeaningsString <- gsub("=888","=Not used",varMeaningsString)
  }else
  {
    varMeaningsString <- gsub("=888","=Missing Value",varMeaningsString)
  }
	varMeaningsString <- gsub("=777","=Not Applicable",varMeaningsString)
	
	outputString <- paste(outputString,"\nMeaning of variable values as used in the analysis",varMeaningsString,sep="")
  
	outputString <- paste(outputString,"\n\nTotal Instances = ",totalInstances,"\nInstances with missing values = ",instancesWithMissing,"\nNumber of instances used in the analysis = ",remainingInstances,sep="")
	write(outputString,commandArgs()[7]);
	write(xmlString,commandArgs()[8]);	
	
}else{
	outputString <- "Could not calculate the p-values."
	write(outputString,commandArgs()[7]);
}



