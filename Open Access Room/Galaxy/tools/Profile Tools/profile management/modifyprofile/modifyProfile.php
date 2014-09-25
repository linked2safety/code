<?
/* 
   modify.php - modifies a profile

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
  echo "<a href=\"http://l2s.cs.man.ac.uk/modifyprofile/selectProfile.php\"><input type =\"button\" value=\"Back\"></a>";
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

/*
$drugs_values2 = "delete=delete".$profile -> drugs_values;
$snps_values2 = "delete=delete".$profile -> snps_values;
$AEs_values2 = "delete=delete".$profile -> AEs_values;
$MMs_values2 = "delete=delete".$profile -> MMs_values;
*/
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
<form action="storeProfile.php" method="post">
<div id="header" style="background-color:#FFA500;">
<h1 align="center" style="margin-bottom:0;">Modify Virtual Profile</h1>
</div>
<div id="form" align="center" style="background-color:#FFD700;">
<table width="50%" align="center" border="1">
<?
 $user_id = $_SESSION['myuser_id'];
 $profile_id = $_POST["profile_id"];

 $_SESSION['profile_id'] = $profile_id;

 $dbconn = pg_connect("host=$host dbname=$dbname user=$user password=$password")
    or die('Could not connect: ' . pg_last_error());

 $query = 'select profile from safetyalertnotificationsubscription where userid='.$user_id.' and    profileid ='.$profile_id;
 $result = pg_query($query) or die('Query failed: '.pg_last_error());
 $row = pg_fetch_array($result, null, PGSQL_ASSOC);

 $fields = explode('#',$row['profile']);
 $profile_name_field = explode(":",$fields[0]);
 $profile_name = $profile_name_field[1];
