<?
/* 
   Profile.php - defines the Profile class

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
class Profile {

 public $drugs;
 public $AEs;
 public $MMs;
 public $snps;
 public $drugs_values;
 public $AEs_values;
 public $MMs_values;
 public $snps_values;
 //fields of a profile
 public $profile_name;
 public $drugs_field;
 public $MMs_field;
 public $snps_field;
 public $AEs_history;

function __construct()
{
 $this -> drugs_values="";
 $this -> AEs_values="";
 $this -> MMs_values="";
 $this -> snps_values="";
 $this -> profile_name="";
 $this -> drugs_field="";
 $this -> MMs_field="";
 $this -> snps_field="";
 $this -> AEs_history="";
}

function get_variables_and_values($host,$dbname,$user,$password)
{
 $user_id = $_SESSION['myuser_id'];
 
 include('db.php');

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 //get drugs from categoriesmeaning table
 $query = "select distinct name from categoriesmeaning where type = 'D'";
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $i=0;
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
  //get all the values of a drug except the missing value '888'
  $query2 = "select values from categoriesmeaning where name = '".$row["name"]."'";
  $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
  $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);
  $values_of_drug = $this -> get_variable_values($row2["values"],"888");
  $this -> drugs[$i] = $row["name"];
  $this -> drugs_values .= "|".$row["name"]."=".$values_of_drug;//create a string: |BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA
  $i++;
 }
 //get medical measurements from categoriesmeaning table 
 $query = "select distinct name from categoriesmeaning where type = 'MM'";
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $i=0;
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
  //get all the values of a measurement except the missing value '888'
  $query2 = "select values from categoriesmeaning where name = '".$row["name"]."'";
  $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
  $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);
  $values_of_MM = $this -> get_variable_values($row2["values"],"888");
  $this -> MMs[$i] = $row["name"];
  $this -> MMs_values .= "|".$row["name"]."=".$values_of_MM;//create a string: |BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA
  $i++;
 }
 //get the SNPs from categoriesmeaning table 
 $query = "select distinct name from categoriesmeaning where type = 'SNP'";
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $i=0;
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
  //get all the values of a SNP except the missing value '888'
  $query2 = "select values from categoriesmeaning where name = '".$row["name"]."'";
  $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
  $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);
  $values_of_snp = $this -> get_variable_values($row2["values"],"888");
  $this -> snps[$i] = $row["name"];
  $this -> snps_values .= "|".$row["name"]."=".$values_of_snp;//create a string: |BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA
  $i++;
 }
 //get adverse events from categoriesmeaning table
 $query = "select distinct name from categoriesmeaning where type = 'AE'";
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $i=0;
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
  //get all the values of a adverse event variable except the missing value '888'
  $query2 = "select values from categoriesmeaning where name = '".$row["name"]."'";
  $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
  $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);
  $values_of_ae = $this -> get_variable_values($row2["values"],"888");
  $this -> AEs[$i] = $row["name"];
  $this -> AEs_values .= "|".$row["name"]."=".$values_of_ae;//create a string: |BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA
  $i++;
 }
 pg_free_result($result);
 pg_close($dbconn);
}

function get_variable_values($variable_values,$missing_value)
 {//get the values of a variable and put them into a string variable
  //input: values (categories) and their meanings of a variable e.g. 0:888,1:1,2:2,3:3,4:4,5:5,6:6,7:7
  //       missing value e.g. '888'
  //output: the meanings of the values of the variable as a string excluding the missing value in the format: 1,2,3,4,5,6,7
  $meanings = "";
  $categories_meanings = explode(",",$variable_values);
  foreach($categories_meanings as $category_meaning)
  {
    $category_meaning2 = explode(":",$category_meaning);
    if($category_meaning2[1] != $missing_value)
       $meanings .= $category_meaning2[1].",";
  }
  return rtrim($meanings,",");
 }

//Each profile is stored into the 'profile' field of the safetyalertnotificationsubscription table.
//profile format: Profile Name:diabetes1#Current Drug Treatments:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNPs:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014
 
function get_profile_name($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: the profile name
  if(preg_match('/^Profile Name[:](.+)#Current Drug Treatments[:].+$/',$profile,$match)==1) 
     $this -> profile_name = $match[1];
  else
     $this -> profile_name = "'Profile Name' field is not found"; 
}

function get_drugs_field($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: drugs in the profile
  if(preg_match('/^.+#Current Drug Treatments[:](.+)#Measurements[:].+$/',$profile,$match)==1) 
     $this -> drugs_field = $match[1];
  else
     $this -> drugs_field = "'Current Drug Treatment' field is not found"; 
}

function get_measurements_field($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: measurements in the profile
  if(preg_match('/^.+#Measurements[:](.+)#SNPs[:].+$/',$profile,$match)==1) 
     $this -> MMs_field = $match[1];
  else
     $this -> MMs_field = "'Measurements' field is not found"; 
}

function get_snps_field($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: snps in the profile
  if(preg_match('/^.+#SNPs[:](.+)#Adverse Events History[:].+$/',$profile,$match)==1) 
     $this -> snps_field = $match[1];
  else
     $this -> snps_field = "'SNPs' field is not found"; 
}

function get_AEs_history_field($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: adverse events history in the profile
  if(preg_match('/^.+#Adverse Events History[:](.+)#Date of Profile Creation[:].+$/',$profile,$match)==1) 
     $this -> AEs_history = $match[1];
  else
     $this -> AEs_history = "'Adverse Events History' field is not found"; 
}

function get_profile_date_field($profile)
{//input: profile field of safetyalertnotificationsubscription table
 //return: date of profile creation in the profile
  if(preg_match('/^.+#Date of Profile Creation[:](.+)$/',$profile,$match)==1) 
     $this -> AEs_history = $match[1];
  else
     $this -> AEs_history = "'Date of Profile Creation' field is not found"; 
}

}
?>

