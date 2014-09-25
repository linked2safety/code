/*
 *   data.h - Loads the data cube in memory
 *   
 *   Copyright (C) 2014  -  University of Cyprus  -  aristodimou.aristos@ucy.ac.cy
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   You may download the source code and associated license at  https://github.com/linked2safety/code
 * 
*/

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <math.h>
#include <cstdlib>
#include <map>

using namespace std;

#define NOT_APPLICABLE_ORIG_VAL "777"
#define MISSING_ORIG_VAL "888"

//split a string based on the delimiter
vector<string> split(const char *str, char delimiter = ',')
{
    vector<string> result;

    while(1)
    {
        const char *begin = str;

        while(*str != delimiter && *str)
                str++;
        //convert to string and store it in the result vector
        result.push_back(string(begin, str));

        //Break when at the end of the string
        if(!*str || !*str++)
                break;
    }

    return result;
}

//===================================================
// D A T A C U B E   stores the data from the input files
//===================================================
class Datacube
{
public:
	vector < vector <int> > values; //contains the data cube values
	vector <string> variableNames; //contains the names of the variables
	map < string, map < int,string> > variableCatMeaning; //contains the meaning of each categorical value of the variables
	map <string,string> variableType; //The variable type SNP=SNP, MM=medical meauserement, AE=adverse event, D=drug 
	
	//Loads the data from the file to the memory
	void loadData(string filename)
	{
		string lineRead; //the value we want to get
		char charRead;
		bool firstLine = true;
		vector < int > input;
		vector < string > tokens;	
		
		//Open the file with the data
		fstream inputFile(filename.c_str(),ios::in); 	
		//Check if the file was successfully opened
		if (inputFile.fail())
		{
			cerr << "\n Error: " << filename << " could not be accessed.\n";	
			exit(-1);
		}
		
		inputFile.precision(9);
		
		//clear any whitespaces
		while(!isalnum(inputFile.peek()))
		{
			inputFile.get(charRead);
			if(inputFile.eof())
				break;
		}

		//Read until the end of file
		while(!inputFile.eof())
		{
			getline(inputFile,lineRead);
			
			//The first line contains the variable names
			if(firstLine)
			{
				firstLine = false;
				variableNames = split(lineRead.c_str());
			}
			else
			{
				tokens = split(lineRead.c_str());
                //Get the data cube values and convert them to numbers
				for(int i=0;i<tokens.size();++i)
					input.push_back(strtod(tokens.at(i).c_str(),NULL));			
				tokens.clear();
				//store the values read in the cube
				values.push_back(input);
				input.clear();
			
				//clear whitespaces
				while(!isalnum(inputFile.peek()))
				{
					inputFile.get(charRead);
					if(inputFile.eof())
						break;
				}
			}
		}	

		inputFile.close();
	}


	//Sets the meaning of each categorical value of the variables
	void setVariablesMeaning(string filename)
	{
		string lineRead; //the value we want to get
		char charRead;
		vector < string > tokens,tempCatValue;
		map <int,string> categories;	
		
		//Open the file with the data
		fstream inputFile(filename.c_str(),ios::in); 	
		//Check if the file was successfully opened
		if (inputFile.fail())
		{
			cerr << "\n Error: " << filename << " could not be accessed.\n";	
			exit(-1);
		}
		
		inputFile.precision(9);
		
		//clear any whitespaces
		while(!isalnum(inputFile.peek()))
		{
			inputFile.get(charRead);
			if(inputFile.eof())
				break;
		}

		//Read until the end of file
		while(!inputFile.eof())
		{
			getline(inputFile,lineRead);
			
			tokens = split(lineRead.c_str(),'|');
			string varName = tokens[0];
			variableType[varName]=tokens[2];
			tokens = split(tokens[1].c_str());
			//Get the data cube values and convert them to numbers
			for(int i=0;i<tokens.size();++i)
			{
				tempCatValue = split(tokens[i].c_str(),':');
				//Check if it is a digit. For SNPs additional info might 
				//be stored such as the chromosome and its position
				if(isdigit(tempCatValue[0][0])!=0)
					categories[atoi(tempCatValue[0].c_str())]= tempCatValue[1];
			}		
			variableCatMeaning[varName] = categories;
			tokens.clear();
			categories.clear();

			//clear whitespaces
			while(!isalnum(inputFile.peek()))
			{
				inputFile.get(charRead);
				if(inputFile.eof())
					break;
			}
		}	

		inputFile.close();			
		
	}


	//Get the meaning of a data cube value (input is the x,y pos of the value in the data cube)
	string getMeaningOfValue(unsigned int x, unsigned int y)
	{
		return variableCatMeaning[variableNames[y]][values[x][y]];		
	}
	
	
	//Returns the number of categorical values of a variable
	int getNumOfVariableValues(string variableName)
	{
		return variableCatMeaning[variableName].size();
	}
	