?>
<!--1st row -->
<tr>
<th> Profile Name:</th>
<td align="center" id="profilename_id" >
<input type="text" align="center" name="profilename" value="<?echo $profile_name;?>" readonly>
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
<?
 $profile_str = $row['profile'];
 $profile -> get_drugs_field($profile_str);

 if($profile -> drugs_field ==='')//no drugs in the Current Drug Treatments field of the profile
{
 echo' <tr id="drugs_tr_id">';
 echo '<td align="center">';
 echo '<select name="drugs_select1" onchange="measurementChange(this,\''. $drugs_values2.'\',\'drugs_table\',\'drugs_value1_id\',\'drugs_tr_id\')">';
 echo '<option value="Select a Variable" selected>Select a Variable</option>';
 foreach($drugs2 as $drug)
 {
  echo '<option value="'.$drug.'" >'.$drug.'</option>';
 }
 echo '<option value="delete">delete</option>';
 echo '</select>';
 echo '</td>';
 echo '<td align="center">';
 echo '=';
 echo '</td>';
 echo '<td align="center">';
 echo '<select name="drugs_selectvalues1" id="drugs_value1_id">';
 echo '<option value="0">Select a Value</option>';
 echo '</select>';
 echo '</td>';
 echo '</tr>'; 
}
 else 
{
 $drug_vals = explode(',',$profile -> drugs_field);
 foreach($drug_vals as $drug_val) 
 {
  $drug_val2 = explode('=',$drug_val);
  $selected_drug = $drug_val2[0];
  $selected_drug_val = $drug_val2[1];
  //create a list of drugs for each drug in the field of the profile and a list of values of the drug
  $r = mt_rand(10,9999999) * mt_rand(10,9999999);
  $row_id = 'drugs_row_id'.(string)$r;
  echo '<tr id="'.$row_id.'">';
  echo '<td align="center">';
  echo '<select name="drugs_select'.(string)$r.'" onchange="measurementChange(this,\''.$drugs_values2.'\',\'drugs_table\',\'drugs_value'.(string)$r."_id','$row_id')\">";
  foreach($drugs2 as $drug)
  {
   if($drug === $selected_drug)
   {
     echo '<option value="'.$selected_drug.'" selected>'.$selected_drug.'</option>';
   }   
   else
   {
     echo '<option value="'.$drug.'">'.$drug.'</option>';
   }   
  }
  echo '<option value="delete">delete</option>';
   
  echo '</select>';
  echo '</td>';
  echo '<td align="center">';
  echo '=';
  echo '</td>';
  //create a list of the values of the drug in the Current Drug Treatments field
  echo '<td align="center">';
  //get the values of the drug in the Current Drug Treatments field 
  $drugs_values3 = explode('|',$drugs_values2);//format: |drug1=yes,no|drug2=Yes,No
  echo '<select name="drugs_selectvalues'.(string)$r.'" id="drugs_value'.(string)$r.'_id">';
  foreach($drugs_values3 as $drug_values)
  {
    $drug_values4 = explode('=',$drug_values);
    if($drug_values4[0] === $selected_drug)//if the selected drug is obtained, create a list of values of the drug
    {
      $vals = explode(',',$drug_values4[1]);
      foreach($vals as $val)
      {
        if($val === $selected_drug_val)
         echo '<option value="'.$selected_drug_val.'" selected>'.$selected_drug_val.'</option>';
        else
         echo '<option value="'.$val.'">'.$val.'</option>';
      }
      break;
    }
  }
  echo '</select>';
  echo '</td>';
  echo '</tr>';
 }
}
?>
</table>
</td>
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
<?
 $profile -> get_measurements_field($profile_str);

 if($profile -> MMs_field ==='')//no measurements in the Medical Measurements field of the profile
{
 echo' <tr id="tr1_id">';
 echo '<td align="center">';
 echo '<select name="measurements_select1" onchange="measurementChange(this,\''.$MMs_values2."','measurements_table','value1_id','tr1_id')\">";
 echo '<option value="Select a Variable" selected>Select a Variable</option>';
 foreach($MMs2 as $MM)
 {
  echo '<option value="'.$MM.'" >'.$MM.'</option>';
 }
 echo '<option value="delete">delete</option>';
 echo '</select>';
 echo '</td>';
 echo '<td align="center">';
 echo '=';
 echo '</td>';
 echo '<td align="center">';
 echo '<select name="measurements_selectvalues1" id="value1_id">';
 echo '<option value="0">Select a Value</option>';
 echo '</select>';
 echo '</td>';
 echo '</tr>'; 
}
 else 
{
 $MM_vals = explode(',',$profile -> MMs_field);
 foreach($MM_vals as $MM_val) 
 {
  $MM_val2 = explode('=',$MM_val);
  $selected_MM = $MM_val2[0];
  $selected_MM_val = $MM_val2[1];
  //create a list of MMs for each MM in the field of the profile and a list of values of the MM
  $r = mt_rand(10,9999999) * mt_rand(10,9999999);
  $row_id2 = 'measurements_row_id'.(string)$r;
  echo '<tr id="'.$row_id2.'">';
  echo '<td align="center">';
  echo '<select name="measurements_select'.(string)$r.'" onchange="measurementChange(this,\''.$MMs_values2.'\',\'measurements_table\',\'MMs_value'.(string)$r."_id','$row_id2')\">";
  foreach($MMs2 as $MM)
  {
   if($MM === $selected_MM)
   {
     echo '<option value="'.$selected_MM.'" selected>'.$selected_MM.'</option>';
   }   
   else
   {
     echo '<option value="'.$MM.'">'.$MM.'</option>';
   }   
  }
  echo '<option value="delete">delete</option>';
   
  echo '</select>';
  echo '</td>';
  echo '<td align="center">';
  echo '=';
  echo '</td>';
  //create a list of the values of the MM in the Medical Measurements field
  echo '<td align="center">';
  //get the values of the MM in the Medical Measurements field 
  $MMs_values3 = explode('|',$MMs_values2);//format: |drug1=yes,no|drug2=Yes,No
  echo '<select name="measurements_selectvalues'.(string)$r.'" id="MMs_value'.(string)$r.'_id">';
  foreach($MMs_values3 as $MM_values)
  {
    $MM_values4 = explode('=',$MM_values);
    if($MM_values4[0] === $selected_MM)//if the selected MM is obtained, create a list of values of the MM
    {
      $vals = explode(',',$MM_values4[1]);
      foreach($vals as $val)
      {
        if($val === $selected_MM_val)
         echo '<option value="'.$selected_MM_val.'" selected>'.$selected_MM_val.'</option>';
        else
         echo '<option value="'.$val.'">'.$val.'</option>';
      }
      break;
    }
  }
  echo '</select>';
  echo '</td>';
  echo '</tr>';
 }
}
?>
</table>
</td>
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
<?
 $profile -> get_snps_field($profile_str);

 if($profile -> snps_field ==='')//no measurements in the Medical Measurements field of the profile
{
 echo' <tr id="snps_tr1_id">';
 echo '<td align="center">';
 echo '<select name="snps_select1" onchange="measurementChange(this,\''.$snps_values2."','snps_table','snps_value1_id','snps_tr1_id')\">";
 echo '<option value="Select a Variable" selected>Select a Variable</option>';
 foreach($snps2 as $snp)
 {
  echo '<option value="'.$snp.'" >'.$snp.'</option>';
 }
 echo '<option value="delete">delete</option>';
 echo '</select>';
 echo '</td>';
 echo '<td align="center">';
 echo '=';
 echo '</td>';
 echo '<td align="center">';
 echo '<select name="snps_selectvalues1" id="snps_value1_id">';
 echo '<option value="0">Select a Value</option>';
 echo '</select>';
 echo '</td>';
 echo '</tr>'; 
}
 else 
{
 $snps_vals = explode(',',$profile -> snps_field);
 foreach($snps_vals as $snp_val) 
 {
  $snp_val2 = explode('=',$snp_val);
  $selected_snp = $snp_val2[0];
  $selected_snp_val = $snp_val2[1];
  //create a list of SNPs for each SNP in the field of the profile and a list of values of the SNP
  $r = mt_rand(10,9999999) * mt_rand(10,9999999);
  $row_id3 = 'snps_row_id'.(string)$r;
  echo '<tr id="'.$row_id3.'">';
  echo '<td align="center">';
  echo '<select name="snps_select'.(string)$r.'" onchange="measurementChange(this,\''.$snps_values2.'\',\'snps_table\',\'snps_value'.(string)$r."_id','$row_id3')\">";
  foreach($snps2 as $snp)
  {
   if($snp === $selected_snp)
   {
     echo '<option value="'.$selected_snp.'" selected>'.$selected_snp.'</option>';
   }   
   else
   {
     echo '<option value="'.$snp.'">'.$snp.'</option>';
   }   
  }
  echo '<option value="delete">delete</option>';
   
  echo '</select>';
  echo '</td>';
  echo '<td align="center">';
  echo '=';
  echo '</td>';
  //create a list of the values of the SNP in the SNPs field
  echo '<td align="center">';
  //get the values of the SNP in the SNPs field 
  $snps_values3 = explode('|',$snps_values2);//format: |rs0=AA,AT,CC|rs1=CG,GG,GG
  echo '<select name="snps_selectvalues'.(string)$r.'" id="snps_value'.(string)$r.'_id">';
  foreach($snps_values3 as $snp_values)
  {
    $snp_values4 = explode('=',$snp_values);
    if($snp_values4[0] === $selected_snp)//if the selected SNP is obtained, create a list of values of the SNP
    {
      $vals = explode(',',$snp_values4[1]);
      foreach($vals as $val)
      {
        if($val === $selected_snp_val)
         echo '<option value="'.$selected_snp_val.'" selected>'.$selected_snp_val.'</option>';
        else
         echo '<option value="'.$val.'">'.$val.'</option>';
      }
      break;
    }
  }
  echo '</select>';
  echo '</td>';
  echo '</tr>';
 }
}
?>
</table>
</td>
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
<?
 $profile -> get_AEs_history_field($profile_str);

 if($profile -> AEs_history ==='')//no AEs in the Adverse Events History field of the profile
{
 echo' <tr id="aes_tr1_id">';
 echo '<td align="center">';
 echo '<select name="aes_select1" onchange="measurementChange(this,\''.$AEs_values2."','aes_table','aes_value1_id','aes_tr1_id')\">";
 echo '<option value="Select a Variable" selected>Select a Variable</option>';
 foreach($AEs2 as $AE)
 {
  echo '<option value="'.$AE.'" >'.$AE.'</option>';
 }
 echo '<option value="delete">delete</option>';
 echo '</select>';
 echo '</td>';
 echo '<td align="center">';
 echo '=';
 echo '</td>';
 echo '<td align="center">';
 echo '<select name="aes_selectvalues1" id="aes_value1_id">';
 echo '<option value="0">Select a Value</option>';
 echo '</select>';
 echo '</td>';
 echo '<td align="center" id="Date1" >';
 echo '<input type="text" name="date1">';
 echo '</td>';
 echo '</tr>'; 
}
 else 
{
 $aes_vals = explode(',',$profile -> AEs_history);//AE history format: diabetes=control(unknown date),cancer=case(10/11/08)
 foreach($aes_vals as $aes_val) 
 {
  $aes_val2 = explode('=',$aes_val);
  $selected_ae = $aes_val2[0];
  preg_match('/^(.+)\((.+)\)$/',$aes_val2[1],$match);
  $selected_ae_val = $match[1];
  $ae_date = $match[2];
  //create a list of AEs for each AE in the AE history field and a list of values of the AE
  $r = mt_rand(10,9999999) * mt_rand(10,9999999);
  $row_id4 = 'snps_row_id'.(string)$r;
  echo '<tr id="'.$row_id4.'">';
  echo '<td align="center">';
  echo '<select name="aes_select'.(string)$r.'" onchange="measurementChange(this,\''.$AEs_values2.'\',\'aes_table\',\'aes_value'.(string)$r."_id','$row_id4')\">";
  foreach($AEs2 as $AE)
  {
   if($AE === $selected_ae)
   {
     echo '<option value="'.$selected_ae.'" selected>'.$selected_ae.'</option>';
   }   
   else
   {
     echo '<option value="'.$AE.'">'.$AE.'</option>';
   }   
  }
  echo '<option value="delete">delete</option>';
   
  echo '</select>';
  echo '</td>';
  echo '<td align="center">';
  echo '=';
  echo '</td>';
  //create a list of the values of the AE in the AEs history field
  echo '<td align="center">';
  //get the values of the AE in the AEs history field 
  $AEs_values3 = explode('|',$AEs_values2);//format: |rs0=AA,AT,CC|rs1=CG,GG,GG
  echo '<select name="aes_selectvalues'.(string)$r.'" id="aes_value'.(string)$r.'_id">';
  foreach($AEs_values3 as $AE_values)
  {
    $AE_values4 = explode('=',$AE_values);
    if($AE_values4[0] === $selected_ae)//if the selected AE is obtained, create a list of values of the AE
    {
      $vals = explode(',',$AE_values4[1]);
      foreach($vals as $val)
      {
        if($val === $selected_ae_val)
         echo '<option value="'.$selected_ae_val.'" selected>'.$selected_ae_val.'</option>';
        else
         echo '<option value="'.$val.'">'.$val.'</option>';
      }
      break;
    }
  }
  echo '</select>';
  echo '</td>';
 //create a date text field
  echo '<td align="center" id="Date'.(string)$r.'" >';
  if($ae_date != 'date not entered')
    echo '<input type="text" name="date'.(string)$r.'" value="'.$ae_date.'">';
  else
    echo '<input type="text" name="date'.(string)$r.'">';
  echo '</td>';
  echo '</tr>';
 }
}
?>
</table>
</td>
<td align="center">
<INPUT align="center" type="button" value="Add Adverse Event" onclick="add_measurement('aes_table','<? echo $AEs_values2; ?>','aes_select','aes_select_id','add_textfield')"/>
</td>
</tr>
</table>
</div>
<div align="center" id="footer" style="background-color:#FFA500;clear:both;text-align:center;">
<input type="submit" value="Modify"><a href="http://l2s.cs.man.ac.uk/modifyprofile/selectProfile.php"><input type ="button" value="Cancel"></a>
</div>
</form>
</body>
</html>
