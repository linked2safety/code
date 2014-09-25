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
<form action="deleteProfile.php", method="POST">
<div id="header" style="background-color:#FFA500">
<h1 align="center" style="margin-bottom:0;">Select Profiles to Delete
</h1>
</div>
<br>
<?
include('db.php');

$user_id = $_SESSION['myuser_id'];

$dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

$query = 'SELECT profileid, profile FROM safetyalertnotificationsubscription where userid='.$user_id;
$result = pg_query($query) or die('Query failed: '.pg_last_error());

$row = pg_fetch_array($result, null, PGSQL_ASSOC);
if(empty($row))
{
 echo "There are no profiles.";
 echo "<div style=\"text-align:center\"><a href=\"http://l2s.cs.man.ac.uk/deleteprofile/logout.php\"><input type =\"button\" value=\"Logout\"></a></div>";
 echo "</body>";
 echo "</html>";
}
else
{
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 while($row = pg_fetch_array($result, null, PGSQL_ASSOC)) 
 {
    $profile_id=$row["profileid"];
    $profile = $row["profile"];
    echo "<table align = \"center\"><tr><td><input type=\"checkbox\" name=\"profile_id$profile_id\" value=\"$profile_id\"></td>";
    echo '<td>';     
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
    echo "</table>";
    echo "</td></tr></table>";
    echo "<br>";
 }

 echo "<div id=\"footer\" style=\"text-align:center\">";
 echo "<input type=\"submit\" value=\"Delete\">";
 echo "<a href=\"http://l2s.cs.man.ac.uk/deleteprofile/logout.php\"><input type =\"button\" value=\"Logout\"></a>";
 echo "</div>";
 echo "</body>";
 echo "</form>";
 echo "</html>";
 pg_free_result($result);
 pg_close($dbconn);
}
?>



