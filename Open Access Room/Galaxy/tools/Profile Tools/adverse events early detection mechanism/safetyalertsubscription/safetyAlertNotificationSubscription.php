<?
/* 
   safetyAlertNotificationSubscription.php - subscribes profiles to safety alerts notification

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
error_reporting(E_ALL);
ini_set( 'display_errors','1');
 
session_start();
if(!isset($_SESSION['myuser_id']))
  header("location:main_login.html");
else
{
  create_cron_jobs();
}

function insertCronJobIntoCronJobTable($array,$user_id,$profile_id,$cmd)
{
 $minute = $array['minute'.$profile_id];
 if($minute =='*')
 $minute = '\'*\'';
 $hour = $array['hour'.$profile_id];
 if($hour =='*')
 $hour = '\'*\'';
 $day_of_month = $array['day_of_month'.$profile_id];
 if( $day_of_month == '*' )
 $day_of_month = '\'*\'';
 $month = $array['month'.$profile_id];
 if($month == '*')
 $month = '\'*\'';
 $day_of_week = $array['day_of_week'.$profile_id];
 if($day_of_week == '*')
 $day_of_week = '\'*\'';
 $query = 'SELECT * from cron_job WHERE userid = '.$user_id.' and profileid = '.$profile_id;
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);
 if($row == '')//the profile has no cron jobs in the cron_job table, add a new cron job for the profile
 {
  $query = 'INSERT into cron_job (userid,profileid,min,hour,day_of_month,month,day_of_week,cmd) VALUES('.$user_id.','.$profile_id.','.$minute.','.$hour.','.$day_of_month.','.$month.','.$day_of_week.', \''.$cmd.'\')';
 }
 else //update the cron job of the profile
 {
  $query = 'UPDATE cron_job SET min = '.$minute.', hour = '.$hour.', day_of_month = '.$day_of_month.', month = '.$month.', day_of_week = '.$day_of_week.', cmd = \''.$cmd.'\' WHERE userid = '.$user_id.' and profileid = '.$profile_id;
 }
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 pg_free_result($result); 
}

function addCronJobToCrontabFile($crontab,$user_id,$profile_id,$cmd)
{
 $query = 'SELECT min,hour,day_of_month,month,day_of_week,cmd from cron_job WHERE userid='.$user_id.' and profileid = '.$profile_id;
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);

 $minute = $row['min'];
 $hour = $row['hour'];
 $day_of_month = $row['day_of_month'];
 $month = $row['month'];
 $day_of_week = $row['day_of_week'];
 $cmd = $row['cmd'];
 
 $cron_job = $minute.' '.$hour.' '.$day_of_month.' '.$month.' '.$day_of_week.' '.$cmd;
 $crontab -> append_cronjob($cron_job);

}

function updateNotificationSubscriptionStatus($user_id,$profile_id)
{
 $query = 'UPDATE safetyalertnotificationsubscription set subscription=\'yes\' WHERE userid='.$user_id.' and profileid = '.$profile_id;
 $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 pg_free_result($result);
}

function create_cron_jobs()
{
 include 'db.php';
 include 'Ssh2_crontab_manager2.php';
 
 if (!array_key_exists('myuser_id',$_SESSION) or !array_key_exists('myusername',$_SESSION) or !array_key_exists('mypassword',$_SESSION))
   header("location:main_login.html");
 else
{
 $user_id = $_SESSION['myuser_id'];
 $user_name = $_SESSION['myusername'];
 $user_password= $_SESSION['mypassword'];
 
 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 try {
      $crontab = new Ssh2_crontab_manager2('localhost','22',$user_name,$user_password);  
 }
 catch (Exception $e)
 {
   echo 'Session expired. Login again.';
   echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/safetyalertsubscription/logout.php\"><input type =\"button\" value=\"Login\"></a></div>";
   exit;
 }
 
 $pattern ='/^minute([0-9]+)$/';//minute field pattern; each cron job has one minute field
 
 foreach($_POST as $key => $value)
 {  
  if(preg_match($pattern,$key,$match)==1)//if a profile id variable is obtained then get the cron job times and the cmd corresponding to the profile id
  {
   $profile_id = $match[1];
   $dir = '/var/www/safetyalertsubscription/';//directory of generateSafetyAlert.php
   $cmd = 'php '.$dir.'generateSafetyAlert.php '.$profile_id.' '.$user_id;
   insertCronJobIntoCronJobTable($_POST,$user_id,$profile_id,$cmd);
   addCronJobToCrontabFile($crontab,$user_id,$profile_id,$cmd);
   updateNotificationSubscriptionStatus($user_id,$profile_id);
  }
 }
 //display a confirmation of subscription
 echo "<h2 align=\"center\">Safety alert notification subscription started.</h2>";
 echo "<h3 align=\"center\">Schedules of Safety Alerts Notification in Greece time:</h3>";
 echo "<table width=\"1000\" border=\"1\" align=\"center\" cellpadding=\"0\" cellspacing=\"1\">";
 echo "<tr>";
 echo "<th width =\"78\"> Profile Name </th>";
 echo "<th width =\"78\"> Minute </th>";
 echo "<th width =\"78\"> Hour </th>";
 echo "<th width =\"78\"> Day of Month </th>";
 echo "<th width =\"78\"> Month </th>";
 echo "<th width =\"78\"> Day of Week</th>";
 echo "</tr>";

 foreach($_POST as $key => $value)
 //foreach($array as $key => $value)
 {
  if(preg_match($pattern,$key,$match)==1)//if a profile id variable is obtained then get the cron job times and the cmd corresponding to the profile id
  {
   $profile_id = $match[1];
  
   $query = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
   $result = pg_query($query) or die('Query failed: '.pg_last_error());
   $row = pg_fetch_array($result, null, PGSQL_ASSOC);

   //get name of the profile
   $fields = explode('#',$row['profile']);
   $profile_name_field = explode(":",$fields[0]);
   $profile_name = $profile_name_field[1];
  
   $query = 'SELECT min,hour,day_of_month,month,day_of_week,cmd from cron_job WHERE userid='.$user_id.' and profileid='.$profile_id;
   $result = pg_query($query) or die('Query failed: ' . pg_last_error());
   $row = pg_fetch_array($result, null, PGSQL_ASSOC);

   echo '<tr>';
   echo "<td align =\"center\" width =\"78\" readonly>".$profile_name."</td>";
   echo "<td align =\"center\" width =\"78\" readonly>".$row["min"]."</td>";//minute
   echo "<td align =\"center\" width =\"78\" readonly>".$row["hour"]."</td>";//hour
   echo "<td align =\"center\" width =\"78\" readonly>".$row["day_of_month"]."</td>";//day of month
   echo "<td align =\"center\" width =\"78\" readonly>".$row["month"]."</td>";//month
   echo "<td align =\"center\" width =\"78\" readonly>".$row["day_of_week"]."</td>";//day of week
   echo '</tr>';
  }
 }
 echo '</table>';
 echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/safetyalertsubscription/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
 pg_close($dbconn);
}
}
?>
  



