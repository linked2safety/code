<?
/* 
   checklogin.php - authenticates the user

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
include 'db.php';

$dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

$email_address=$_POST['email_address'];
$password=$_POST['password'];

$query='SELECT userid, name FROM users WHERE email_address=\''.$email_address.'\' and password=\''.$password.'\'';
$result = pg_query($query) or die('Query failed: ' . pg_last_error());
$row = pg_fetch_array($result, null, PGSQL_ASSOC);

if(empty($row))
 echo 'email address or password is wrong.<br>';
else 
{
 session_start();
 $_SESSION['myuser_id'] = $row["userid"];
 $_SESSION['myusername'] = $row["name"];
 $_SESSION['mypassword'] = $password;
 header("location:selectProfile.php");
}
?>



