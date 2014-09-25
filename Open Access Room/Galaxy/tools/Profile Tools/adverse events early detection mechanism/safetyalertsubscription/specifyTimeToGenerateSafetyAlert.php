<?
/* 
   specifyTimeToGenerateSafetyAlert.php - specifies the times in Greece time when safety alerts are generated 

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
  echo "<a href=\"http://l2s.cs.man.ac.uk/safetyalertsubscription/selectProfile.php\"><input type =\"button\" value=\"Back\"></a>";
  exit;
} 
?>

<!DOCTYPE html>
<html>
<body>
<div width="1000" id="header">
<h1 align="center" style="margin-bottom:0;">Schedule Safety Alerts Notification</h1>
</div>
<hr>
<h3>Safety alert notifications are scheduled in terms of minute, hour, day of month, month and day of week in Greece time.</h3>
The allowed ranges of these fields are:
<ol>
<li> minute: 0 to 59, * (* = every minute)</li>
<li> hour: 0 to 23 (0 = midnight), * (* = every hour)</li>
<li> day of month: 1 to 31, * (* = every day)</li>
<li> month: 1 to 12, * (* = every month)</li>
<li> day of week: 0 to 6, * (* = every day of week)</li>
</ol>

<?
schedule_safety_alert_notification();
?>

Examples:<br>
<ol>
<!-- example 1 -->
<li> To schedule safety alert notification at 8:30 am on 10th June:<br>
<table border="1" cellpadding="0" cellspacing="1">
<tr>
<th width ="78"> Minute </th>
<th width ="78"> Hour </th>
<th width ="78"> Day of Month </th>
<th width ="78"> Month </th>
<th width ="78"> Day of Week</th>
</tr>
<tr>
<td width ="78"> 30 </td>
<td width ="78"> 8 </td>
<td width ="78"> 10 </td>
<td width ="78"> 6 </td>
<td width ="78"> * </td>
</tr>
</table>
</li>
<!-- example 2 -->
<li> To schedule safety alert notification at 22:00pm on every day:<br>
<table border="1" cellpadding="0" cellspacing="1">
<tr>
<th width ="78"> Minute </th>
<th width ="78"> Hour </th>
<th width ="78"> Day of Month </th>
<th width ="78"> Month </th>
<th width ="78"> Day of Week</th>
</tr>
<tr>
<td width ="78"> 0 </td>
<td width ="78"> 22 </td>
<td width ="78"> * </td>
<td width ="78"> * </td>
<td width ="78"> * </td>
</tr>
</table>
</li>
<!-- example 3 -->
<li> To schedule safety alert notification at 20:00pm on every wednesday:<br>
<table border="1"cellpadding="0" cellspacing="1">
<tr>
<th width ="78"> Minute </th>
<th width ="78"> Hour </th>
<th width ="78"> Day of Month </th>
<th width ="78"> Month </th>
<th width ="78"> Day of Week</th>
</tr>
<tr>
<td width ="78"> 0 </td>
<td width ="78"> 20 </td>
<td width ="78"> * </td>
<td width ="78"> * </td>
<td width ="78"> 3 </td>
</tr>
</table>
</li>
</ol>
<hr>

<?
function schedule_safety_alert_notification()
{
 include 'db.php';

 $user_id = $_SESSION['myuser_id'];

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 echo "<form action=\"safetyAlertNotificationSubscription.php\", method=\"POST\">";
 echo "<table width=\"1000\" border=\"1\" align=\"center\" cellpadding=\"0\" cellspacing=\"1\">";
 echo "<tr>";
 echo "<th width =\"78\"> Profile Name </th>";
 echo "<th width =\"78\"> Minute </th>";
 echo "<th width =\"78\"> Hour </th>";
 echo "<th width =\"78\"> Day of Month </th>";
 echo "<th width =\"78\"> Month </th>";
 echo "<th width =\"78\"> Day of Week</th>";
 echo "</tr>";

 foreach($_POST as $profile_id)
 {
  $query = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
  $result = pg_query($query) or die('Query failed: '.pg_last_error());
  $row = pg_fetch_array($result, null, PGSQL_ASSOC);

  //get name of the profile
  $fields = explode('#',$row['profile']);
  $profile_name_field = explode(":",$fields[0]);
  $profile_name = $profile_name_field[1];

  echo '<tr>';
  echo "<td align =\"center\" width =\"78\"><input type=\"text\" align =\"center\" name=\"".'profile_id'.$profile_id."\" value=\"".$profile_name."\" readonly></td>";
  echo "<td align =\"center\" width =\"78\"><select name=\"".'minute'.$profile_id."\">";//minute
  echo '<option value="*" selected>*</option>';
  for($i=0; $i < 60; $i++)
    echo '<option value="'.$i.'" >'.$i.'</option>';
  echo '</select>';
  echo "</td>";
  echo "<td align =\"middle\" width =\"78\"><select name=\"".'hour'.$profile_id."\">";//hour
  echo '<option value="*" selected>*</option>';
  for($i=0; $i < 24; $i++)
    echo '<option value="'.$i.'" >'.$i.'</option>';
  echo '</select>';
  echo '</td>';
  echo "<td align =\"center\" width =\"78\"><select name=\"".'day_of_month'.$profile_id."\">";//day of month
  echo '<option value="*" selected>*</option>';
  for($i=1; $i < 32; $i++)
    echo '<option value="'.$i.'" >'.$i.'</option>';
  echo '</select>';
  echo "</td>";
  echo "<td align =\"center\" width =\"78\"><select name=\"".'month'.$profile_id."\">";//month
  echo '<option value="*" selected>*</option>';
  for($i=1; $i < 13; $i++)
    echo '<option value="'.$i.'" >'.$i.'</option>';
  echo '</select>';
  echo "</td>";
  echo "<td align =\"center\" width =\"78\"><select name=\"".'day_of_week'.$profile_id."\">";//day of week
  echo '<option value="*" selected>*</option>';
  for($i=0; $i < 7; $i++)
    echo '<option value="'.$i.'" >'.$i.'</option>';
  echo '</select>';
  echo "</td>";
  echo '</tr>';
 }
 pg_free_result($result);
 pg_close($dbconn);
 echo '</table>';
 echo "<div width=\"1000\" id=\"footer\" style=\"clear:both;text-align:center\">";
 echo "<input type=\"submit\" value=\"Subscribe\">";
 echo '<a href="http://l2s.cs.man.ac.uk/safetyalertsubscription/selectProfile.php"><input type ="button" value="Cancel"></a>';
 echo "<a href=\"http://l2s.cs.man.ac.uk/safetyalertsubscription/logout.php\"><input type =\"button\" value=\"Logout\"></a>";
 echo "</div>";
 echo '</form>';
 echo '</body>';
 echo '</html>';
}
?>
