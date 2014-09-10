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

#ifndef _CLASSIFIER_H_
#define _CLASSIFIER_H_

#include <vector>
#include <map>

class Classifier
{
private:
	std::map<std::string, std::vector<std::pair<int, std::pair<std::string, std::string> > > > _valueRanges;
	bool _isClinicalTrial;
	double stringToDouble(std::string doubleStr);
	std::vector<std::string> inline StringSplit(const std::string &source, const char *delimiter = " ", bool keepEmpty = false)
	{
		std::vector<std::string> results;

		size_t prev = 0;
		size_t next = 0;

		while ((next = source.find_first_of(delimiter, prev)) != std::string::npos)
		{
			if (keepEmpty || (next - prev != 0))
			{
				results.push_back(source.substr(prev, next - prev));
			}
			prev = next + 1;
		}

		if (prev < source.size())
		{
			results.push_back(source.substr(prev));
		}

		return results;
	}
public:
	int getIndexforVarInRange(std::string varName, std::string varValue);
	Classifier() {};
	Classifier(std::vector<char> inputXml);
	void setClinical(std::vector<char> inputXml);
	bool isClinical() {return _isClinicalTrial;}
	std::map<std::string, std::vector<std::pair<int, std::pair<std::string, std::string> > > > getValueRanges() {return _valueRanges;}
};

#endif