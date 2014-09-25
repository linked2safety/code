<?
/* 
   safetyalertunsubscription.php - unsubscribes profiles to safety alerts notification

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
  echo "<a href=\"http://l2s.cs.man.ac.uk/safetyalertunsubscription/selectProfile.php\"><input type =\"button\" value=\"Back\"></a>";
  exit;
}

unsubscribe();


function unsubscribe()
{
 include 'db.php';
 include 'Ssh2_crontab_manager2.php';

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 $user_id = $_SESSION['myuser_id'];
 $user_name = $_SESSION['myusername'];
 $user_password = $_SESSION['mypassword'];
 
 try {
    $crontab = new Ssh2_crontab_manager2('localhost','22',$user_name,$user_password);  
 }
 catch(Exception $e)
 {
   echo '<h3 align="center">Session expired. Login again.</h3>';
   echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/safetyalertunsubscription/logout.php\"><input type =\"button\" value=\"Login\"></a></div>";
   exit;
 }
 
 foreach ($_POST as $profile_id)
 {
  //delete the cron job of the profile from the cron_job table
  $query = 'DELETE from cron_job WHERE userid = '.$user_id.' and profileid = '.$profile_id;
  $result = pg_query($query) or die('Query failed: ' . pg_last_error());
  //delete the cron job from crontab file
  $crontab -> remove_cronjob('/php \/var\/www\/safetyalertsubscription\/generateSafetyAlert.php '.$profile_id.' '.$user_id.'/');
  //update subscription status of the profile
  $query = 'UPDATE safetyalertnotificationsubscription set subscription=\'no\' WHERE userid='.$user_id.' and profileid = '.$profile_id;
  $result = pg_query($query) or die('Query failed: ' . pg_last_error());
 }
 pg_free_result($result);
 pg_close($dbconn);
 echo "<h3 align=\"center\"> Safety alert notification unsubscribed.</h3><br>"; 
 echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/safetyalertunsubscription/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
}
?>
