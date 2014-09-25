 # # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
 #
 #   linkageDisequilibrium.R - Calculates the linkage disequilibrium between two SNPs
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
 #   You may download the source code and associated license at https://github.com/linked2safety/code
 #
 # # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 


nullFile <- file("/dev/null", "w"); 
sink(nullFile,type="message");
library("genetics");
sink(NULL, type="message")
close(nullFile) 

#Expected SNP values
MISSING_VAL = NA;
HOMOZYGOUS_1 = 1;
HOMOZYGOUS_2 = 3;
HETEROZYGOUS = 2;

#LOAD THE DATA and get the dimensions
inpData <- read.table(commandArgs()[4], header=T, sep=",");
rows <- nrow(inpData);
columns <- ncol(inpData);


#Convert missing values to MISSING_VAL
con <- file(commandArgs()[8], "r")
varMeanings<-readLines(con)
close(con)
varType <- inpData[1,-columns]
for(varIndx in 1:(columns-1))
{
	tokens = unlist(strsplit(varMeanings[varIndx], "[|]"))
	varName = tokens[1];
	varType[varName] <- tokens[3]
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


if(columns < 3)
{
	errMessage = " The data cube must have at least 2 variables.\n Process terminated. ";
	stop(errMessage);
}

ldMeasure = as.integer(commandArgs()[5]);
if(ldMeasure < 1 || ldMeasure>3)
{
	errMessage = " An undefined LD measure was selected ";
	stop(errMessage);
}

#GET THE INDEX OF THE FIRST TWO SNPs
snp1 = 0;
snp2 = 0;
for(varIndx in 1:(columns-1))
{
	if(varType[varIndx] == "SNP"){
		snp1<-varIndx;
		break;
	}
}
if(snp1 == 0){
	errMessage = " No SNPs were found in the dataset provided ";
	stop(errMessage);
}
if(snp1 == (columns-1)){
	errMessage = " There is only one SNP in the dataset provided ";
	stop(errMessage);
}

for(varIndx in (snp1+1):(columns-1))
{
	if(varType[varIndx] == "SNP"){
		snp2<-varIndx;
		break;
	}
}
if(snp2 == 0){
	errMessage = " There is only one SNP in the dataset provided ";
	stop(errMessage);
}

#Find the total number of genotypes
numOfGenotypes <- 0;
for(rowNum in 1:rows)
{
	numOfGenotypes <- numOfGenotypes + inpData[rowNum,columns];
}

genotypeVec <- rep("A/T",numOfGenotypes);

#print(genotypeVec1)

#create the genotypes for the two SNPs
#SNP1
curInstance = 1;
for(rowNum in 1:rows)
{
	instances <- inpData[rowNum,columns];
	
	for(i in 1:instances)
	{
		g <- inpData[rowNum,snp1];
		if(is.na(g))
		{
			genotypeVec[curInstance] <- "NA";
		}
		else if(g == HOMOZYGOUS_1)
		{
			genotypeVec[curInstance] <- "A/A";
		}
		else if(g == HOMOZYGOUS_2)
		{
			genotypeVec[curInstance] <- "T/T";
		}
		else if(g == HETEROZYGOUS)
		{
			genotypeVec[curInstance] <- "A/T";
		}
		curInstance = curInstance + 1;
	}
}
genotype1 <- genotype(genotypeVec);
#SNP2
curInstance = 1;
for(rowNum in 1:rows)
{
	instances <- inpData[rowNum,columns];
	
	for(i in 1:instances)
	{
		g <- inpData[rowNum,snp2];
		if(is.na(g))
		{
			genotypeVec[curInstance] <- "NA";
		}
		else if(g == HOMOZYGOUS_1)
		{
			genotypeVec[curInstance] <- "A/A";
		}
		else if(g == HOMOZYGOUS_2)
		{
			genotypeVec[curInstance] <- "T/T";
		}
		else if(g == HETEROZYGOUS)
		{
			genotypeVec[curInstance] <- "A/T";
		}
		curInstance = curInstance + 1;
	}
}
genotype2 <- genotype(genotypeVec);




#Calculate LD
result <- LD(genotype1,genotype2);


#Output the results in a file
varNames <- colnames(inpData);
outputString <- varNames[snp1];
outputString <- paste(outputString,varNames[snp2],sep = " => ");

if(ldMeasure == 1){
	outputString <- paste(outputString,result$D,sep = "    D = ");
}else if(ldMeasure == 2){
	outputString <- paste(outputString,result$"D'",sep = "    D' = ");
}else if(ldMeasure == 3){
	outputString <- paste(outputString,result$r,sep = "    r = ");
}

write(outputString,commandArgs()[6]);

#Output the XML contents describing the algorithm and the association	
xmlString <- "<log>\n<SingleHypothesisTesting algorithm_name=\"Linkage Disequilibrium\" algorithmParamsSetting=\"";
xmlString <- paste(xmlString,"LD_measure=",sep = "");
xmlString <- paste(xmlString,ldMeasure,sep = "");
xmlString <- paste(xmlString,"\"/>",sep = "");

xmlString <- paste(xmlString,"<Association Description=\"",sep = "\n");
xmlString <- paste(xmlString,varNames[snp1],sep = "");
xmlString <- paste(xmlString,varNames[snp2],sep = " =&gt; ");
xmlString <- paste(xmlString,"\" LD=\"",sep = "");
if(ldMeasure == 1){
	xmlString <- paste(xmlString,result$D,sep = "");
}else if(ldMeasure == 2){
	xmlString <- paste(xmlString,result$"D'",sep = "");
}else if(ldMeasure == 3){
	xmlString <- paste(xmlString,result$r,sep = "");
}
xmlString <- paste(xmlString,"\"/>",sep = "");
xmlString <- paste(xmlString,"</log>",sep = "\n"); 

write(xmlString,commandArgs()[7]);



