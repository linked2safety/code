<!--
    Query Interface for Galaxy

    Copyright (C) 2014  Panagiotis Hasapis (Panagiotis.hasapis@intrasoft-intl.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    This component links (bundles) to DHTMLx library , which is available
    under a GPLv2 license. You may download the source code and associated license at: http://www.dhtmlx.com

    This component links (bundles) to JQuerylibrary , which is available
    under an MIT license. You may download the source code and associated license at: https://jquery.org/

    The source files are available at:  https://github.com/linked2safety/code
-->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.1/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.1/jquery-ui.js"></script>        
        <script>
                var dimIndex = 0;
                var loadedTable = {};

                function getRange(dataproperty)
                {
                    var checkQuery = "";
                    $.ajax({  
                            url: "https://secure.linked2safety-project.eu/jboss/sehrrelatedservices/webresources/datapropertiesranges/" + dataproperty,  
                            type: "POST",
                            data: checkQuery,
                            processData: false,  
                            contentType: false, 
                            success: function (res) {
                                
                                document.getElementById(dataproperty + "_selected").innerHTML = res;
                                return('');
                                
                                html = "";
                                
                                for(var key in res) {
                                    if(res.hasOwnProperty(key))
                                    {
                                        html += "<option value='" + key  + "'>" +res[key] + "</option>";
                                        alert(html);
                                    }
                                }
                                
                                return html;
                            }
                    });                      

                }

                function presentSelectedVariables()
                {
                    var variable_selected = "";

                    $('#selectedvalues').empty();
                    $('#selectedvalues').append('<p>Assign potential values:<br>');
                    $('#selectedvalues').append('<table>');
                    $('input[id^="dimension"]').each(function() {
                        if($(this).val()!="")                            

                        $('#selectedvalues').append('<tr><td>' + $(this).val() + '</td><td><select id=\"' + $(this).val() + '_type\">'
                                    + '<option value=\"selfReported\">Self Reported</option>'
                                    + '<option value=\"diagnosed\">Diagnosed</option>'
                                    + '<option value=\" \">NA</option>'
                                    + '</select></td><td><select id=\"' + $(this).val() + '_selected\">' +  '</select></td></tr>');
                        getRange(variable_selected);                                                    

                    });
                    
                    $('#selectedvalues').append('</table>');
                    
                }
        
                function addSelectionRow()
                {
                   $('.containerwid').append('<div class="ui-widget"><label for="dimensions' + dimIndex + '">Dimensions: </label><input id="dimension' + dimIndex + '" onclick="javascript:addSelectionRow()"></div>');
                    
                   $(function() {
                       var availableTags = [
                       ];
                       $( "#dimension" + dimIndex).autocomplete({
                            source: availableTags
                       });
                    });                      
                    dimIndex++;
                }
                function formulateQuery()
                {
                    var dimensionBaseURL = "http://www.linked2safety-project.eu/properties/";
                    var sparqlQuery = "where { ";
                    var parameters = "";
                    
                    var pred = 0;
                    
                    $('input[id^="dimension"]').each(function() {
                        if($(this).val()!="")                            
                            sparqlQuery = sparqlQuery + " ?a <" + dimensionBaseURL + $(this).val().toLowerCase() + "> ?v_" + pred + + ".%0A";
                            parameters += "?v_" + (pred++) + " ";
                            console.log(sparqlQuery);
                    });

                    sparqlQuery += "?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c.%0A";
                    sparqlQuery += "BIND(STR(?c) AS ?v_" + pred + ").%0A";    
                    sparqlQuery += addStudyProperties() + "}%0A";                    

                    sparqlQuery = "PREFIX lmds: <http://www.linked2safety.eu/lmds2#>%0A" +
                                "PREFIX l2s-dim: <http://www.linked2safety-project.eu/properties/>%0A" +
                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>%0A" +
                                "select " + parameters + "?v_" + pred + " " + sparqlQuery;
                       

                   if(document.getElementById('cluster').checked)
                        window.location = "https://1.1.1.1:3030/tool_runner?tool_id=upload1&file_type=auto&files_0|url_paste=http://1.1.1.1:3030/L2STriplestoreInterface/query?sparql=cluster" + encodeURIComponent(sparqlQuery) + "&cluster=yes&refresh=refresh&runtool_btn=Execute&callback=?%22%20%29%20%%20%28%20gal_host,%20gal_host,%20temp_file%20%29";    
                   else
                        window.location = "https://1.1.1.1:3030/tool_runner?tool_id=upload1&file_type=auto&files_0|url_paste=http://1.1.1.1:3030/L2STriplestoreInterface/query?sparql=" + encodeURIComponent(sparqlQuery) + "&cluster=yes&refresh=refresh&runtool_btn=Execute&callback=?%22%20%29%20%%20%28%20gal_host,%20gal_host,%20temp_file%20%29";                           
                }
                
                function loadForm()
                {
                        $(function() {
                       var availableTags = [

                       ];
                       $( "#dimension" ).autocomplete({
                       source: availableTags
                       });
                       });                    

                        
                }

                function performCountQuery(sparql, provider)
                {
                    $.ajax({  
                            url: "https://secure.linked2safety-project.eu/jboss/L2STriplestoreInterface/query",  
                            type: "POST",  
                            data: "sparql=" + sparql,  
                            processData: true,
                            success: function (res) {
                                var downlimit = res.split("\n")[1].split("^")[0];
                                var diff = downlimit % 10;
                                downlimit = downlimit- diff;
                                document.getElementById("response").innerHTML += "Number of subjects found(" + provider + "): " + downlimit + "-" + (downlimit+10);
                            },
                            error: function (xhr, ajaxOptions, thrownError) {  
                                alert('error');
                                console.log(xhr.status);
                                console.log(thrownError);                                
                            }                             
                    });                 
                }

                function addStudyProperties()
                {
                    var studyproperties = "";
                    
                    $(':checkbox:checked').each(function() {
                            studyproperties += "?dataset <" + studyPropertiesURL + "> <" + studyPropertyBaseURL + $(this).attr('name') + ">. ";
                            alert($(this).attr('name'));
                    });
                    
                    return studyproperties;
                }
                
                function returnInstitutionsDetails()
                {
                    var institutionsDetails = "<table></table>";

                    return institutionsDetails;
                }

        </script>        
        <title>Linked2Safety Query Interface</title>
    </head>
    <body onload="javascript:loadForm()">
        <br><br>
        <p> In each line you select, another row for dimension is added.
        <p>
           <p>Studies type: 
            <input type="checkbox" name="doubleBlind" value="doubleBlind">Double blind 
            <input type="checkbox" name="multicenter" value="multicenter">Multicenter 
            <input type="checkbox" name="randomized" value="randomized">Randomized
            <input type="checkbox" name="doubleDummy" value="doubleDummy">Double Dummy
            <input type="checkbox" name="crossOver" value="crossOver">Cross Over
            <input type="checkbox" name="singleDose" value="singleDose">Single Dose
            <br><br>
            <input type="checkbox" id="cluster" value="yes">Aggregate related drugs (if drugs selected in variables menu)<br><br>
            <a href="javascript:void(0);" onClick="presentSelectedVariables();">Select</a> | 
            <a href="javascript:void(1);" onClick="getSubjectCount();">Get Subject Count</a> |
            <a href="javascript:void(2);" onClick="formulateQuery();">Search</a><br><br>
            <br><br>            

        <div class="containerwid">
            <div class="ui-widget">
                <label for="dimensions">Dimensions: </label>
                <input id="dimension" onclick="javascript:addSelectionRow()">
            </div>
        </div>            
        <div id="selectedvalues" style="width:400px; height:318px;background-color:#ffffff"></div>                                        
    </body>
</html>
