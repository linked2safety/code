#    removeDuplicateRules.py - remove duplicate rules from the association rules output by random forest tools
#
#    Copyright (C) 2014 The University of Manchester  tiand@cs.man.ac.uk
#  
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
# 
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
# 
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#  
#    You may download the source code and associated license at  https://github.com/linked2safety/code

import sys
import re

def main():

        rules_file = sys.argv[1]
        rules_file2 = sys.argv[2]
        fr = open(rules_file,"r") 
	fw = open(rules_file2,"w")
	rules = []
        line = fr.readline()
        while line != '':
		line = line.rstrip('\n')
		rules = get_a_rule(line,rules,fw)
		line = fr.readline()
	fr.close()        
	fw.close()

def get_a_rule(line,rules,fw):
	matchObj = re.match('[0-9]+([\\.](.+)=>(.+)lift.+)', line)
        if matchObj!= None:
                lhs = matchObj.group(2)
                rhs = matchObj.group(3)
		ruleStr = matchObj.group(1)
		
		rule = []
		#lhs items
		lhs_items = re.findall('[\\s]+[^\\s]+[\\s]+=[\\s]+[^\\s]+',lhs)
		if lhs_items != []:
			lhs = set()
			for item in lhs_items:
				lhs.add(item)
			#rhs items
			rhs_items = re.findall('[\\s]+[^\\s]+[\\s]+=[\\s]+[^\\s]+',rhs)
			if rhs_items != []:
				rhs = set()
				for item in rhs_items:
					rhs.add(item)
				rule.append(lhs)
				rule.append(rhs)
			else:
				sys.exit('rhs of rule is empty')
                else:
			sys.exit('lhs of rule is empty')
		if rule not in rules:
			rules.append(rule)
			rule_no = len(rules)
			fw.write(str(rule_no)+ruleStr+'\n')
			
        else:
               fw.write(line+'\n') 
        return rules

if __name__ == "__main__":
        main()

