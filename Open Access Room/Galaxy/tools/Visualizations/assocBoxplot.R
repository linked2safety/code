
# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   assocBoxplot.py - Creates boxplot of the metrics for the associations containing certain outcomes  
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

#Load the library
nullFile <- file("/dev/null", "w"); 
sink(nullFile,type="message");
library(RPostgreSQL)
library(hash)
sink(NULL, type="message")
close(nullFile) 

#1. Instantiates the driver object 
drv <- dbDriver("PostgreSQL")
#drv

DATABASE_NAME<-'galaxy_prod'
HOST<-'1.0.0.1'
USERNAME<-'pass'
PASSWORD<-'pass'

#2. Creates and opens a connection to the database implemented by the driver drv
con <- dbConnect(drv, dbname=DATABASE_NAME, host=HOST, user=USERNAME, password=PASSWORD)
#con

#3. Call select query
#Define the values
#These will be automatically set by the xml file
selected_var<-commandArgs()[5]
#remove any whitespaces
selected_var<-gsub("\\s","",selected_var)

if(commandArgs()[6] == "0"){
	metric<-"pValue"
}else{
	metric<-"oddsRatio"
}

sql1<-""
sql2<-""
#Call select query
if (metric == "oddsRatio") {
	sql1 <- "SELECT association,odds_ratio FROM association WHERE odds_ratio is not NULL and visible=TRUE"
	sql2 <- "SELECT association,odds_ratio FROM selfreportedassociation WHERE odds_ratio is not NULL and visible=TRUE"
} else{
	sql1 <- "SELECT association, p_value FROM association WHERE p_value is not NULL and visible=TRUE"
	sql2 <- "SELECT association, p_value FROM selfreportedassociation WHERE p_value is not NULL and visible=TRUE"
}
 
#4. Fetches the result set
result<-dbGetQuery(con, sql1)
result<-rbind(result,dbGetQuery(con, sql2))

#5. Finds in which associations (first column of result from query) the selected_var exists
assoc_with_selected<-grep(selected_var,result[,1])

if(length(assoc_with_selected) == 0) {
	errMessage = "No associations exist in the database for the selected variable";
	stop(errMessage);
}

#6. Get each Association the selected_var exists and finds the other associated variable
##Add the associations and their oddsRatios or pValues in hashTable (AssociationList)
#Init variables

j<-1
k<-1
flag<-0
associations<-c()
assocValues<-c()	
AssociationList<-hash()
used<-{}

#+++++  could replace result with assoc_with_selected
for(i in 1:length(result[,1]))
	used[i] <- 0

#Loop the Associations (first column of result)
for(i in 1:length(assoc_with_selected)){ 
	#It does not do the rest of the process if this association has already been processed before (for more efficiency)
	if(used[assoc_with_selected[i]] == 0){
		assocFound <- 0
		#split the association (based on symbol '=>')
		splitted_res<-unlist(strsplit(result[assoc_with_selected[i],1], "\\=>"))

		#For oddsRatio there is only one variable in the left side
		if(metric == "oddsRatio") {
			#Check if the right side of the association has the selected_var and save the left side
			if(splitted_res[1] == selected_var) {
				associations[k] <- splitted_res[2]
				assocFound <- 1
				k<-k+1
			#Check if the left side of the association has the selected_var and save the right side
			} else if(splitted_res[2] == selected_var) {
				associations[k] <- splitted_res[1]
				assocFound <- 1
				k<-k+1
			}
		#For the pValue: Check only if the right side of the association has the selected_var and save the left side
		}else if(metric == "pValue") {
			if(splitted_res[2] == selected_var) {
				associations[k] <- splitted_res[1]
				assocFound <- 1
				k<-k+1
			} #else if (flag == 1 && splitted_res[1] == selected_var) {
			#	flag<-0
			#	associations[k] <- splitted_res[2]
			#	assocFound <- 1
			#	k<-k+1
			#}
		} 
		
		if(assocFound == 1){
			assocValues<-c()
			#Get all the associations with the same association name
			y<-grep(result[assoc_with_selected[i],1],result[,1])
			#Get the oddsRatios or pValues of this association
			for(j in 1:length(y)) {
				if(metric == "oddsRatio") {
					assocValues[j]<-result[y[j],2]
				} else if(metric == "pValue") {
					assocValues[j]<-(-log10(result[y[j],2]))
				}

				used[y[j]] <- 1
			}
			
			#If only one value exists, add it twice for avoiding some issues with the boxplot function
			if(length(assocValues)==1)
				assocValues[2] <- assocValues[1]
			#Saves the association and its oddsRatios or pValues in a HashTable
			AssociationList[associations[k-1]]<-assocValues
		}
	}
}

if(length(keys(AssociationList)) == 0)
	stop("Could not find any associations for the specified variable");





#8. Create the boxplot (boxwex->column width)
pdf(file=commandArgs()[4]); #open pdf connection
par(mar = c(5, 10, 4, 2) + 0.1);
if(metric == "oddsRatio") {
	if(length(keys(AssociationList)) == 1){
		boxplot(values(AssociationList),range=0, main=paste(keys(AssociationList),"association with",selected_var), xlab= "oddsRatio", ylab="", boxwex = 0.25, col='grey', horizontal=T)
		mtext(keys(AssociationList), side = 2, line=4)
	}else{
		boxplot(values(AssociationList),range=0, main=paste("Factors associated with",selected_var), xlab= "oddsRatio", ylab="", boxwex = 0.25, col='grey', horizontal=T)
		mtext("Factors", side = 2, line=8)
	}
} else if(metric == "pValue") {
	if(length(keys(AssociationList)) == 1){
		boxplot(values(AssociationList),range=0, main=paste(keys(AssociationList),"association with",selected_var), xlab= "-log(pValue)", ylab="", boxwex = 0.25, col='grey', horizontal=T)
		mtext(keys(AssociationList), side = 2, line=4)
	}else{
		boxplot(values(AssociationList),range=0, main=paste("Factors associated with",selected_var), xlab= "-log(pValue)", ylab="", boxwex = 0.25, col='grey',las=1, horizontal=T)
		mtext("Factors", side = 2, line=8)
	}
}
#close pdf connection
res <- dev.off() 


#9. Close the connection 
res <- dbDisconnect(con);

#10. Frees all the resources on the driver 
res <- dbUnloadDriver(drv);

