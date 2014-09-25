/*
 *   alleleFrequencyQC.cpp -  Calculates the allele frequencies of SNPs and removes
 *   any SNPs that have a minor allele frequency below the user defined threshold
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
 *   This component links (bundles) to data.h, which is available under GPLv3. 
 *   You may download the source code and associated license at  https://github.com/linked2safety/code
 * 
*/

#include "data.h"

//Constants (The mapper creates by default these values for SNPs)
#define MISSING_DATA 0
#define HOMOZYGOUS_1 1
#define HETEROZYGOUS 2
#define HOMOZYGOUS_2 3

int main (int argc, char *argv[]) {


	//ALLELE FREQUENCY QUALITY CONTROL
	//Get the data	
	Datacube datacube;
	datacube.loadData(argv[1]);
	datacube.setVariablesMeaning(argv[5]);


	//Set the threshold
	float threshold;
	if(argv[2]!=NULL)
		threshold = atof(argv[2]);
	else
		threshold = 0.05;
	bool keep[datacube.values.at(0).size()];
	for(int i=0; i<datacube.values.at(0).size(); ++i)
		keep[i] = true;
	int minorAlleles,h1count,h2count,total;
	//Check which variables pass the allele frequency QC
	int countsCol = datacube.values.at(0).size() - 1;
	//For each column of the cube
	for(int column=0;column<countsCol;column++) 
	{
		if(datacube.getVariableType(column).compare("SNP")==0)
		{
			minorAlleles = 0;
			h1count = 0;
			h2count = 0;
			total = 0;
			//for each instance of the cube
			for(int row=0;row<datacube.values.size();row++)
			{	
				//count the alleles
				switch(datacube.values.at(row).at(column))
				{
					//homozygous_1
					case HOMOZYGOUS_1:
						h1count += datacube.values.at(row).at(countsCol)*2;
						break;
					//heterozygous alleles
					case HETEROZYGOUS:
						h1count += datacube.values.at(row).at(countsCol);
						h2count += datacube.values.at(row).at(countsCol);
						break;		
					//homozygous_2
					case HOMOZYGOUS_2:
						h2count += datacube.values.at(row).at(countsCol)*2;
						break;			
				}
				//count the total alleles of the SNP
				if(datacube.values.at(row).at(column) != MISSING_DATA)
					total += datacube.values.at(row).at(countsCol)*2;
			}
			//Find the minor allele counts
			if(h1count < h2count)
				minorAlleles = h1count;
			else
				minorAlleles = h2count;
			
			//check if the minor allele frequency is acceptable
			if(total == 0)
				keep[column] = false;
			else if(double(minorAlleles)/double(total) < threshold)
				keep[column] = false;
		}
	}
	
	//Remove the variables that did not pass the allele frequency QC
    datacube.removeVariables(keep);
    //output the datacube if no variable was removed
	datacube.writeToFile(argv[3]);		
	
	//Output the XML contents describing the algorithm
	ofstream outpXML;
	outpXML.open(argv[4]);
	
	outpXML << "<log>" << endl;
	outpXML << "<QualityControl algorithm_name=\"Allele Frequency Test\" algorithmParamsSetting=\"threshold=" << argv[2] << "\"/>" << endl;
	outpXML << "</log>";
	
	outpXML.close();	
}
