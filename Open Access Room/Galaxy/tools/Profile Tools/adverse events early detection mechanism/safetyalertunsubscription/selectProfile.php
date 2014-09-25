<?
/* 
  selectProfile.php - selects profiles

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
else
{
echo '<!DOCTYPE html>';
echo '<html>';
echo '<body>';
echo "<form action=\"safetyAlertNotificationUnsubscription.php\", method=\"POST\">";
echo "<div id=\"header\" style=\"background-color:#FFA500\">";
echo "<h1 align=\"center\" style=\"margin-bottom:0;\">Select Profiles to Unsubscribe to Safety Alerts Notification</h1>";
echo "</div>";
echo '<br>';

include('db.php');

$user_id = $_SESSION['myuser_id'];

$dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

//List all the cron jobs of the user with user id
$query = 'SELECT profileid,min,hour,day_of_month,month,day_of_week from cron_job WHERE userid='.$_SESSION['myuser_id'];
$result = pg_query($query) or die('Query failed: ' . pg_last_error());
$row = pg_fetch_array($result, null, PGSQL_ASSOC);
if($row == '')
{
 echo "No profiles are available to select. No profiles have been subscribed to safety alert notification.<br>";
 echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/safetyalertunsubscription/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
 echo "</body>";
 echo "</html>";
}
else
{
 // Printing results in HTML
 echo "<table align =\"center\" border=\"1\">";
 echo "<tr>";
 //Profile Id column heading
 echo "<th> Profile Name </th>";
 echo "<th> Minute </th>";
 echo "<th> Hour </th>";
 echo "<th> Day of Month </th>";
 echo "<th> Month </th>";
 echo "<th> Day of Week</th>";
 echo "</tr>";
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
    $profile_id = $row["profileid"];
    $query2 = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
    $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
    $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);

    //get name of the profile
    $fields = explode('#',$row2['profile']);
    $profile_name_field = explode(":",$fields[0]);
    $profile_name = $profile_name_field[1];

    echo "<tr>";
    $td = ": <input type=\"checkbox\" name=\"profile_id$profile_id\" value=\"$profile_id\"></td>";
    echo "<td> ".$profile_name.$td;
    echo "<td align =\"center\" width =\"78\">".$row["min"]."</td>";//minute
    echo "<td align =\"center\" width =\"78\">".$row["hour"]."</td>";//hour
    echo "<td align =\"center\" width =\"78\">".$row["day_of_month"]."</td>";//day of month
    echo "<td align =\"center\" width =\"78\">".$row["month"]."</td>";//month
    echo "<td align =\"center\" width =\"78\">".$row["day_of_week"]."</td>";//day of week
    echo "</tr>";
 }
 echo "</table>";
 echo "<div id=\"footer\" style=\"background-color:#FFA500;clear:both;text-align:center\">";
 echo "<input type=\"submit\" value=\"Unsubscribe\">";
  echo '<a href="http://l2s.cs.man.ac.uk/safetyalertunsubscription/selectProfile.php"><input type ="button" value="Cancel"></a>';
 echo "<a href=\"http://l2s.cs.man.ac.uk/safetyalertunsubscription/logout.php\"><input type =\"button\" value=\"Logout\"></a>";
 echo "</div>";
 echo "</body>";
 echo "</html>";
}
pg_free_result($result);
pg_close($dbconn);
}
?>



