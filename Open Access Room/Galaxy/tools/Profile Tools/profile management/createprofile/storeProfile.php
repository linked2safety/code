<?
/* 
   storeProfile.php - stores profiles into database

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
session_start();
if(!isset($_SESSION['myuser_id']))
{
 header("location:main_login.html");
}
else
{
 $empty_filename = '/^\s*$/';

 if(preg_match($empty_filename,$_POST['profilename'],$match)==1)
 {
   echo "The profile name is not entered.<br>";
   echo '<div style="text-align:center">';
   echo '<a href="http://l2s.cs.man.ac.uk/createprofile/createProfile.php">';
   echo '<input type ="button" value="Create Profile">';
   echo '</a>';
   echo '</div>';
 }
 else if(profile_name_exist()==false)
    store_profile();
 else
 {
   echo "Enter a different profile name.<br>The profile name exists.<br>";
   echo '<div style="text-align:center">';
   echo '<a href="http://l2s.cs.man.ac.uk/createprofile/createProfile.php">';
   echo '<input type ="button" value="Create Profile">';
   echo '</a>';
   echo '</div>';
 }
}

function profile_name_exist()
{
 include('db.php');

 $user_id = $_SESSION['myuser_id'];

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password") or die('Could not connect: ' . pg_last_error());
 $query = "select profile from safetyalertnotificationsubscription where profile ~ 'Profile Name:".$_POST['profilename']."#' and userid=$user_id";

 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);
 pg_free_result($result);
 pg_close($dbconn);

 //echo $row['profile'].'<br>';
 if($row == '')//no profile with specified profile name and sql query returns an empty string
  return false;
 else
  return true;
} 

function store_profile()
{
//get profile from the form and store into database
//Each profile is stored into the 'profile' field of the safetyalertnotificationsubscription table.
//profile format: Profile Name:diabetes1#Current Drug Treatment:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNPs:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014, at 18.40 pm
 
 $drug_pattern = '/^drugs_select([_.\d]+)$/';
 $measurement_pattern = '/^measurements_select([_.\d]+)$/';//group 1 contains the random number which is used to find the name of the measurement value <element>, because the name of measurement <select> has same random number as the name of the value <select> element  
$snp_pattern = '/^snps_select([_.\d]+)$/'; 
$adverse_event_pattern = '/^aes_select([_.\d]+)$/';
 
 $drugs='';
 $measurements='';
 $snps='';
 $adverse_events='';
 
 $profile_name = $_POST['profilename'];
 
 foreach ($_POST as $key => $value)
 {
  //echo $key.'=>'.$value.'<br>';
  if(preg_match($drug_pattern,$key,$drug_match)==1)//key is the name of the drug <select> element, value is a drug
  {
   if($value != 'Select a Variable')
       $drugs = $drugs.$value.'='.$_POST['drugs_selectvalues'.$drug_match[1]].',';
  }
  else if(preg_match($measurement_pattern,$key,$measurement_match)==1)
  {
   if($value != 'Select a Variable')  
       $measurements = $measurements.$value.'='.$_POST['measurements_selectvalues'.$measurement_match[1]].',';
  }
  else if(preg_match($snp_pattern,$key,$snp_match)==1)//key is name of a SNP <select> element, value is a SNP 
  {
   if($value != 'Select a Variable')
       $snps = $snps.$value.'='.$_POST['snps_selectvalues'.$snp_match[1]].',';
  }
  else if(preg_match($adverse_event_pattern,$key,$adverse_event_match)==1)//key is name of an adverse event <select> element, value is an adverse event 
  {
    if($value != 'Select a Variable')
    {
     $date = $_POST['date'.$adverse_event_match[1]];
     if($date === '')//if date not entered by user, add 'unknown date'
        $adverse_events = $adverse_events.$value.'='.$_POST['aes_selectvalues'.$adverse_event_match[1]].'(date not entered),';
     else
        $adverse_events = $adverse_events.$value.'='.$_POST['aes_selectvalues'.$adverse_event_match[1]].'('.$date.'),';
    }
  }
  else
  {}
 }
 $drugs = 'Current Drug Treatments:'.$drugs;
 $measurements = 'Measurements:'.$measurements;
 $snps = 'SNPs:'.$snps;
 $adverse_events = 'Adverse Events History:'.$adverse_events;
 #$date = 'Date of Profile Creation:'.date("d/M/y G.i:s", time());//date when this profile is created
 $date = 'Date of Profile Creation:'.date("j F y, \a\\t G.i a", time());//date when this profile is created
 
 $drugs = rtrim($drugs,",");
 $measurements = rtrim($measurements,",");
 $snps = rtrim($snps,",");
 $adverse_events = rtrim($adverse_events,",");

 $profile = 'Profile Name:'.$profile_name.'#'.$drugs.'#'.$measurements.'#'.$snps.'#'.$adverse_events.'#'.$date;

 include('db.php');

 $user_id = $_SESSION['myuser_id'];
 
 //insert the profile into the safetyalertnotificationsubscription table
 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());
 $query = "INSERT INTO safetyalertnotificationsubscription (profile,subscription,userid) VALUES('$profile','no','$user_id')";
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 pg_free_result($result);
 pg_close($dbconn);

 echo "The virtual profile: $profile_name has been created on the Linked2Safety platform.<br>";
 echo '<div style="text-align:center">';
 echo '<a href="http://l2s.cs.man.ac.uk/createprofile/logout.php">';
 echo '<input type ="button" value="Logout">';
 echo '</a>';
 echo '</div>';
}
?>