	//Returns the number of categorical values of a variable
	int getNumOfVariableValues(unsigned int variableIndx)
	{
		return variableCatMeaning[variableNames[variableIndx]].size();
	}
	
	//Returns the variable's type
	string getVariableType(int pos)
	{
		return variableType[variableNames[pos]];
	}

	//Returns the variable's type
	string getVariableType(string varName)
	{
		return variableType[varName];
	}
	
	//Write the cube to a file
	void writeToFile(string filename)
	{
		ofstream outp;
		outp.open(filename.c_str());
		//output the variable names
		for(int i=0; i<variableNames.size(); ++i)
			if(i==0)
				outp << variableNames.at(i);
			else
				outp << "," << variableNames.at(i);
		//output the numeric values of the cube
		for(int i=0; i<values.size(); ++i)
		{
			outp << endl;
			for(int j=0; j<values.at(i).size(); ++j)
				if(j==0)
					outp << values.at(i).at(j);
				else
					outp << "," << values.at(i).at(j);	
		}
		outp.close();		
	}


    //Remove data cube variables
    void removeVariables(bool keep[])
    {
    	vector <string> varNames;
    	vector < vector < int > > newValues;
    	vector <int> tempValues;
    		
    	//Remove the variable names of the SNPs that did not pass the test
    	for(int column=0; column<variableNames.size(); ++column)
    		if(keep[column])
    			varNames.push_back(variableNames.at(column));
    			
    	//No variable was removed		
    	if(varNames.size() == variableNames.size())		
    		return;
    			
   
    	//Exit if all variables will be removed
    	if(varNames.size() == 1)
    	{
    		cerr << "\nAll variables failed passing the test!" << endl << "Process Terminated." << endl;
    		exit(-1);
    	}
     	//Exit if only one variable will remain
    	if(varNames.size() == 2)
    	{
    		cerr << "\nOnly one variable will remain in the data cube!" << endl << "Process Terminated." << endl;
    		exit(-1);
    	}   	

     	//Remove the variables that did not pass the test
    	for(int row=0; row<values.size(); ++row)
    	{
    		for(int column=0; column < values.at(0).size(); ++column)
    		{
                //if the variable will be stored
    			if(keep[column])				
    				tempValues.push_back(values.at(row).at(column));
    		}
    		//store the values in the data cube
    		newValues.push_back(tempValues);
    		tempValues.clear();
    	}
    
    	//Merge any rows with the same variable values
    	if(variableNames.size() != varNames.size())
    	{
            //initialize vector
    		map <vector<int>,int> mergedDup;		
    		//the column with the counts
    		int countsCol = varNames.size()-1;
    
    		for(int row=0; row<newValues.size(); ++row)
    		{
				tempValues = newValues.at(row);
    			tempValues.pop_back();
    			//Merge duplicate rows and sum their counts
    			if(mergedDup.count(tempValues) == 0)
    				mergedDup[tempValues] = newValues.at(row).at(countsCol);
    			else
    				mergedDup[tempValues] = mergedDup[tempValues] + newValues.at(row).at(countsCol);
    		}
    		
    		map< vector <int> ,int > ::iterator mapIt;
    		vector <int> tempVec;
    		values.clear();
            //Store the new values of the data cube
    		for(mapIt = mergedDup.begin(); mapIt!=mergedDup.end(); ++mapIt)
    		{
                tempVec.clear();
                tempVec = mapIt->first;
                tempVec.push_back(mapIt->second);
    			values.push_back(tempVec);
    		}		
    		
        	variableNames = varNames;
        }
		
    
    }


	//Remove data cube instances
	void removeInstances(bool keep[])
	{
		vector < vector <int> > remainingInstances;
		//store the instances that will be kept in a temp vector
		for(int row=0;row<values.size();++row)
		{
			if(keep[row])
				remainingInstances.push_back(values.at(row));
		}
		//clear the previous instances
		values.clear();
		//store the instances that will remain in the data cube
		values = remainingInstances;
	}
	
	//Show all values of this class
	void showAll()
	{
        //Output the variable names
		for(int i=0; i<variableNames.size(); ++i)
			cout << "\t" << variableNames.at(i);
		//output the numeric values of the cube
		for(int i=0; i<values.size(); ++i)
		{
			cout << endl;
			for(int j=0; j<values.at(i).size(); ++j)
				cout << "\t" << values.at(i).at(j);	
		}
		cout << endl;
		
		//Output the variables meanings
		for(map < string, map < int,string> > :: iterator varIt=variableCatMeaning.begin();varIt!=variableCatMeaning.end();++varIt)
		{
			cout << endl << varIt->first << endl;
			for(map < int,string> :: iterator valIt = varIt->second.begin(); valIt!=varIt->second.end();++valIt)
				cout << valIt->first << " = " << valIt->second << endl;
		}		
		
	}
	
};

