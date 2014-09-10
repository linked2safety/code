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

#ifndef _RDFXML_READER_H_
#define _RDFXML_READER_H_

#include <vector>
#include <map>
#include <string>
#include <set>
#include "Classifier.h"

class rdfxmlReader
{
private:
	std::vector<char> _inputXml;
	std::vector<std::map<std::string, std::string>> _data_file;
	std::map<std::string, std::string> _dataCube;
	std::map<std::string, std::map<std::string, std::string > > _dataCubesClinical;
	std::set<std::string> _headers;
	std::string hashCode(std::string strToHash);
	static bool isDataMissing(const std::map<std::string, std::string> _patLine);
	Classifier _classifier;
public:
	static unsigned int _maxSize;
	rdfxmlReader() { _maxSize = 0; };
	rdfxmlReader(std::vector<char> inputXml) {
		_inputXml.reserve(inputXml.size());
		_inputXml.swap(inputXml);
		_maxSize = 0;
	}
	void setClassifier(std::vector<char> inputXml) {
		_classifier = Classifier(inputXml);
	}
	bool isClinical() { return _classifier.isClinical(); }
	void readDataFile();
	void dropNonExistentData();
	void createCube();
	void createCubeClinical();
	void dumpCsv(std::ofstream &output_fs);
	void dumpCsvClinical(char *fileName);
	void perturbeData();
	void dumpVars(char *fileName);
};
#endif