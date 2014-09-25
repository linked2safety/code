<?
/* 
   viewSafetyAlertsSubscriptions.php - displays safety alerts notification subscriptions

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

<?
 include 'db.php';

 $user_id = $_SESSION['myuser_id'];

 echo "<div id=\"header\" style=\"background-color:#FFA500\">";
 echo "<h1 align=\"center\" style=\"margin-bottom:0;\">View Safety Alerts Notification Subscriptions</h1>";
 echo "</div>";
 echo "<br>";

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());
 $query = 'SELECT profileid,min,hour,day_of_month,month,day_of_week from cron_job WHERE userid='.$user_id;
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);
 if($row == '')
 {
  echo "No profiles have subscribed to safety alert notifications.";
  echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/viewsubscriptions/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
  echo "</body>";
  echo "</html>";
 }
 else
 {
  echo "<h3 align=\"center\">Schedules of Safety Alerts Notification in Greece Time:</h3>";
  echo "<table width=\"1000\" border=\"1\" align=\"center\" cellpadding=\"0\" cellspacing=\"1\">";
  echo "<tr>";
  echo "<th width =\"78\"> Profile Name </th>";
  echo "<th width =\"78\"> Minute </th>";
  echo "<th width =\"78\"> Hour </th>";
  echo "<th width =\"78\"> Day of Month </th>";
  echo "<th width =\"78\"> Month </th>";
  echo "<th width =\"78\"> Day of Week</th>";
  echo "</tr>";
  $result = pg_query($query) or die('Query failed: ' . pg_last_error());
  while($row = pg_fetch_array($result, null, PGSQL_ASSOC))
  {  
    $profile_id = $row["profileid"];
    $query2 = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
    $result2 = pg_query($query2) or die('Query failed: '.pg_last_error());
    $row2 = pg_fetch_array($result2, null, PGSQL_ASSOC);

    $fields = explode('#',$row2['profile']);
    $profile_name_field = explode(":",$fields[0]);
    $profile_name = $profile_name_field[1];

    echo '<tr>';
    echo "<td align =\"center\" width =\"78\">".$profile_name."</td>";
    echo "<td align =\"center\" width =\"78\">".$row["min"]."</td>";//minute
    echo "<td align =\"center\" width =\"78\">".$row["hour"]."</td>";//hour
    echo "<td align =\"center\" width =\"78\">".$row["day_of_month"]."</td>";//day of month
    echo "<td align =\"center\" width =\"78\">".$row["month"]."</td>";//month
    echo "<td align =\"center\" width =\"78\">".$row["day_of_week"]."</td>";//day of week
    echo '</tr>';
  }
  echo '</table>';
  echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/viewsubscriptions/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
  echo "</body>";
  echo "</html>";
  pg_close($dbconn);
 } 
?>
