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

#include "Classifier.h"
#include "rapidXml/rapidxml.hpp"
#include <string>
#include <sstream>
#include <algorithm>
#include <iostream>

#ifdef _DEBUG
#include <iostream>
#endif

Classifier::Classifier(std::vector<char> inputXml)
{
	setClinical(inputXml);
}

void Classifier::setClinical(std::vector<char> inputXml)
{
	using namespace rapidxml;
	xml_document<> doc;
//std::cerr << "setClinical: about to parse inputxml\n";
//for (unsigned int i = 0; i < inputXml.size(); i++) {
//	std::cerr << inputXml[i];
//}
	doc.parse<0>(&inputXml[0]);

//std::cerr << "Entered setClinical\n";
	xml_node<> *headNode = doc.first_node("rdf:RDF");
//std::cerr << "Entered head in setClinical\n";
	xml_node<> *recordDescriptionNode = headNode->first_node();
	std::string recordDescriptionNodeAttribute = recordDescriptionNode->first_attribute()->value();
	/*if (! (recordDescriptionNodeAttribute.compare("&map;RecordDescription")) ) {
		_isClinicalTrial = true;
#ifdef _DEBUG_
		std::cout << "Found clinical\n";
#endif
	} else {
		_isClinicalTrial = false;
	}*/
	_isClinicalTrial = false;
	//set Ranges
	for (xml_node<> *rangesNode = recordDescriptionNode->first_node("map:hasRange"); rangesNode; rangesNode = rangesNode->next_sibling("map:hasRange") ) {
		std::string varName = rangesNode->first_attribute()->value();
		char chars[] = "&";
		for (unsigned int i = 0; i < strlen(chars); ++i) {
			varName.erase( std::remove(varName.begin(), varName.end(), chars[i]), varName.end() );
		}
		std::replace( varName.begin(), varName.end(), ';', ':');
#ifdef _DEBUG_
		std::cout << "Var: " << varName << "has range: " << rangesNode->value() << "\n";
#endif
		//range pair
		std::vector<std::string> ranges = StringSplit(rangesNode->value(), ",", true);
		//get minmax ends;

		std::vector<std::pair<int, std::pair<std::string, std::string> > > rangesVector;
		std::pair<std::string, std::string> valuePair;
		for (unsigned int i = 0; i < ranges.size(); i++) {
			std::vector<std::string> ends = StringSplit(ranges[i], "-", true);
			
			if (ends[0].empty()) {
				std::pair<std::string, std::string> valuePair;
				valuePair.first = std::string("NULL");
				valuePair.second = ends[1];
			} else if (ends.size() == 1) {
				std::pair<std::string, std::string> valuePair;
				valuePair.first = ends[0];
				valuePair.second = std::string("NULL");
			} else {				
				valuePair.first = ends[0];
				valuePair.second = ends[1];
			}

			std::pair<int, std::pair<std::string, std::string>> pairIndex;
			pairIndex.first = i+1;
			pairIndex.second = valuePair;
			rangesVector.push_back(pairIndex);
		}
		_valueRanges[varName] = rangesVector;
#ifdef _DEBUG_
		for (unsigned int i = 0; i < ranges.size(); i++) {
			std::cout << "Range: ";
			if ( !(ranges[i].empty()) ) {
				std::cout << ranges[i];
			} else {
				std::cout << "NULL";
			}
					std::cout << "\n";
		}
#endif
#ifdef _DEBUG_
		std::cout << "Var: " << varName << "Index for 36.2: " << getIndexforVarInRange(varName, "39.4");
#endif
	}



	return;
}

int Classifier::getIndexforVarInRange(std::string varName, std::string varValue)
{
	std::vector<std::pair<int, std::pair<std::string, std::string> > > variable = _valueRanges[varName];
	

	double val = stringToDouble(varValue);
	for (unsigned int i = 0; i < variable.size(); i++) {
		double lowBound = stringToDouble(variable[i].second.first);
		double highBound = stringToDouble(variable[i].second.second);
		if ( (val > lowBound) && (val <= highBound)) {
			return variable[i].first;
		}
	}

	return 0;
}

double Classifier::stringToDouble(std::string doubleStr)
{
	std::stringstream doubleStream = std::stringstream(doubleStr);

	double val;
	doubleStream >> val;
	return val;
}