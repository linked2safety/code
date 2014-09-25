<?
/* 
   SafetyAlert.php - defines the SafetyAlert class

   Copyright 2014 The University of Manchester tiand@cs.man.ac.uk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  
   The source files are available at: https://github.com/linked2safety/code
*/
class SafetyAlert{

function generate_a_safety_alert($user_id,$profile_id,$profileObj)
{
   $query = 'SELECT profile FROM safetyalertnotificationsubscription WHERE userid='.$user_id.' and profileid='.$profile_id;
   
   $result = pg_query($query) or die('Query failed: ' . pg_last_error());
   $row = pg_fetch_array($result, null, PGSQL_ASSOC);
   $profile = $row['profile'];
   $profileObj -> get_profile_name($profile);
   
   echo '<tr>';
   echo '<td align="center"> '.$profileObj -> profile_name.' </td>';
   $profile = $row["profile"];
   $mined_rules = $this -> getMinedRules();
   $reported_rules = $this -> getSelfReportedRules();
   $matched_rules = $this -> generateSafetyAlert($profile,$mined_rules,$reported_rules);
   
   //time of safety alert generation
     
   $date = 'date +'.'"'.'%'.'c'.'"'; 
   $date = shell_exec($date); //run date +"%c" on shell
  
   if ($matched_rules != '')//profile matches adverse events detection rules
   {  
      $safetyalert = array();
      $safetyalert_str='';
      $safetyalert_str0='';
      $adverse_events_detected = 0;

      foreach ($matched_rules as $matched_rule)//add adverse events to $safetyalert array
      {
         $rhs = $matched_rule['righthandside'];
	 $outcomes = explode('&',$rhs);
	 $k = count($outcomes);
	
	 if($k == 1)//one outcome
         {         
	  $safetyalert = $this -> add_outcome($safetyalert,$rhs);
	 }
	 else //more than one outcome
	 {
	  foreach($outcomes as $outcome)
	  {
	   $safetyalert = $this -> add_outcome($safetyalert,$outcome);
	  }
	 }
       }
       //display safety alert on web page and insert safety alert into DB 
       echo "<td>";
       $n = count($safetyalert);
       if($n > 0)
       {
         foreach ($safetyalert as $AE)
         {
	   $safetyalert_str0 .= $AE.'<br>';
           $safetyalert_str .= $AE.';';   	
         }
         $safetyalert_str0 = 'Profile '.$profileObj -> profile_name.' has adverse events: <p style="color:blue">'.$safetyalert_str0.'</p>';
         $safetyalert_str = 'Profile '.$profileObj -> profile_name.' has adverse events: <p style="color:blue">'.$safetyalert_str.'</p>';	 
	 $adverse_events_detected = 1;
       }
       else
       { 
         $safetyalert_str0 = 'No adverse events are detected for the profile '.$profileObj -> profile_name.'.';
         $safetyalert_str = 'No adverse events are detected for the profile '.$profileObj -> profile_name.'.';
	 $adverse_events_detected = 0;
       }
       echo $safetyalert_str0; 
       echo "</td>";
       if($adverse_events_detected == 1)
       {
        $rules='';
        foreach ($matched_rules as $matched_rule)
        {
         $lhs = str_replace('&',' ',$matched_rule['lefthandside']);
         $rhs = str_replace('&',' ',$matched_rule['righthandside']);
         $rules = $rules.$lhs.' => '.$rhs.'<br>';
        }
       }
       else
       {
	$rules = 'No adverse events detection rules match the profile '.$profileObj -> profile_name.'.';;
       }      
       echo "<td> $rules </td>";
       echo "<td align=\"center\"> $date </td>";
       echo "</tr>";
      
       $rules = str_replace('<br>',';',$rules);
       $query = "INSERT INTO safetyalert (profileid,safetyalert,rules,date) VALUES('$profile_id','$safetyalert_str','$rules','$date')";
   }
   else//profile does not match adverse events detection rules
   {  $profilename = $profileObj -> profile_name;
      echo "<td align=\"center\"> No adverse events are detected for the profile $profilename.</td>";
      echo "<td align=\"center\"> No adverse events detection rules match the profile $profilename.</td>";
      echo "<td align=\"center\"> $date </td>";
      echo "</tr>";
      $safetyalert_str = 'No adverse events are detected for the profile '.$profileObj -> profile_name.'.';
      $rules = 'No adverse events detection rules match the profile '.$profileObj -> profile_name.'.';
      $query = "INSERT INTO safetyalert (profileid,safetyalert,rules,date) VALUES('$profile_id','$safetyalert_str','$rules','$date')";
   }
   //insert safety alert, rules and date of safety alert generation into SafetyAlert table
   $result = pg_query($query) or die('Query failed: '.pg_last_error());
 return $result;
 }

function add_outcome($safetyalert,$outcome)
{
 //an outcome has form variable=value e.g. diabetes=yes, drug=no
 $pattern = '/^\s*([^\s]+)\s*=\s*([noNO]+)\s*$/';

 $query = "select name from categoriesmeaning where '".$outcome."' ~* name and name in (select name from categoriesmeaning where type='AE')";
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);

