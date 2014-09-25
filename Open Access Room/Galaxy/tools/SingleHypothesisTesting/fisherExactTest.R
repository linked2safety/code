 # # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
 #
 #   fisherExactTest.R - Calculates the p value of the association tested using Fisher's exact test
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


#The value for missing data
MISSING_VAL = NA


#LOAD THE DATA and get the dimensions
inpData <- read.table(commandArgs()[4], header=T, sep=",");
rows <- nrow(inpData);
columns <- ncol(inpData);
varDims <- c(1:(columns-1));

targetVar = columns-1;

#Check the dimensions of the data cube
if(targetVar <= 1)
{
	errMessage = " The data cube must have at least 2 variables.\n Process terminated. ";
	stop(errMessage);
}

#Convert missing values to MISSING_VAL
con <- file(commandArgs()[7], "r")
varMeanings<-readLines(con)
close(con)
for(varIndx in 1:(columns-1))
{
	tokens = unlist(strsplit(varMeanings[varIndx], "[|]"))
	varName = tokens[1];
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

#Remove missing data
inpData <- na.omit(inpData)
rows <- nrow(inpData);

#Calculate the remaining instances
remainingInstances <- sum(inpData[,columns])



#find the distinct categorical values of the variables and convert them to 1,2,3...n
for(colNum in 1:targetVar)
{
	varValues = (unique(inpData[colNum]));
	varDims[colNum] = nrow(varValues);
	for(rowNum in 1:rows)
	{
		dimNum = 1;
		while(inpData[rowNum,colNum] != varValues[dimNum,1])
		{
			dimNum = dimNum + 1;
		}
		inpData[rowNum,colNum] = dimNum;
	}
}

totalRows = 1;
for(i in 1:targetVar)
{
	totalRows = totalRows * varDims[i];
}

#create FULL DATA CUBE
curDim <- varDims;
for(i in 1:targetVar)
{
	curDim[i] = 1;
}
cube <-matrix(1:(totalRows*columns),totalRows);
maxItForValue = totalRows;
for(colNum in 1:targetVar)
{	
	maxItForValue = maxItForValue/varDims[colNum];
	curValIt = 1;
	for(rowNum in 1:totalRows)
	{
		cube[rowNum,colNum] = curDim[colNum];
		if(curValIt == maxItForValue)
		{
			curValIt = 0;
			if(curDim[colNum] == varDims[colNum])
			{
				curDim[colNum] = 0;
			}
			curDim[colNum] = curDim[colNum] + 1;
		}
		curValIt = curValIt+1;
	}	
}
for(rowNum in 1:totalRows)
{
	cube[rowNum,columns] = 0;
}
#copy the counts from the initial cube
targetVarDim = varDims[targetVar];
for(i in 1:targetVar)
{
	varDims[i] = 1;
	for(j in (i+1):targetVar)
	{
		varDims[i] = varDims[i]*varDims[j];
	}
}
for(i in 1:rows)
{
	indexRow = 0;
	for(j in 1:(targetVar-1))
	{
		indexRow = indexRow + (inpData[i,j]-1)*varDims[j];
	}
	indexRow = indexRow + inpData[i,targetVar];
	cube[indexRow,columns] = inpData[i,columns];
}

#Create the CONTINGENCY TABLE
maxIteration = totalRows/targetVarDim;
contTable <- matrix(1:targetVarDim,nrow=targetVarDim)
for(i in 1:targetVarDim)
{
	contTable[i] = cube[i,columns]
}

tempContTable <- matrix(1:targetVarDim,nrow=targetVarDim)

for(i in 2:maxIteration) 
{
	for(j in 1:targetVarDim) 
	{
		tempContTable[j] = cube[(i-1)*targetVarDim+j,columns]
	}
	contTable <- cbind(contTable,tempContTable)
}

#Perform FISHER'S EXACT TEST
tableDim <- dim(contTable)
if(tableDim[1]==2 && tableDim[2]==2){
  result <- fisher.test(contTable,hybrid=FALSE,workspace=1e9);
}else{
  result <- fisher.test(contTable,hybrid=TRUE,workspace=1e9);
}


#Output the p.value in a text file
varNames <- colnames(inpData);
outputString <- "Association\n";
outputString <- paste(outputString,varNames[1],sep="");
xmlAssociation <- varNames[1];
if((targetVar -1) >= 2)
{
	for(i in 2:(targetVar-1))
	{
		outputString <- paste(outputString," & ",sep = "");
		xmlAssociation <- paste(xmlAssociation," &amp; ",sep = "");
		outputString <- paste(outputString,varNames[i],sep = "");
		xmlAssociation <- paste(xmlAssociation,varNames[i],sep = "");
	}
}
outputString <- paste(outputString," => ",sep = "");
xmlAssociation <- paste(xmlAssociation," =&gt; ",sep = "");
outputString <- paste(outputString,varNames[targetVar],sep = "");
xmlAssociation <- paste(xmlAssociation,varNames[targetVar],sep = "");
outputString <- paste(outputString,"    p.value = ",sep = "");
outputString <- paste(outputString,result$p.value,sep = "");
outputString <- paste(outputString,"\n\nTotal Instances = ",totalInstances,"\nInstances with missing values = ",instancesWithMissing,"\nNumber of instances used in the analysis = ",remainingInstances,sep="")

write(outputString,commandArgs()[5]);

#Output the XML contents describing the algorithm and the association	
xmlString <- "<log>\n<SingleHypothesisTesting algorithm_name=\"Fisher Exact Test\" algorithmParamsSetting=\"\"/>";

xmlString <- paste(xmlString,"<Association Description=\"",sep = "\n");
xmlString <- paste(xmlString,xmlAssociation,sep = "");
xmlString <- paste(xmlString,"\" p_value=\"",sep = "");
xmlString <- paste(xmlString,result$p.value,sep = "");
xmlString <- paste(xmlString,"\"/>",sep = "");
xmlString <- paste(xmlString,"</log>",sep = "\n"); 

write(xmlString,commandArgs()[6]);



