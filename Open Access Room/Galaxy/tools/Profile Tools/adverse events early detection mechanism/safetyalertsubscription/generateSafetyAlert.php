<?
/* 
   generateSafetyAlert.php - generates a safety alert for a virtual profile

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
$profile_id = $argv[1];
$user_id = $argv[2];

main($profile_id,$user_id);

function main($profile_id,$user_id)
{
 include 'db.php';
 include 'SafetyAlert.php';
 include 'Profile.php';

 //A profile is stored in the field 'profile' of safetyalertnotificationsubscription table
 //profile format: Profile Name:diabetes1#Current Drug Treatment:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNPs:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014, at 10 pm

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());
 
 $safetyAlert = new SafetyAlert;
 $profileObj = new Profile;

 $result = $safetyAlert -> generate_a_safety_alert($user_id,$profile_id,$profileObj);
 pg_free_result($result);
 pg_close($dbconn);
}
?>
