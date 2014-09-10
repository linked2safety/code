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

#include "rdfxmlReader.h"
#include "rapidXml/rapidxml.hpp"
#include <string>
#include <list>
#include <boost/foreach.hpp>
#include <boost/tokenizer.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/iter_find.hpp>
#include <map>
#include <sstream>
#include <fstream>
#include <ctime> 
#include <algorithm>
#include "Classifier.h"


//#ifdef _DEBUG
#include <iostream>
//#endif

#define _ATHOS_ADHOC_

unsigned int rdfxmlReader::_maxSize = 0;

void rdfxmlReader::readDataFile()
{
	using namespace rapidxml;
	xml_document<> doc;    // character type defaults to char
	doc.parse<0>(&_inputXml[0]);    // 0 means default parse flags

	//std::cerr << "Entered reading...\n";

#ifdef _DEBUG_
	std::cout << "Name of my first node is: " << doc.first_node()->name() << "\n";
	xml_node<> *node = doc.first_node("rdf:RDF");
	for (xml_attribute<> *attr = node->first_attribute(); attr; attr = attr->next_attribute())
	{
		std::cout << "Node foobar has attribute " << attr->name() << " ";
		std::cout << "with value " << attr->value() << "\n";
	}
#endif
	xml_node<> *headNode = doc.first_node("rdf:RDF");
//std::cerr << "Entered\n";


	//std::cout << "First Node: " << childNode->name() << "\n";

	for (xml_node<> *child = headNode->first_node(); child != NULL; child = child->next_sibling()) {
		if ( child->first_attribute()->value() == std::string("&map;RecordDescription") ) {
			continue;
		}
		std::map<std::string, std::string> patientData;

#ifdef _DEBUG_
		std::cout << "Sibling Node: " << child->name() << "\n";
#endif
//std::cerr << "lo!\n";
		//get values:
		bool isRecordComplete = true;
		for (xml_node<> *values = child->first_node(); values; values = values->next_sibling()) {
			std::string varStr = std::string(values->name());
//std::cerr << "lo!\n";
//std::cerr << varStr << "\n";
			//if (varStr == std::string("clin:internal_no")) {
			//	continue;
			//}
#ifdef _ATHOS_ADHOC_
			std::stringstream varStream = std::stringstream(values->value());
			double varStreamInt = 0.0;
			varStream >> varStreamInt;
			/*if (varStreamInt != -9) {
				varStreamInt++;
			} else {
				varStreamInt = 0;
			}*/
			varStream = std::stringstream(std::string());
			varStream << varStreamInt;
			std::string valueStr = varStream.str();
#else
			std::string valueStr = std::string(values->value());
#endif
			if (valueStr.empty()) {
				isRecordComplete = false;
				break;
			}
			patientData[varStr] = valueStr;
			_headers.insert(values->name());
#ifdef _DEBUG_
		std::cout << "\tValues Node: " << values->name() << " Value: " << values->value() << "\n";
#endif
		}
		if (isRecordComplete) {
			_data_file.push_back(patientData);
		}

	//	xml_node<> *DescriptionNode = headNode->
	}
}

void rdfxmlReader::dropNonExistentData()
{
	//find max size of maps
	for (unsigned int i=0; i < _data_file.size(); i++) {
		if (_data_file[i].size() > rdfxmlReader::_maxSize) {
			rdfxmlReader::_maxSize = _data_file[i].size();
		}
	}

	_data_file.erase( std::remove_if(_data_file.begin(), _data_file.end(), rdfxmlReader::isDataMissing), _data_file.end() );

	return;
}

void rdfxmlReader::createCube()
{

	for (unsigned int i=0; i<_data_file.size(); i++) {
		std::map<std::string, std::string>::iterator mapIter;
		std::string indexStr;
		for (mapIter = _data_file[i].begin(); mapIter != _data_file[i].end(); mapIter++) {
			if (! (mapIter->first.compare(std::string("clin:internal_no"))) ) {
				continue;
			}
			if (indexStr.empty()) {
				std::stringstream iss;
				iss << _classifier.getIndexforVarInRange(mapIter->first, mapIter->second);
				if (  _classifier.getIndexforVarInRange(mapIter->first, mapIter->second) ) {
					indexStr = iss.str();
				} else {
					indexStr = mapIter->second;
				}
			} else {
				std::stringstream iss;
				iss << _classifier.getIndexforVarInRange(mapIter->first, mapIter->second);
				if (  _classifier.getIndexforVarInRange(mapIter->first, mapIter->second) ) {
					indexStr += std::string( ("_SEP_" + iss.str() ) );
				} else {
					indexStr += "_SEP_" + mapIter->second;
				}
			}
		}
		if (_dataCube[indexStr].empty()) {
			_dataCube[indexStr] = "1";
		} else {
			int count;
			std::istringstream buffer(_dataCube[indexStr]);
			buffer >> count;
			count++;
			std::stringstream countStr;
			countStr << count;
			_dataCube[indexStr] = countStr.str();

		}
#ifdef _DEBUG_
			std::cout << "IndexStr: " << indexStr << "\n";
#endif
	}

	//remove small values
	/*std::map<std::string, std::string>::iterator _dataCubeIterator;
	for (_dataCubeIterator = _dataCube.begin(); _dataCubeIterator != _dataCube.end();) {
		if ( (atoi(_dataCubeIterator->second.c_str()) < 5) ) {
			_dataCube.erase(_dataCubeIterator++);
		} else {
			_dataCubeIterator++;
		}
	}*/

	return;
}

