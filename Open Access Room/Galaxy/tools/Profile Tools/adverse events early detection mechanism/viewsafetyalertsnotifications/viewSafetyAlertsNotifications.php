<?
/* 
   viewSafetyAlertsNotifications.php - displays safety alerts notifications

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
<body>
<div id="header" style="background-color:#FFA500">
<h1 align="center" style="margin-bottom:0">Safety Alerts Notifications</h1>
</div>
<br>
<?
 include 'db.php';

 $user_id = $_SESSION['myuser_id'];

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 $query = 'SELECT profileid, safetyalertid, safetyalert, rules, date FROM safetyalert WHERE profileid in (select profileid from safetyalertnotificationsubscription where userid='.$user_id.') order by date desc';
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);
 
 if($row == '')
 {
  echo "There are no safety alerts notifications.";
  echo "<div style=\"text-align:center\">";
  echo "<a href=\"http://l2s.cs.man.ac.uk/viewsafetyalertsnotifications/logout.php\"><input type =\"button\" value=\"Logout\">";
  echo "</a>";
  echo "</div>";
 }
 else
 {
  echo "<form action=\"deleteSafetyAlert.php\", method=\"POST\">";
  echo "<table align=\"center\" border=\"1\">";
  echo "<tr>";
  echo "<th></th>";
  echo "<th> Profile Names </th>";
  echo "<th> Safety Alerts Notifications of Profiles</th>";
  echo "<th> Adverse Event Detection Rules </th>";
  echo "<th> Dates of Notifications (Greece time)</th>";
  echo "</tr>";
    
  $result = pg_query($query) or die('Query failed: '.pg_last_error());
  while($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
  {
   $query2 = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and profileid ='.$row["profileid"];
   $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
   $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);

   $fields = explode('#',$row2['profile']);
   $profile_name_field = explode(":",$fields[0]);
   $profile_name = $profile_name_field[1];

   $safetyalert = $row["safetyalert"];
   $rules = $row["rules"];
   $date = $row["date"];
   $safetyalert = str_replace(";","<br>",$safetyalert);
   $rules = str_replace(";","<br>",$rules);
   echo "<tr>";

   $profile_id=$row["profileid"];
   $safetyalert_id=$row["safetyalertid"];
   $profile = $row["profile"];
   $ids = $profile_id.'#'.$safetyalert_id;
   echo "<td><input type=\"checkbox\" name=\"ids$profile_id$safetyalert_id\" value=\"$ids\"></td>";    

   echo '<td align="center">'.$profile_name.'</td>';
   echo "<td>$safetyalert</td>";
   echo "<td>$rules</td>";
   echo "<td>$date</td>";
   echo "</tr>";
  }
  echo "</table>";
  echo "<div style=\"text-align:center\">";
  echo "<input type=\"submit\" value=\"Delete\">";
  echo "<a href=\"http://l2s.cs.man.ac.uk/viewsafetyalertsnotifications/logout.php\"><input type =\"button\" value=\"Logout\">";
  echo "</a>";
  echo "</div>";
  echo "</form>";
 }
 pg_free_result($result);
 pg_close($dbconn);
?>
</body>
</html>
