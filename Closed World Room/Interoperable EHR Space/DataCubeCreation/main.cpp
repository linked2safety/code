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

	This component links (bundles) to RapidXML library, which is available 
	under Boost Software License. You may download the source code and associated 
	license at: http://rapidxml.sourceforge.net/

	This component links (bundles) to Boost C++ libraries, which is available 
	under Boost Software License. You may download the source code and associated 
	license at: http://www.boost.org/

*******************************************************************************/

#include <fstream>

#include <iostream>
#include <sstream>
#include <regex>
#include "rapidXml\rapidxml.hpp"
#include "rdfxmlReader.h"
#include "Classifier.h"

#include "csvReader.h"

int main(int argc, char *argv[])
{
	std::ofstream error_log = std::ofstream("error.log");
	std::ofstream app_log = std::ofstream("app.log");
	std::ifstream ifs = std::ifstream(argv[1]);

	if (!ifs) {
		error_log << "Could not open input file: " << strerror(errno);
		exit(-1);
	}


	//std::ifstream ifs = std::ifstream("C:\\Users\\Jim\\Documents\\Visual Studio 2012\\Projects\\DataCubes\\Debug\\Data-2013-05-21 17-36-42.rdf");
	//std::ifstream ifs = std::ifstream("F:\\RDFs\\Study CI_1.rdf");
	//std::ifstream ifs = std::ifstream("F:\\RDFs\\Study SIV Small.rdf");
	//std::ifstream ifs = std::ifstream("F:\\RDFs\\Study CII Small.rdf");
	//using namespace rapidxml;
	
	//std::ifstream ifs = std::ifstream(argv[1]);
	std::string line;
	std::string input_xml;

//std::cerr << "about to get lines..\n";
	while(std::getline(ifs,line)) {
		std::regex e1 ( "(.*)(rdf:Description)(.*)");
		std::regex e2 ( "(.*)(xmlns:)(.*)");
		std::regex e3 ( "(.*)(version)(.*)");
		if ( !std::regex_match (line,e1) && !std::regex_match (line,e2) && !std::regex_match (line,e3) )  {
			std::regex e4 (" ");
			line = std::regex_replace(line, e4, "");
			//std::cerr << line << "\n";
		}
		//<http://hcls.deri.ie/l2s/chuv/1.0#:
		std::regex e5 ("(.*)(<http:)(.*)(#:)(.*)");
		if (std::regex_match(line, e5) ) {
			//std::cerr << "Found http regex\n";
			//std::cerr << line << "\n";
			//std::regex e6("NonMedraVariables:");
			std::tr1::regex e6 ("(http:)(.*)(#:)");
			std::regex e7 ("(</http:)(.*)(#:)");
			line = std::regex_replace(line, e7, "</NonMedraVariables:");
			line = std::tr1::regex_replace(line, e6, std::string("NonMedraVariables:"), std::tr1::regex_constants::format_first_only);
			
			//std::cerr << "Replaced line: \n";
			//std::cerr << line << "\n";
		}

		//std::cerr << line << "\n";
		input_xml += line;
	}
//std::cerr << "done getting lines\n";

	std::vector<char> xml_copy(input_xml.begin(), input_xml.end());

	xml_copy.push_back('\0');

	//Classifier cl = Classifier();
	//cl.setClinical(xml_copy);

	app_log << "Starting Data Cube component..\n";
	std::ofstream output_fs = std::ofstream(argv[2]);

	if (!output_fs) {
		error_log << "Could not open output file: " << strerror(errno);
		exit(-1);
	}

//std::cerr << "About to create reader\n";
	rdfxmlReader rr = rdfxmlReader(xml_copy);
//std::cerr << "Main: about to set classifier\n";
	rr.setClassifier(xml_copy);
//std::cerr << "Main:about to read file data\n";
	rr.readDataFile();
	rr.dropNonExistentData();
	if (rr.isClinical()) {
		//std::cout << "Found Internal Key\n";
		app_log << "Found Internal Key\n";
		rr.createCubeClinical();
	} else {
		//std::cout << "No Internal Key Found\n";
		app_log << "No Internal Key Found\n";
		rr.createCube();
	}
	rr.perturbeData();
	if (rr.isClinical()) {
		//std::cout << "Dumping Csvs...\n";
		app_log << "Dumping Csvs...\n";
		rr.dumpCsvClinical(argv[1]);
	} else {
		//std::cout << "Dumping Csv...\n";
		app_log << "Dumping Csvs...\n";
		rr.dumpCsv(output_fs);
	}
	
	//rr.createCube();
	//rr.dumpCsv();

	//rr.dumpVars(argv[1]);

	//get variables to be included by user
	/*std::vector<int> vars;
	int var;

	std::cout << "Enter 3 variables to create DataCube\n";
	std::cout << "Var 1: ";
	std::cin >> var;
	vars.push_back(var);
	std::cout << "Var 2: ";
	std::cin >> var;
	vars.push_back(var);
	std::cout << "Var 3: ";
	std::cin >> var;
	vars.push_back(var);

	//Thresholds:
	std::vector<double> thress;
	double thres;

	std::cout << "Threshold for Var 1: ";
	std::cin >> thres;
	thress.push_back(thres);
	std::cout << "Threshold for Var 2: ";
	std::cin >> thres;
	thress.push_back(thres);
	std::cout << "Threshold for Var 3: ";
	std::cin >> thres;
	thress.push_back(thres);

	csvReader cr = csvReader();
	cr.readHeaders(ifs);
	cr.setVars(vars);
	cr.setThresholds(thress);
	cr.readData(ifs);
	cr.discretizeData();
	cr.indexData();
	cr.createCube();

	std::cout << cr.getCount();*/
	return 0;
}