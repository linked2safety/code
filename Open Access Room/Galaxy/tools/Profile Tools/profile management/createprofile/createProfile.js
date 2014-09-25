/* 
   createProfile.js - creates profiles

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
function add_drug_selectlist(drugs_table_id,drugs) 
{//input: drugs table id, 
 //       drugs
 //format of drugs: delete|ibuprofen|aspirin|citalopram|sertraline

    var select_name = 'drug';
    var sel = document.createElement("select");
    var r = Math.random()*12345678909876;
    sel.name = select_name.concat(r.toString());//set the name of the select object to a random string of format 'drug990993435'
    var drugs_list = drugs.split("|");
    var row_id;
 
    for(var i = drugs_list.length-1; i > -1; i--)
    {
     add_option(drugs_list[i],drugs_list[i],sel);
    }

    row_id = addDrugRow(drugs_table_id,sel);        
    sel.onchange = function(){delete_list(drugs_table_id,row_id,this);};
}

function addDrugRow(table_id, selectObj)
{//add a row consisting of one select object into a table
 var indx = document.getElementById(table_id).rows.length;
 var x = document.getElementById(table_id).insertRow(indx);//add a <tr> to table after 1st row and return <tr>
 var y = x.insertCell(0);//add a <td> to <tr> and return <td>
 var r = Math.random()*Math.random();
 var row_id = 'drug_row_id'.concat(r.toString());
 
 x.id = row_id;
 y.appendChild(selectObj);
 
 return row_id;
}

function delete_list(table_id,row_id,selectObj)
{
 //get the index of the selected option
 var idx = selectObj.selectedIndex;
 var option = selectObj.options[idx].value;
 
 if(option === "delete")
 {
  delete_row(table_id,row_id); 
 }
}

function delete_row(table_id,row_id)
{
 var row_idx;
 var row;
 
 row = document.getElementById(row_id);
 document.getElementById(table_id).deleteRow(row.rowIndex);
}

function add_adverse_event(adverse_events_table_id, adverse_events)
{  //format of adverse events: delete|stroke|hypertension|diabetes
    var r = Math.random()*Math.random();
    var AEs_select = create_adverse_events_select_list(adverse_events,r);    
    var row_id = addAdverseEventRow(adverse_events_table_id, AEs_select,r);

    AEs_select.onchange = function(){delete_list(adverse_events_table_id, row_id, this);};
}

function add_adverse_event2(adverse_events_table_id, adverse_events)
{  //format of adverse events: delete|stroke|hypertension|diabetes
    var r = Math.random()*Math.random();
    var name = 'ae';
    var id = 'ae_id';

    var AEs_select = create_measurement_select_list(adverse_events,r,name,r); 
    var values_select = create_measurement_values_list(r,name,id);

    var row_id = addMeasurementsRow(adverse_events_table_id,AEs_select,values_select);

    //var row_id = addAdverseEventRow(adverse_events_table_id, AEs_select,r);   

    //var values_select = create_ae_values_list(r,name,id);
  
    //var row_id = addAdverseEventRow(adverse_events_table_id, AEs_select,values_select, r);

    AEs_select.onchange = function(){delete_list(adverse_events_table_id, row_id, this);};
}

/*
function create_adverse_events_select_list(adverse_events,randNum)
{
 var sel = document.createElement("select");
 var select_name = 'ae';
 
 sel.name = select_name.concat(randNum.toString());//set the name of the select object to a random string of format 'ae2320405'
 var AEs = adverse_events.split("|");
 
 for(var i=AEs.length-1; i > -1; i--)
 {
  add_option(AEs[i],AEs[i],sel);  
 } 
 return sel;
}
*/
/*
function create_ae_values_list(randNum,name,id) 
{ 
   //var select_name = 'value';
    var select_name = name.concat('value');
    var sel = document.createElement("select");
    //var values_id = "value_id".concat(randNum.toString());
    var values_id = id.concat('value').concat(randNum.toString());
    sel.name = select_name.concat(randNum.toString());//set the name of the select object to a random string of format 'value233223'
    sel.id = values_id;//set the id of this values list
    
    add_option("Select a value","0",sel);
   
    return sel;
}
*/
/*
function addAdverseEventRow(table_id,AEsSelectObj,randNum)
{
 var indx = document.getElementById(table_id).rows.length;
 var row = document.getElementById(table_id).insertRow(indx);//add a <tr> to table after 1st row and return <tr>
 var cell1 = row.insertCell(0);//add a <td> to <tr> and return <td>
 var cell2 = row.insertCell(1);
 //var cell3 = row.insertCell(2);
 //var cell4 = row.insertCell(3);

 var row_id = 'ae_row_id'.concat(randNum.toString());
 var txtfield;

 row.id = row_id;

 cell1.appendChild(AEsSelectObj);
 //cell1.align="center";
 //cell2.innerHTML = "=";
 //cell3.appendChild(valuesSelectObj);

 txtfield = document.createElement("input");
 txtfield.setAttribute("type", "text");
 txtfield.setAttribute("name",'date'.concat(randNum.toString()));//set name of text field to a random string of format 'date32322445'
 
 cell2.appendChild(txtfield);
 //cell4.align="center";

 return row_id;
}
*/

