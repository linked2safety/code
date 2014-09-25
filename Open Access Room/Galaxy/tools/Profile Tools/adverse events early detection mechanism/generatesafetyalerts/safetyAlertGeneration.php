<?
/* 
   SafetyAlertGeneration.php - generates safety alerts of virtual profiles

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
 exit;
}

if(empty($_POST))
{
  echo "No profiles are selected.<br>";
  echo "<a href=\"http://l2s.cs.man.ac.uk/generatesafetyalerts/selectProfile.php\"><input type =\"button\" value=\"Back\"></a>";
  exit;
} 
?>

<!DOCTYPE html>
<html>
<body>
<h1 align="center">Safety Alerts of Profiles</h1>

<?
 include 'db.php';
 include 'SafetyAlert.php';
 include 'Profile.php';

 $user_id = $_SESSION['myuser_id'];

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());
?>
<table align="center" border="1">
<tr>
<th> Profile Names </th>
<th> Safety Alerts of Profiles </th>
<th> Adverse Events Detection Rules </th>
<th> Date of Safety Alerts Generation (Greece time)</th>
</tr>
<?
 //A profile is stored in the field 'profile' of safetyalertnotificationsubscription table
 //profile format: Profile Name:diabetes1#Current Drug Treatment:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNPs:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014, at 10 pm

 $safetyAlert = new SafetyAlert;
 $profileObj = new Profile;

 foreach( $_POST as $profile_id )
 {
   //echo $profile_id;
   //$user_id = '1'; 
   //$profile_id = '33';
   $result = $safetyAlert -> generate_a_safety_alert($user_id,$profile_id,$profileObj);
 }
 pg_free_result($result);
 pg_close($dbconn); 
?>
</table>
<div style="text-align:center"><a href="http://l2s.cs.man.ac.uk/generatesafetyalerts/logout.php"><input type ="button" value="Logout"></a></div>
</body>
</html>
