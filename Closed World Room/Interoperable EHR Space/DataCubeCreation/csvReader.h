/*****************************************************************************
   Copyright 2013 Hasapis Panagiotis INTRASOFT

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*******************************************************************************/

#ifndef _CSV_READER_H_
#define _CSV_READER_H_

#include <vector>
#include <map>
#include <fstream>


class csvReader
{
private:
	//contains the file in tabular form. Headers not included
	std::vector<std::vector<double> > _file_data; 
	//for each variable, its column position
	std::map<std::string, int> _headers;
	// for each variable, indexed by its pos, its threshold to round up.
	std::vector<double> _thresholds;	
	//the variables from the file that are used for the DataCube creation.
	//_vars[i] == true means that variable in column i is used in the cube
	std::vector<bool> _vars;
	//Each element of the vector contains a map that contains indexes for each discrete value of the attribute
	//For example _valuesMap[2][38] == 1 means that the 3rd variable value of 38 has and index of 1.
	std::vector<std::map<double, int> > _valuesMap;			 
	//Key: the combined index formed by indexes of _valuesMap. Value: the count
	std::map<int, int> _dataCube;
	//helper function
	double roundTo(double numToRound, double step);
	//the number of variables used for dataCubeCreation
	unsigned int _numVars;
#ifdef _DEBUG
	unsigned int _totalCount;
#endif
public:
	csvReader() : _numVars(0) 
	{
#ifdef _DEBUG
		_totalCount = 0;
#endif
	};
	//reads the headers to _headers.
	void readHeaders(std::ifstream &inputfile);
	//reads the data to _file_data
	void readData(std::ifstream &inputfile);  
	//for each variables, sets the step of the descretization
	void setThresholds(std::vector<double> thresholds) {_thresholds = thresholds;}
	//sets the valid variables
	void setVars (std::vector<int> vars);
	//Discretizes the data according to  _thresholds 
	void discretizeData();
	//Fills the _valueMap
	void indexData();
	//Filles _dataCube
	void createCube();
#ifdef _DEBUG
	void printVars();
	void printHeaders();
	void printData();
	unsigned int getCount() {return _totalCount;}
#endif
};

#endif