/*
function addAdverseEventRow(table_id,AEsSelectObj,valuesSelectObj,randNum)
{
 var indx = document.getElementById(table_id).rows.length;
 var row = document.getElementById(table_id).insertRow(indx);//add a <tr> to table after 1st row and return <tr>
 var cell1 = row.insertCell(0);//add a <td> to <tr> and return <td>
 var cell2 = row.insertCell(1);
 //var cell3 = row.insertCell(2);
 //var cell4 = row.insertCell(3);

 var row_id = 'ae_row_id'.concat(randNum.toString());
 var txtfield;

 row.id = row_id;

 cell1.appendChild(AEsSelectObj);
 //cell1.align="center";
 //cell2.innerHTML = "=";
 //cell3.appendChild(valuesSelectObj);

 txtfield = document.createElement("input");
 txtfield.setAttribute("type", "text");
 txtfield.setAttribute("name",'date'.concat(randNum.toString()));//set name of text field to a random string of format 'date32322445'
 
 cell2.appendChild(txtfield);
 //cell4.align="center";

 return row_id;
}
*/

function add_option(text,value,element) 
{
    var option=document.createElement("option");

    option.text=text;
    option.value=value;

    try
     {
      //for IE earlier than version 8
      element.add(option,element.options[null]);
     }
     catch (e)
     {
      element.add(option,null);
     }  
}

function add_measurement(measurements_table_id, measurement_values, name, id,textfield_option)
{   
    var r = Math.random()*Math.random();
    var measurements_select = create_measurement_select_list(measurement_values,r,name,id);
    var values_select = create_measurement_values_list(r,name,id);

    var row_id = addMeasurementsRow(measurements_table_id,measurements_select,values_select,r,name,textfield_option);

    measurements_select.onchange = function(){measurementChange(this, measurement_values, measurements_table_id, values_select.id, row_id);};
}


function create_measurement_select_list(measurements_values,randNum,name,id)
{
  var sel = document.createElement("select");
  var selectlist_id = id.concat(randNum.toString());

  sel.id = selectlist_id;
  sel.name = name.concat(randNum.toString());

  var measurements_values2 = measurements_values.split("|");
  var newOption = document.createElement("option");

  newOption.value = "Select a Variable";
  newOption.text = "Select a Variable";
  newOption.selected = true;
 
  try {
        sel.add(newOption); 
  }
  catch (e) {
       sel.appendChild(newOption);
  }

  for(var i=0; i < measurements_values2.length; i++)
  {
   var measurements_values3 = measurements_values2[i].split("=");
   var measurement = measurements_values3[0];
   add_option(measurement,measurement,sel);
  }  
   
  return sel;
}

