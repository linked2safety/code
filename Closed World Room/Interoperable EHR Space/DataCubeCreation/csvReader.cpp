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

#include "csvReader.h"
#include <string>
#include <sstream>

#ifdef _DEBUG
#include <iostream>
#endif


void csvReader::readData(std::ifstream &inputfile)
{
	std::string line;

	int line_count = 0;
	while (inputfile) {
		_file_data.push_back(std::vector<double>());
		std::getline(inputfile, line);
		std::istringstream iss(line);
		int var_count = 0;
		double varVal;
		while (iss >> varVal) {
			if (_vars[var_count]) {
				_file_data[line_count].push_back(varVal);
			}
			var_count++;
		}
		line_count++;
	}
	return;
}

void csvReader::discretizeData()
{
	for (unsigned int i = 0; i < _file_data.size(); i++) {
		for (unsigned int j=0; j < _file_data[i].size(); j++) {
			_file_data[i][j] = roundTo(_file_data[i][j], _thresholds[j]);
		}
	}
	return;
}

void csvReader::indexData()
{
	
	std::vector<std::vector<double>> columns;
	columns.resize(_numVars);

	for (unsigned int i = 0; i < _numVars; i++) {			//num of columns
		for (unsigned j = 0; j < _file_data.size()-1; j++) {  //num of rows
			columns[i].push_back(_file_data[j][i]);
		}
	}

	_valuesMap.resize(_numVars);
	for (unsigned int i = 0; i < columns.size(); i++) {
		int curr_count = 0;
		for (unsigned int j = 0; j < columns[i].size(); j++) {
			if (_valuesMap[i].empty()) {
				_valuesMap[i][columns[i][j]] = curr_count;
				curr_count++;
			}
			if ( !(_valuesMap[i].count(columns[i][j])) ) {
				_valuesMap[i][columns[i][j]] = curr_count;
				curr_count++;
			} else {
			}
		}
	}
	return;
}

void csvReader::createCube()
{
	//iterate through data:
	for (unsigned int i = 0; i < _file_data.size(); i++) {
		int index = 0;
		for (unsigned int j = 0; j < _file_data[i].size(); j++) {
			int weight = (int) _valuesMap[j][_file_data[i][j]];
			//index += (int) 10^j * ( _valuesMap[j][_file_data[i][j]] );
			index += (10^j)*weight;
		}
		_dataCube[index]++;
	}
#ifdef _DEBUG
	std::map<int,int>::iterator iter;
	for (iter = _dataCube.begin(); iter != _dataCube.end(); iter++) {
		_totalCount += iter->second;
	}
#endif
}

void csvReader::readHeaders(std::ifstream &inputfile)
{
	std::string headerLine;
	std::getline(inputfile, headerLine);

	std::istringstream iss(headerLine);

	int headcount = 0;
	std::string varStr;
	while (iss >> varStr) {
		_headers[varStr] = headcount;
		headcount++;
	}
	return;
}

void csvReader::setVars(std::vector<int> vars)
{
	for (unsigned int j=0; j < _headers.size(); j++) {
		_vars.push_back(false);
	}

	for (unsigned int i = 0; i < vars.size(); i++) {
		_vars[vars[i]] = true;
		_numVars++;
	}

	return;
}
 
double csvReader::roundTo(double numToRound, double step)
{

	if (step == 0)
		return numToRound;

	//Calc the floor value of numToRound
	double floor = ((long) (numToRound / step)) * step;

	//round up if more than half way of step
	double round = floor;
	double remainder = numToRound - floor;
	if (remainder >= step / 2)
		round += step;

	return round;

}


#ifdef _DEBUG
void csvReader::printVars()
{
	for (unsigned int i=0; i < _vars.size(); i++) {
		std::cout << _vars[i] << "\n";
	}
}

void csvReader::printHeaders()
{
	std::map<std::string, int>::iterator mapIter;
	for (mapIter = _headers.begin(); mapIter != _headers.end(); mapIter++) {
		std::cout << "Header: " << mapIter->first.c_str() << " Index: " << mapIter->second << "\n";
	}
}

void csvReader::printData()
{
	for (unsigned int i=0; i < _file_data.size(); i++) {
		std::cout << "-------Line Break----------";
		for (unsigned int j=0; j < _file_data[i].size(); j++) {
			std::cout << _file_data[i][j] << " ";
		}
		std::cout << "\n";
	}
}


#endif