<?
/* 
   deleteProfile.php - deletes profiles

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
  echo "<a href=\"http://l2s.cs.man.ac.uk/deleteprofile/selectProfile.php\"><input type =\"button\" value=\"Back\"></a>";
  exit;
}
else
{
 delete_profiles();
}

function delete_profiles()
{
echo '<!DOCTYPE html>';
echo '<html>';
echo '<body>';
echo "<div id=\"header\" style=\"background-color:#FFA500;\">";
echo "<h1 align=\"center\" style=\"margin-bottom:0;\">Deleted Virtual Profiles</h1></div>";
echo "<div id=\"menu\" style=\"background-color:#FFD700\">";

include('db.php');

$user_id = $_SESSION['myuser_id'];

$dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

foreach( $_POST as $profile_id )
{
  $query = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
  $result = pg_query($query) or die('Query failed: '.pg_last_error());
  $row = pg_fetch_array($result, null, PGSQL_ASSOC);

  $fields = explode('#',$row['profile']);
  $profile_name_field = explode(":",$fields[0]);
  $profile_name = $profile_name_field[1];

  $transaction = "BEGIN; DELETE FROM safetyalert WHERE profileid=$profile_id; DELETE FROM safetyalertnotificationsubscription WHERE userid=$user_id and profileid=$profile_id; COMMIT;";
  $result = pg_query($transaction) or die('Query failed: ' . pg_last_error());
  
  echo 'The profile: '.$profile_name.' has been deleted.<br>';
}
pg_free_result($result);
pg_close($dbconn);

echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/createprofile/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
echo '</body>';
echo '</html>';
}
?>