 if($row != '')//if outcome contains an adverse event variable
 {
   $return = preg_match($pattern,$outcome,$match);
   if($return == 0)//if outcome has form 'diabetes=yes' or 'cancer=case'
   {
    if(array_search($outcome,$safetyalert) === false)
    {			
     array_push($safetyalert,$outcome);
    }
   }
 }
 return $safetyalert;
}

 function getMinedRules()
 {
   $query = "SELECT lefthandside, righthandside FROM associationrule, categoriesmeaning WHERE visible = true AND righthandside ~ categoriesmeaning.name and categoriesmeaning.name in (select name from categoriesmeaning where type='AE')";
   $rules = pg_query($query) or die('Query failed: ' . pg_last_error());
   return $rules;
 }

 function getSelfReportedRules()
 {
   $query = "SELECT lefthandside, righthandside FROM selfreportedassociationrule, categoriesmeaning WHERE visible = true AND righthandside ~ categoriesmeaning.name and categoriesmeaning.name in (select name from categoriesmeaning where type='AE')";
   $rules2 = pg_query($query) or die('Query failed: ' . pg_last_error());
   return $rules2;
 }
 
//profile format: Profile Name:diabetes1#Current Drug Treatment:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNPs:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014, at 10 pm

 function generateSafetyAlert($profile,$mined_rules,$self_reported_rules)
 {//input: profile, rules resultset from AssociationRule table, rules resultset from selfReportedAssociationRule table
  //output: the rules matching the profile or empty string if no rules match the profile
 
  $adverse_events_of_mined_rules='';
  $adverse_events_of_self_reported_rules='';
  $i=0;

  $matched_rules = array(array());

  while($rule = pg_fetch_array($mined_rules, null, PGSQL_ASSOC))
  {
    $lhs = $rule['lefthandside'];
    $lhs = str_replace(' ','',$lhs);   
    if($this -> profileMatchLeftHandSide($profile,$lhs))
    {//right hand side gives the adverse events
     $adverse_events = $rule['righthandside'];
     $adverse_events2 = str_replace('&',' ',$adverse_events);	
     $adverse_events_of_mined_rules = $adverse_events_of_mined_rules.' '.$adverse_events2;
    
     if(array_search($rule,$matched_rules) === false)//dulicate rules are not stored in array $matched_rules
     { $matched_rules[$i] = $rule;
       $i++;
     }
    }    
  }

  while($rule = pg_fetch_array($self_reported_rules, null, PGSQL_ASSOC))
  {
    $lhs = $rule['lefthandside'];
    $lhs = str_replace(' ','',$lhs);//remove any spaces in lhs
    if($this -> profileMatchLeftHandSide($profile,$lhs))
    {//right hand side gives the adverse events
     $adverse_events = $rule['righthandside'];
     $adverse_events2 = str_replace('&',' ',$adverse_events);	  
     $adverse_events_of_self_reported_rules = $adverse_events_of_self_reported_rules.' '.$adverse_events2;
     if(array_search($rule,$matched_rules) === false)//dulicate rules are not stored in array $matched_rules
     { $matched_rules[$i] = $rule;
       $i++;
     }
    }       
  }

  if($i===0)
     return '';//no rules match profile
  else
     return $matched_rules;
 }

 function profileMatchLeftHandSide($profile,$lhs)
 {
   //foreach condition of lhs
   //{ if profile does not match the condition
   //  then the profile does not match the lhs
   //       break loop;
   //}
   //return: true if profile match the lhs or false otherwise
   
   $profileObj = new Profile;
   $profileObj -> get_drugs_field($profile);
   $profileObj -> get_measurements_field($profile);
   $profileObj -> get_snps_field($profile);
   
   $drugs = $profileObj -> drugs_field; 
   $MMs = $profileObj -> MMs_field; 
   $snps = $profileObj -> snps_field;  				   
    
   $conditions = explode('&',$lhs);
   $match_all_conditions = true;
   foreach($conditions as $condition)
   {
     if($this -> profile_match_condition($drugs,$MMs,$snps,$condition)==false)
     {
      $match_all_conditions = false;
      break;
     }
   }
   return $match_all_conditions;
 }

 function profile_match_condition($drugs,$MMs,$snps,$condition)
 {//match the contents of the fields: drugs, MMs and snps with a condition
  //return: true if a match exists or false otherwise
  //field content formats:
  //'Current Drug Treatments' content format: ibuprofen=yes,aspirin=yes,citalopram=yes
  //'Measurements' content format: bmi=25,smokeEver=yes
  //'SNPs' content format: rs0=CC,rs11=GG
  //  
  //condition format: variable=value
 		
  if($this -> field_match_condition($drugs,$condition)==true)
     return true;
  elseif ($this -> field_match_condition($MMs,$condition)==true)
     return true;
  elseif ($this -> field_match_condition($snps,$condition)==true)
     return true;
  else 
     return false; 
 }

 function field_match_condition($field_content,$condition)
 {//field content format: variable1=value1,variable2=value2,...
  //condition format: variable=value
  //return: true if field content matches condition or false otherwise
  //a rule condition test: variable=value
  if($field_content ==='')
   return false;
  else
  {
   $condition = preg_replace('/[\s_]+/','',$condition);
   $pattern = '/^\s*([^\s]+)\s*=\s*([^\s]+)\s*$/';
   preg_match($pattern,$condition,$match); 
   $cond_variable = $match[1];
   $cond_value = $match[2];  
   $cond_variable = strtolower($cond_variable);
   $cond_value = strtolower($cond_value);

   $elements = explode(',',$field_content);
   $match_condition = false;
   foreach($elements as $element)
   {
    $element = preg_replace('/[\s_]+/','',$element);   
    preg_match($pattern,$element,$match2);
    $field_variable = $match2[1];
    $field_value = $match2[2];
    $field_variable = strtolower($field_variable);
    $field_value = strtolower($field_value);
    if($cond_variable === $field_variable)
      if($cond_value === $field_value)
	{
	   $match_condition = true;
           break;
	}	
   }
   return $match_condition;
  }
 }
}
?>