function create_measurement_values_list(randNum,name,id) 
{ 
    var sel = document.createElement("select");
    var values_id = id.concat('values').concat(randNum.toString());
   
    sel.name = name.concat('values').concat(randNum.toString());
    sel.id = values_id;//set the id of this values list
    
    add_option("Select a value","0",sel);
   
    return sel;
}

function addMeasurementsRow(table_id,measuresSelectObj,valuesSelectObj,randNum,name,textfield_option)
{//add a row consisting of a measurements select object and a values select list into a table
 var indx = document.getElementById(table_id).rows.length;
 var row = document.getElementById(table_id).insertRow(indx);//add a <tr> to table after 1st row and return <tr>
 var cell1 = row.insertCell(0);//add a <td> to <tr> and return <td>
 var cell2 = row.insertCell(1);
 var cell3 = row.insertCell(2);
 
if(textfield_option === 'add_textfield')
{ 
 var cell4 = row.insertCell(3);
 //###add a text field
 var txtfield = document.createElement("input");
 txtfield.setAttribute("type", "text");
 txtfield.setAttribute("name",'date'.concat(randNum.toString()));
 cell4.appendChild(txtfield);
}

 var r = Math.random()*Math.random();
 var row_id = name.concat('_row_id').concat(r.toString());
 
 row.id = row_id;

 cell1.appendChild(measuresSelectObj);
 cell2.innerHTML = "=";
 cell3.appendChild(valuesSelectObj); 

 return row_id;
}

function measurementChange(selectObj,measurements_values, measurements_table_id, values_list_id, row_id) 
{//add the values of the selected measurement to the drop-down list next to the meaurement drop-down list
 //format of measurements_values: BMI=1,2,3,4,5,6,7,NA|smokingEver=Yes,No,NA|empty=Select a Measurement
   
  //get the index of the selected option
  var idx = selectObj.selectedIndex;
  // get the value of the selected option
  var which = selectObj.options[idx].value;
 
 if(which === "delete")
 {
  delete_row(measurements_table_id,row_id);
 }
 else if(which === "Select a Variable")
 {
        // get the select element via its known id
        var cSelect = document.getElementById(values_list_id);	
	var len = cSelect.options.length;
	while (cSelect.options.length > 0) {
		cSelect.remove(0);
	}
	var newOption;
	// create new option
	newOption = document.createElement("option");
	newOption.value = "Select a Value";
	newOption.text= "Select a value";
	// add the new option
	try {
	     cSelect.add(newOption);  // this will fail in DOM browsers but is needed for IE
	}
	catch(e) {
            cSelect.appendChild(newOption);
	}	
 }
 else
 {
  var measurements_values2 = measurements_values.split("|");
  var measurements_values3 = new Array();
  var measurement = new Array();
  var measurement_values = new Array();
  var values = new Array();
  var measurementValues = new Object();
  
  for(var i=0; i < measurements_values2.length; i++)
  {
   //document.write("Hello World!");
   measurements_values3 = measurements_values2[i].split("=");
   measurement = measurements_values3[0];
   measurement_values = measurements_values3[1];
   values = measurement_values.split(",");
   measurementValues[measurement] = values;   
  } 
 
  // use the selected option value to retrieve the list of values from the measurementValues array
  cList = measurementValues[which];
  // get the select element via its known id
  var cSelect = document.getElementById(values_list_id);
	
  var len=cSelect.options.length;
  while (cSelect.options.length > 0)
  {
    cSelect.remove(0);
  }
  var newOption;
  // create new options
  for (var i=0; i<cList.length; i++) {
	newOption = document.createElement("option");
	newOption.value = cList[i];  //assumes option string and value are the same
	newOption.text=cList[i];
	// add the new option
	try {
	      cSelect.add(newOption);  // this will fail in DOM browsers but is needed for IE
	    }
	 catch (e) {
		cSelect.appendChild(newOption);

	 }
  }
 }
}