void rdfxmlReader::createCubeClinical()
{

	for (unsigned int i=0; i<_data_file.size(); i++) {
		std::map<std::string, std::string>::iterator mapIter;

		std::string internalNubmerStr = std::string("DEF");
		//find internal number for each row
		for (mapIter = _data_file[i].begin(); mapIter != _data_file[i].end(); mapIter++) {
			if (! (mapIter->first.compare(std::string("clin:internal_no"))) ) {
				internalNubmerStr = mapIter->second;
			}
		}

		std::string indexStr;
		for (mapIter = _data_file[i].begin(); mapIter != _data_file[i].end(); mapIter++) {
			if (! (mapIter->first.compare(std::string("clin:internal_no"))) ) {
				continue;
			}
			if (indexStr.empty()) {
				std::stringstream iss;
				iss << _classifier.getIndexforVarInRange(mapIter->first, mapIter->second);
				if (  _classifier.getIndexforVarInRange(mapIter->first, mapIter->second) ) {
					indexStr = iss.str();
				} else {
					indexStr = mapIter->second;
				}
			} else {
				std::stringstream iss;
				iss << _classifier.getIndexforVarInRange(mapIter->first, mapIter->second);
				if (  _classifier.getIndexforVarInRange(mapIter->first, mapIter->second) ) {
					indexStr += std::string( ("_SEP_" + iss.str() ) );
				} else {
					indexStr += "_SEP_" + mapIter->second;
				}
			}
		}
		if ( (_dataCubesClinical[internalNubmerStr][indexStr]).empty()) {
			_dataCubesClinical[internalNubmerStr][indexStr] = "1";
		} else {
			int count;
			std::istringstream buffer(_dataCubesClinical[internalNubmerStr][indexStr]);
			buffer >> count;
			count++;
			std::stringstream countStr;
			countStr << count;
			_dataCubesClinical[internalNubmerStr][indexStr] = countStr.str();

		}
#ifdef _DEBUG_
			std::cout << "IndexStr: " << indexStr << "\n";
#endif
	}
	return;
}

void rdfxmlReader::dumpCsv(std::ofstream &output_fs)
{
	std::stringstream fileNameStream;
	srand((unsigned)time(0)); 
	fileNameStream << "Cube" << rand() << ".csv";
	//std::ofstream ofs = std::ofstream(fileNameStream.str());
	std::ofstream &ofs = output_fs;
	
	std::set<std::string>::iterator headIter;
	for (headIter = _headers.begin(); headIter != _headers.end(); headIter++) {
		std::list<std::string> stringList;
		boost::iter_split(stringList, *headIter, boost::first_finder(":"));

		//should be one
		int boost_count = 0;
		BOOST_FOREACH(std::string token, stringList)
		{
			if (! (token.empty()) && boost_count == 1) {
				ofs << token << ',';
			}
			boost_count = 1;
		}

	}
	ofs << "number\n";

	std::map<std::string, std::string>::iterator mapIter;
	for (mapIter = _dataCube.begin(); mapIter != _dataCube.end(); mapIter++) {

		if (mapIter->second.empty() ) {
			continue;
		}

		std::list<std::string> stringList;
		boost::iter_split(stringList, mapIter->first, boost::first_finder("_SEP_"));

		BOOST_FOREACH(std::string token, stringList)
		{   
#ifdef _ATHOS_ADHOC_
			if ( ! (token.compare(std::string("-9"))) ) {
				//token = std::string("0");
				ofs << 0 << ",";
			} else {
				ofs << token << ",";
			}
#else
			//ofs << hashCode(token) << ',';
			ofs << token << ",";
#endif
		}

		ofs << mapIter->second;
		ofs << "\n";
	}
	ofs.close();
}

