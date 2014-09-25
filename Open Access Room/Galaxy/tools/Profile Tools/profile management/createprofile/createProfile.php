<?
/* 
   createProfile.php - creates a virtual profile

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

include 'Profile.php';
include 'db.php';

$profile = new Profile;

$profile -> get_variables_and_values($host,$dbname,$user,$password);

$drugs2 = $profile -> drugs;
$snps2 = $profile -> snps;
$AEs2 = $profile -> AEs;
$MMs2 = $profile -> MMs;

natcasesort($drugs2);
natcasesort($snps2);
natcasesort($AEs2);
natcasesort($MMs2);

$drugs_values0 = $profile -> drugs_values;
$snps_values0 = $profile -> snps_values;
$AEs_values0 = $profile -> AEs_values;
$MMs_values0 = $profile -> MMs_values;

$drugs_values2 = sort_variables_values($drugs2,$drugs_values0);
$snps_values2 = sort_variables_values($snps2,$snps_values0);
$AEs_values2 = sort_variables_values($AEs2,$AEs_values0);
$MMs_values2 = sort_variables_values($MMs2,$MMs_values0);

function sort_variables_values($variables, $variables_values){

 $variables_values1 = explode("|",$variables_values);#drugs_values: |BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA
 
 foreach($variables_values1 as $variable_value1)
 {
  $var_val = explode("=",$variable_value1);
  if($var_val != '')
     $vals[$var_val[0]] = $var_val[1];
 }

 $variables_values1='';

 foreach($variables as $variable)
 {
  $variables_values1 = $variables_values1.'|'.$variable.'='.$vals[$variable]; 
 }
 $variables_values1 = 'delete=delete'.$variables_values1;

 return $variables_values1;
}

/*$drugs_values2 = "delete=delete".$profile -> drugs_values;
$snps_values2 = "delete=delete".$profile -> snps_values;
$AEs_values2 = "delete=delete".$profile -> AEs_values;
$MMs_values2 = "delete=delete".$profile -> MMs_values;*/

?>

<!DOCTYPE html>
<html>
<style>
table
{
 border-spacing:5px;
}
</style>
<body>
<script src="createProfile.js" type="text/javascript">
</script>
<script>
function validateForm()
{
 var x_name = document.forms["myForm"]["profilename"].value;
 if(x_name =='' || x_name == null)
 {
  alert("Profile name must be entered.");
  return false;
 }
}
</script>
<form name="myForm" action="storeProfile.php" onsubmit="return validateForm()" method="post">
<div id="header" style="background-color:#FFA500;">
<h1 align="center" style="margin-bottom:0;">Create Virtual Profile</h1>
</div>
<div id="form" align="center" style="background-color:#FFD700;">
<table width="50%" align="center" border="1">
<!--1st row -->
<tr>
<th> Profile Name:</th>
<td align="center" id="profilename" >
<input type="text" name="profilename">
</td>
</tr>
<!--2nd row-->
<tr>
<th colspan="2">Current Drug Treatments<br>
(Enter the drugs which are being taken)
</th>
</tr>
<tr>
<td align="center">
<table id = "drugs_table">
<tr id="drugs_tr_id">
<td align="center">
<select name="drugs_select1" onchange="measurementChange(this,'<? echo $drugs_values2;?>','drugs_table','drugs_value1_id','drugs_tr_id')">
<option value="Select a Variable" selected>Select a Variable</option>
<?
foreach($drugs2 as $drug)
{
 echo '<option value="'.$drug.'" >'.$drug.'</option>';
}
?>
<option value="delete">delete</option>
</select>
</td>
<td align="center">
=
</td>
<td align="center">
<select name="drugs_selectvalues1" id="drugs_value1_id">
<option value="0">Select a Value</option>
</select>
</td>
</tr>
</table>
<td align="center">
<INPUT align="center" type="button" value="Add Drug" onclick="add_measurement('drugs_table','<? echo $drugs_values2; ?>','drugs_select','drugs_select_id','not_add_textfield')"/>
</td>
</tr>
<!--3rd row -->
<tr>
<th colspan="2">Medical Measurements</th>
</tr>
<tr>
<td align="center">
<table id = "measurements_table">
<tr id="tr1_id">
<td align="center">
<select name="measurements_select1" onchange="measurementChange(this,'<? echo $MMs_values2;?>','measurements_table','value1_id','tr1_id')">
<option value="Select a Variable" selected>Select a Variable</option>
<?
foreach($MMs2 as $MM)
{
 echo '<option value="'.$MM.'" >'.$MM.'</option>';
}
?>
<option value="delete">delete</option>
</select>
</td>
<td align="center">
=
</td>
<td align="center">
<select name="measurements_selectvalues1" id="value1_id">
<option value="0">Select a Value</option>
</select>
</td>
</tr>
</table>
<td align="center">
<INPUT align="center" type="button" value="Add Measurement" onclick="add_measurement('measurements_table','<? echo $MMs_values2; ?>','measurements_select','measurements_select_id','not_add_textfield')"/>
</td>
</tr>
<!--4th row-->
<tr>
<th colspan="2">SNP Information</th>
</tr>
<tr>
<td align="center">
<table id = "snps_table">
<tr id="snps_tr1_id">
<td align="center">
<select name="snps_select1" onchange="measurementChange(this,'<? echo $snps_values2;?>','snps_table','snps_value1_id','snps_tr1_id')">
<option value="Select a Variable" selected>Select a Variable</option>
<?
foreach($snps2 as $snp)
{
 echo '<option value="'.$snp.'" >'.$snp.'</option>';
}
?>
<option value="delete">delete</option>
</select>
</td>
<td align="center">
=
</td>
<td align="center">
<select name="snps_selectvalues1" id="snps_value1_id">
<option value="0">Select a Value</option>
</select>
</td>
</tr>
</table>
<td align="center">
<INPUT align="center" type="button" value="Add SNP" onclick="add_measurement('snps_table','<? echo $snps_values2; ?>','snps_select','snps_select_id','not_add_textfield')"/>
</td>
</tr>
<!--5th row-->
<tr>
<th colspan="2">Adverse Events History</th>
</tr>
<tr>
<td align="center">
<table id = "aes_table">
<tr>
<td colspan="3" align="center">
Adverse Events
</td>
<td align="center">
Date of Detection
</td>
<td>
</td>
</tr>
<tr id="aes_tr1_id">
<td align="center">
<select name="aes_select1" onchange="measurementChange(this,'<? echo $AEs_values2;?>','aes_table','aes_value1_id','aes_tr1_id')">
<option value="Select a Variable" selected>Select a Variable</option>
<?
foreach($AEs2 as $AE)
{
 echo '<option value="'.$AE.'" >'.$AE.'</option>';
}
?>
<option value="delete">delete</option>
</select>
</td>
<td align="center">
=
</td>
<td align="center">
<select name="aes_selectvalues1" id="aes_value1_id">
<option value="0">Select a Value</option>
</select>
</td>
<td align="center" id="Date1" >
<input type="text" name="date1">
</td>
</tr>
</table>
</td>
<td align="center">
<INPUT align="center" type="button" value="Add Adverse Event" onclick="add_measurement('aes_table','<? echo $AEs_values2; ?>','aes_select','aes_select_id','add_textfield')"/>
</td>
</tr>
</table>
</div>
<div align="center" id="footer" style="background-color:#FFA500;clear:both;text-align:center;">
<input type="submit" value="Create"><a href="http://l2s.cs.man.ac.uk/createprofile/logout.php"><input type ="button" value="Logout"></a>
</div>
</form>
</body>
</html>
