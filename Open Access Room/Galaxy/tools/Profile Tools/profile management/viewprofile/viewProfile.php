<?
/* 
   viewProfile.php - displays profiles

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
?>
<!DOCTYPE html>
<html>
<style>
table,th,td
{
border:0.5px solid black;
border-collapse:collapse;
}
</style>
<body>
<div id="header" style="background-color:#FFA500">
<h1 align="center" style="margin-bottom:0;">All Profiles</h1>
</div>
<br>
<?
display_profiles();
?>
<div style="text-align:center"><a href="http://l2s.cs.man.ac.uk/viewprofile/logout.php"><input type ="button" value="Logout"></a></div>
</body>
</html>
<?
function display_profiles()
{
//profile format: Profile Name:diabetes1#Current Drug Treatment:ibuprofen=yes,aspirin=yes,citalopram=yes#Measurements:bmi=25,smokeEver=yes#SNP:rs0=CC,rs11=GG#Adverse Events History:diabetes=control(unknown date),cancer=case(10/11/08)#Date of Profile Creation:10 March 2014
 
 include('db.php');
 $user_id = $_SESSION['myuser_id'];
 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 $query = 'SELECT profileid, profile FROM safetyalertnotificationsubscription where userid='.$user_id;
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);
 if(empty($row))
 { 
  echo "There are no profiles.";
  echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/viewprofile/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
  exit;
 } 

 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
    $profile_id=$row["profileid"];
   
    $profile = $row["profile"];
    echo '<table align="center">';
    $fields = explode('#',$profile);
    foreach($fields as $field)
    {
     $field2 = explode(":",$field);
     $field3 = explode(",",$field2[1]);
     echo "<tr>";
     echo "<th>$field2[0]</th>";
     echo '<td align="center">'.$field3[0];
     for($i=1; $i < count($field3); $i++)
     { 
       echo ",<br>$field3[$i]";
     }
     echo "</td></tr>";
    }
    echo "</table><br>";
 }
 pg_free_result($result);
 pg_close($dbconn);
}
?>