void rdfxmlReader::dumpCsvClinical(char *fileName)
{
	std::stringstream fileNameStream;
	srand((unsigned)time(0)); 

	

	std::map<std::string, std::map<std::string, std::string > >::iterator CubesClinicalIter;
	for (CubesClinicalIter = _dataCubesClinical.begin(); CubesClinicalIter != _dataCubesClinical.end(); CubesClinicalIter++) {
		fileNameStream << std::string(fileName).substr(8,32) << rand() << "N" << CubesClinicalIter->first << ".csv";
		std::ofstream ofs = std::ofstream(fileNameStream.str());
		std::set<std::string>::iterator headIter;
		for (headIter = _headers.begin(); headIter != _headers.end(); headIter++) {
			std::list<std::string> stringList;
			boost::iter_split(stringList, *headIter, boost::first_finder(":"));

			//should be one
			BOOST_FOREACH(std::string token, stringList)
			{
				if (! (token.empty()) || ! (token.compare(std::string("clin:internal_no")) ) ) {
					ofs << token << ',';
				}
			}

		}
		ofs << "number\n";

		std::map<std::string, std::string>::iterator mapIter;
		for (mapIter = _dataCubesClinical[CubesClinicalIter->first].begin(); mapIter != _dataCubesClinical[CubesClinicalIter->first].end(); mapIter++) {

			if (mapIter->second.empty() ) {
				continue;
			}

			std::list<std::string> stringList;
			boost::iter_split(stringList, mapIter->first, boost::first_finder("_SEP_"));

			BOOST_FOREACH(std::string token, stringList)
			{   
	#ifdef _ATHOS_ADHOC_
				if ( ! (token.compare(std::string("-9"))) ) {
					//token = std::string("0");
					ofs << 0 << ",";
				} else {
					ofs << token << ",";
				}
	#else
				//ofs << hashCode(token) << ',';
				ofs << token << ",";
	#endif
			}

			ofs << mapIter->second;
			ofs << "\n";
		}
		std::cout << "Dumped Csv No: " << CubesClinicalIter->first << "\n";
		ofs.close();
		fileNameStream.str(std::string());
	}
}

std::string rdfxmlReader::hashCode(std::string strToHash)
{
        int hashcode=0;
        int MOD=10007;
        int shift=29;
        for(int unsigned i=0;i<strToHash.length();i++){
            hashcode = ( (shift*hashcode)%MOD + strToHash.at(i))%MOD;
        }
		std::stringstream hashStr;
		hashStr << hashcode;
		return hashStr.str();
}

void rdfxmlReader::perturbeData()
{
	std::map<std::string, std::string>::iterator cubeIter;

	for  (cubeIter = _dataCube.begin(); cubeIter != _dataCube.end();) {
		std::string value = cubeIter->second;
		unsigned int valueInt;
		std::stringstream valueStream = std::stringstream(value);
		valueStream >> valueInt;
		valueInt += rand()%3 - 1;
		valueStream = std::stringstream();
		valueStream << valueInt;
		if (valueInt < 3) {
			std::map<std::string, std::string>::iterator toErase = cubeIter;
			_dataCube.erase(cubeIter++);
			//cubeIter++;
		} else {
			//_dataCube[cubeIter->first] = valueStream.str();
			cubeIter->second = valueStream.str();
			++cubeIter;
		}
	}
	return;
}

bool rdfxmlReader::isDataMissing(const std::map<std::string, std::string> _patLine)
{
	if (_patLine.size() != rdfxmlReader::_maxSize) {
		return true;
	} else {
		return false;
	}
}

void rdfxmlReader::dumpVars(char *fileName)
{
	std::stringstream fileNameStream;
	fileNameStream << "Maps" << std::string(fileName).substr(8,32) << ".txt";
	std::ofstream ofs = std::ofstream(fileNameStream.str());

	std::map<std::string, std::vector<std::pair<int, std::pair<std::string, std::string> > > > valueRanges = _classifier.getValueRanges();
	std::map<std::string, std::vector<std::pair<int, std::pair<std::string, std::string> > > >::iterator valueRangesIter;

	for (valueRangesIter = valueRanges.begin(); valueRangesIter != valueRanges.end(); valueRangesIter++) {
		ofs << "Variable: " << valueRangesIter->first << "\n";
		for (unsigned int i = 0; i < valueRanges[valueRangesIter->first].size(); i++) {
			ofs << "\t" << "Index: " << valueRanges[valueRangesIter->first][i].first;
			ofs << "\t" << "Range: " << valueRanges[valueRangesIter->first][i].second.first << " - " << valueRanges[valueRangesIter->first][i].second.second;
			ofs << "\n";
		}
	}

	ofs.close();
	return;
}