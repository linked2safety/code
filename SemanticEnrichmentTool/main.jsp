<%-- 
   Copyright 2014 Ntalaperas Dimitrios UBITECH

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   This component links (bundles) to Arbor library name, which is available 
   under MIT license. You may download the source code and associated license 
   at: http://arborjs.org/ home site URL or pointer where it is stored within the component 
   source files.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="style.css"/>
        <title>Semantic Annotator</title>
        <link rel="stylesheet" href="jquery.treeview.css" />

    </head>
    <body>
        <!--<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js"></script>-->
        <script src="lib/jquery.min.js"></script>
        <script src="lib/arbor.js"></script> 
        <script src="graphics.js"></script> 
        <script src="renderer.js"></script>
        <script src="jquery.jstree.js"></script>
        <script src="parser.js"></script>
        <script src="jquery.treeview.js" type="text/javascript"></script>	
        <script type="text/javascript" src="demo.js"></script>

        <table>
            <tr>
                <td colspan="2">
                    <div class="header">
                        Semantic Enrichement Tool
                    </div>
                </td>
            </tr>
            <tr>
                <td rowspan="2">
                    <div class="navigation">
                        <input type="file" id="fileinput" />
                    </div>
                    <br />
                    <div>
                        <canvas id="viewport" width="800" height="800"></canvas>
                    </div>
                </td>
                <td>
                    <div class="annotator">
                        <table>
                            <tr>
                                <td colspan ="2" style="border:0px">
                            <u>Top level annotations:</u>
                            </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Comment:
                                </td>
                                <td style="border:0px">
                                    <textarea id="TopLevelCommentArea"></textarea>
                                </td>
                            </tr>
                            <!--<tr>
                                <td style="border:0px">
                                    How were the data collected
                                </td>
                                <td style="border:0px">
                                    <textarea id="TopLevelDatacollectedArea"></textarea>
                                </td>
                            </tr>-->
                            <tr>
                                <td style="border:0px">
                                    Title:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeTitleArea"></textarea>
                                </td>
                            </tr>                            
                            <tr>
                                <td style="border:0px">
                                    Description:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeDescriptionTitleArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Provider:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeProviderTitleArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Creator:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeCreatorArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Study Project Title:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubestudyProjectTitleArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Study Project Description:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeStudyProjectDescriptionArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Study Project Sponsor:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeStudyProjectSponsorArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Study Clinical Site:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubeStudyClinicalSiteArea"></textarea>
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    Study Project Coordinator:
                                </td>
                                <td style="border:0px">
                                    <textarea id="cubesStudyProjectCoordinatorArea"></textarea>
                                </td>
                            </tr>                              
                            <tr>
                                <td style="border:0px">
                                    <b>Study Properties:</b>
                                    <br/>
                                    Multi Center <input type="checkbox" id="multiCenterChkBox" >
                                    <br/>
                                    Randomized   <input type="checkbox" id="randomizedChkBox" >
                                    <br/>
                                    Double Blind <input type="checkbox" id="doubleBlindChkBox" >
                                    <br/>
                                    Double Dummy <input type="checkbox" id="doubleDummyChkBox" >
                                    <br/>
                                    Cross Over   <input type="checkbox" id="crossOverChkBox" >
                                    <br/>
                                    Single Dose  <input type="checkbox" id="singleDoseChkBox" >
                                </td>
                            </tr>
                            <tr>
                                <td colspan ="2" style="border:0px">
                                    <b>Concept:</b>
                                </td>
                            <tr>
                                <td colspan="2" style="border:0px">
                                    <ul id="browser" class="filetree" style="height:200px;overflow-y:auto">
                                        <li><span class="folder">Non Infectious Disease</span>
                                            <ul>
                                                <li><span class="folder">AllergyImmunologyRheumatology &nbsp;<input type="checkbox" id="ch_0" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#AllergyImmunologyRheumatology' onclick='handleCheckBoxClick(this);'></span></li>

                                            </ul>
                                            <ul>
                                                <li><span class="folder">CardiovascularDiseases &nbsp;<input type="checkbox" id="ch_1" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#CardiovascularDiseases' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">CoronaryHeartDisease &nbsp;<input type="checkbox" id="ch_2" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#CoronaryHeartDisease' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Stroke &nbsp;<input type="checkbox" id="ch_3" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Stroke' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Hypertension &nbsp;<input type="checkbox" id="ch_4"  name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#Hypertension' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">MyocardialInfarction &nbsp;<input type="checkbox" id="ch_5" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#MyocardialInfarction' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>
                                                </li>
                                            </ul>
                                            <ul>
                                                <li><span class="folder">Dermatology &nbsp;<input type="checkbox" id="ch_6" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Dermatology' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">SkinProblemNOS &nbsp;<input type="checkbox" id="ch_7" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#SkinProblemNOS' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul> 
                                                </li>
                                            </ul>    
                                            <ul>
                                                <li><span class="folder">EndocrinologyAndMetabolism &nbsp;<input type="checkbox" id="ch_8" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#EndocrinologyAndMetabolism' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">MetabolicSyndrome &nbsp;<input type="checkbox" id="ch_9" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#MetabolicSyndrome ' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Obesity &nbsp;<input type="checkbox" id="ch_10" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Obesity' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">ThyroidAndOtherHormonalProblems &nbsp;<input type="checkbox" id="ch_11" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#ThyroidAndOtherHormonalProblems'onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Diabetes &nbsp;<input type="checkbox" id="ch_12" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#Diabetes' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Dyslipidemia &nbsp;<input type="checkbox" id="ch_13" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#Dyslipidemia' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>  
                                                </li>
                                            </ul>                                     
                                            <ul>
                                                <li><span class="folder">GastrointestinalDiseases &nbsp;<input type="checkbox" id="ch_14" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#GastrointestinalDiseases' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">DigestiveProblems &nbsp;<input type="checkbox" id="ch_15" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#DigestiveProblems' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>                                                      
                                                </li>
                                            </ul>

                                            <ul>
                                                <li><span class="folder">HematologyAndOncology &nbsp;<input type="checkbox" id="ch_16" name="http://hcls.deri.ie/l2s/sehr/chuv/1.0#HematologyAndOncology" onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">Cancer &nbsp;<input type="checkbox" id="ch_17" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Cancer' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>                                                          
                                                </li>

                                            </ul>
                                            <ul>
                                                <li><span class="folder">Neurology &nbsp;<input type="checkbox" id="ch_18" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Neurology' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">PerceptiveDisorders &nbsp;<input type="checkbox" id="ch_19" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#PerceptiveDisorders' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">ParkinsonsDisease &nbsp;<input type="checkbox" id="ch_20" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#ParkinsonsDisease' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Epilepsy &nbsp;<input type="checkbox" id="ch_21" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#Epilepsy' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">HuntingtonsDisease &nbsp;<input type="checkbox" id="ch_22" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#HuntingtonsDisease' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">MigraineCumulative &nbsp;<input type="checkbox" id="ch_23" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#MigraineCumulative' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">MultipleSclerosis &nbsp;<input type="checkbox" id="ch_24" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#MultipleSclerosis' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>                                                     
                                                </li>
                                            </ul>
                                            <ul>
                                                <li><span class="folder">PsychiatricDisorders &nbsp;<input type="checkbox" id="ch_25" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#PsychiatricDisorders' onclick='handleCheckBoxClick(this);'></span>
                                                    <ul>
                                                        <li><span class="folder">SuicideAttemps &nbsp;<input type="checkbox" id="ch_26" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#SuicideAttemps' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">Depression &nbsp;<input type="checkbox" id="ch_27" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#Depression' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">SchizotypalPersonalityDisorder &nbsp;<input type="checkbox" id="ch_28" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#SchizotypalPersonalityDisorder' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">AnxietyDisorders &nbsp;<input type="checkbox" id="ch_29" name='http://hcls.deri.ie/l2s/chuv/1.0#AnxietyDisorders' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">ChildhoodDisorders &nbsp;<input type="checkbox" id="ch_30" name='http://hcls.deri.ie/l2s/chuv/1.0#ChildhoodDisorders' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">EatingDisorders &nbsp;<input type="checkbox" id="ch_31" name='http://hcls.deri.ie/l2s/chuv/1.0#EatingDisorders' onclick='handleCheckBoxClick(this);'></span></li>
                                                        <li><span class="folder">ImpulseControlDisorders &nbsp;<input type="checkbox" id="ch_32" name='http://hcls.deri.ie/l2s/chuv/1.0#ImpulseControlDisorders' onclick='handleCheckBoxClick(this);'></span></li>
                                                    </ul>                                                         
                                                </li>
                                            </ul>
                                            <ul>
                                                <li><span class="folder">RenalDisease &nbsp;<input type="checkbox" id="ch_33" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#RenalDisease' onclick='handleCheckBoxClick(this);'></span></li>
                                            </ul>
                                            <ul>
                                                <li><span class="folder">RespiratoryDiseases &nbsp;<input type="checkbox" id="ch_34" name='http://hcls.deri.ie/l2s/sehr/chuv/1.0#RespiratoryDiseases' onclick='handleCheckBoxClick(this);'></span></li>
                                            </ul>
                                            <ul>
                                                <li><span class="folder">OphthalmologicalProblems &nbsp;<input type="checkbox" id="ch_35" name='http://hcls.deri.ie/l2s/sehr/commonVariables/1.0#OphthalmologicalProblems' onclick='handleCheckBoxClick(this);'></span></li>
                                            </ul> 
                                            <ul>
                                                <li><span class="folder">Anemia &nbsp;<input type="checkbox" id="ch_36" name='http://www.ifomis.org/acgt/1.0#Anemia' onclick='handleCheckBoxClick(this);'></span></li>
                                            </ul>                                               
                                        </li>
                                    </ul>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" style="border:0px">
                            <u>Dimension level annotations:</u>
                            </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_0" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_0" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>
                                    <!--<textarea id="dim1CommentArea" hidden="true"></textarea>-->
                                </td>
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_1" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_1" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option>                                        
                                    </select>                                    
                                    <!--<textarea id="dim2CommentArea" hidden="true"></textarea>-->
                                </td>

                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_2" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_2" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim3CommentArea" hidden="true"></textarea>-->
                                </td>

                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_3" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_3" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_4" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_4" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_5" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_5" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_6" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_6" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_7" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_7" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_8" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_8" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_9" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_9" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_10" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_10" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_11" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_11" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_12" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_12" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_13" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_13" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_14" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_14" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_15" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_15" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_16" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_16" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_17" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_17" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_18" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_18" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_19" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_19" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_20" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_20" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_21" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_21" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_22" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_22" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_23" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_23" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_24" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_24" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_25" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_25" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr> 
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_26" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_26" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_27" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_27" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_28" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_28" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_29" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_29" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>  
                            <tr>
                                <td style="border:0px">
                                    <text id="dimLbl_30" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <select id="dimCommentArea_30" hidden="true">
                                        <option value="0">N/A</option>
                                        <option value="1">Self Reported</option>
                                        <option value="2">Diagnosed</option> 
                                    </select>                                    
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>                              
                            <tr>
                                <td style="border:0px">
                                    <text id="totalcountLbl" hidden="true"></text> 
                                </td>
                                <td style="border:0px">
                                    <textarea id="totalCountArea" hidden="true"> </textarea>                                   
                                    <!--<textarea id="dim4CommentArea" hidden="true"></textarea>-->
                                </td> 
                            </tr>                            
                            <tr>

                            </tr>

                            <!-- <tr>
                                 <td>
                                     <textarea id="datatoFuseki"></textarea>
                                 </td>
                             </tr>-->

                    </div>
        </table>
        <br/>
        <br/>
        <button id="SubmitAnnotationsBtn">Submit Annotations</button>

    </td>
</tr>
<!--<tr>
    <td>
        <div class="footer">
<jsp:text>Please choose a Policy Model:</jsp:text>
&nbsp;
<select id="accessPolicyList" style="width:200px">
    <option> --- </option>
    <option>An access policy having as measure occurrence of cancer and measure values is more than 60  authorizing access to users working in the area of oncology and coming from Cyprus</option>
    <option>An access policy having as dimension drug and dimension values include aspirin and authorizing access to users working in the area of oncology</option>  
    <option>An access policy having as measure the number of diabetes incidences authorizing only John Smith</option>
    <option>An access policy having as dimension the gender and coming from a study project created in clinical sites located in Greece authorizing only users that their role is defined as 'ClinicalTrialCordinator'</option>
    <option>An access policy having as dimension dyslipidemia authorizing end-users working in ZEINCRO</option>
</select>
<br/>
<button>Apply Policy</button>
</div>
</td>
</tr>-->
</table>     
<br/>
<textarea id="debugTextArea" ></textarea>
<script>

    var fileSubmitted = false
    var nodesArray = new Array();

    var urlbase_generic = "http://192.168.1.202:4040/zeincrorep";
//var urlbase     = "http://62.38.242.7:4040";
    var urlbase = "http://192.168.1.202:4040";
    var datasetName = "zeincrorep";

    var cubeDatainRDFformat;

    var conceptTriples = new Array();

    function handleCheckBoxClick(cb)
    {
        if (cb.checked) {
            //alert(cb.name);
            conceptTriples.push(cb.name);
        }
    }

    function loaddata()
    {
        var rowTriple
        var rowTripleDims
        var data = ""
        var numOfDims = 0;
        var DataCubeURI = "";
        var DataCubeURITop = "";
        //alert(cubeDatainRDFformat);
        var subStr = cubeDatainRDFformat.split('\n')
        for (var i = 0; i < subStr.length; i++)
        {
            var topTriple = subStr[i].split('dcterms:date');
            if (topTriple.length > 1) {
                DataCubeURITop = topTriple[0];
                data += DataCubeURITop + " <http://purl.org/dc/terms/date> " + topTriple[1].replace(';', '').replace(' ', '') + ".\n";
            }

            var publisherTriple = subStr[i].split('dcterms:publisher')
            if (publisherTriple.length > 1) {
                data += DataCubeURITop + " <http://purl.org/dc/terms/publisher> " + publisherTriple[1].replace(';', '').replace(' ', '') + ".\n";
            }

            var structureTriple = subStr[i].split('qb:structure')
            if (structureTriple.length > 1) {
                data += DataCubeURITop + " <http://purl.org/linked-data/cube#structure> " + structureTriple[1].replace(';', '').replace(' ', '') + ".\n";
            }

            var labelTriple = subStr[i].split('rdfs:label')
            if (labelTriple.length > 1) {
                data += DataCubeURITop + " <http://www.w3.org/2000/01/rdf-schema#label> " + labelTriple[1].replace(';', '').replace(' ', '') + ".\n";
                data += DataCubeURITop + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/linked-data/cube#DataSet>.\n";
            }

            rowTriple = subStr[i].split('qb:component')
            if (rowTriple.length > 1) {
                DataCubeURI = rowTriple[0];
                var cubeDims = rowTriple[1].split(';')
                cubeDims = cubeDims[0].split(',')
                //alert (cubeDims)
                for (var j = 0; j < cubeDims.length; j++) {
                    //alert(cubeDims[j] + " dimCommentArea_"+j)
                    data += rowTriple[0] + " <http://purl.org/linked-data/cube#component> " + cubeDims[j] + " . \n"
                    //dimCommentArea_3
                    if (j < cubeDims.length - 1) {
                        var e = document.getElementById("dimCommentArea_" + j);
                        var selectedComment = e.options[e.selectedIndex].value;
                        if (selectedComment == "1") {
                            data += cubeDims[j];
                            data += "<http://linked2safety-project.eu/DataCube/variableComment> ";
                            data += "\"Self Reported\"";
                            data += ".\n";
                        } else if (selectedComment == "2") {
                            data += cubeDims[j];
                            data += "<http://linked2safety-project.eu/DataCube/variableComment> ";
                            data += "\"Diagnosed\"";
                            data += ".\n";
                        }
                    }
                    //data += rowTriple[0] + "<http://purl.org/linked-data/cube#dimension> " + cubeDims[j] + ".\n";
                }
                data += rowTriple[0] + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + " <http://purl.org/linked-data/cube#DataStructureDefinition> . \n"
            }
            rowTripleDims = subStr[i].split('qb:dimension')
            if (rowTripleDims.length > 1) {
                numOfDims++;
                var dimStr = rowTripleDims[1].split(':')
                dimStr[1] = dimStr[1].substring(0, dimStr[1].length - 1)
                //alert(dimStr[1]);
                //alert(dimStr[1])
                data += rowTripleDims[0] + "<http://purl.org/linked-data/cube#dimension> " + "<http://www.linked2safety-project.eu/properties/" + dimStr[1] + "> . \n"
            }
        }

        var subpatt = ''
        for (var k = 0; k < numOfDims; k++) {
            subpatt += '*(?:\r\n|\r|\n).'
        }
        //var patt1 = /\n.*qb:dataSet.*\n.*\n.*\n.*\n.*\n.*/g;
        var patt1 = '\n.*qb:dataSet.' + subpatt + '*(?:\r\n|\r|\n).*'

        var result = cubeDatainRDFformat.match(new RegExp(patt1, 'g'));
        for (var i = 0; i < result.length; i++) {
            var tokens = result[i].split(';')
            var firstRowTokens = tokens[0].split('qb:dataSet')
            data += firstRowTokens[0] + "<http://purl.org/linked-data/cube#dataSet>" + firstRowTokens[1] + " .\n"
            //alert(numOfDims);
            for (var j = 1; j < numOfDims + 1; j++) {
                var varEntryTokens = tokens[j].split(' ')
                var varNameTokens = varEntryTokens[0].split(':')
                var varName = varNameTokens[1]
                //alert(varName)
                data += firstRowTokens[0] + "<http://www.linked2safety-project.eu/properties/" + varName + "> " + varEntryTokens[1] + " .\n"
            }
            var casesEntryTokens = tokens[numOfDims + 1].split(' ')
            var casesNumUnparsed = casesEntryTokens[1]
            var casesNumParsed = casesNumUnparsed.split('^^')
            var casesNum = casesNumParsed[0]
            data += firstRowTokens[0] + "<http://purl.org/linked-data/sdmx/2009/measure#Cases> " + casesNum + " .\n"
            data += firstRowTokens[0] + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/linked-data/cube#Observation>.\n"
            //alert(tokens[numOfDims+1])
            //document.getElementById("datatoFuseki").innerHTML = data;
        }

        //alert(data)
        //var data = cubeDatainRDFformat;
        //alert(data);
        //var topCommentAnnotation;

        /*data = "<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://www.w3.org/1999/02/22-rdf-syntax-nstype> <http://xmlns.com/foaf/0.1/Document> .\n" +
         "<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/terms/title> \"N-Triples\"@en-US .\n" +
         "<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://xmlns.com/foaf/0.1/maker> _:art . \n" +
         "<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://xmlns.com/foaf/0.1/maker> _:dave . \n" +
         "_:art <http://www.w3.org/1999/02/22-rdf-syntax-nstype> <http://xmlns.com/foaf/0.1/Person> . \n" +
         "_:art <http://xmlns.com/foaf/0.1/name> \"Art Barstow\". \n" +
         "_:dave <http://www.w3.org/1999/02/22-rdf-syntax-nstype> <http://xmlns.com/foaf/0.1/Person> . \n" +
         "_:dave <http://xmlns.com/foaf/0.1/name> \"Dave Beckett2\".\n";*/
        //var data = cubeDatainRDFformat;
        /*var data = "<http://www.linked2safety-project.eu/fake/data-cube/e7a98633407fd1c5d362915e146f49b7/1> <http://purl.org/dc/terms/date> \"2013_09_17\".\n" +
         "<http://www.linked2safety-project.eu/fake/data-cube/e7a98633407fd1c5d362915e146f49b7/1> <http://purl.org/dc/terms/publisher> \"UCYfakeprovdeddatacubes\".\n" +
         "<http://www.linked2safety-project.eu/fake/data-cube/e7a98633407fd1c5d362915e146f49b7/1> <http://www.w3.org/2000/01/rdf-schema#label> \"SNP\".\n" +
         "<http://www.linked2safety-project.eu/fake/data-cube/e7a98633407fd1c5d362915e146f49b7/1> <http://purl.org/linked-data/cube#structure> <http://www.linked2safety-project.eu/fake/dc/e7a98633407fd1c5d362915e146f49b7/>."*/



        //formdata = "<http://www.linked2safety-project.eu/fake/data-cube/dim/06aadea286561e5d8079d1af8412e0bc/0> <http://www.linked2safety-project.eu/f> <http://www.linked2safety-project.eu/p>.";


        //data+="\n<http://linked2safety-project.eu/providerA/dc/_cube_id_> \t<http://linked2safety-project.eu/hasStudyProperties> \t<http://linked2safety-project.eu/studyProperty/multicenter>";
        //data+="\n<http://linked2safety-project.eu/providerA/dc/_cube_id_> \t<http://linked2safety-project.eu/hasStudyProperties> \t<http://linked2safety-project.eu/studyProperty/randomized>";


        //append concepts
        for (var i = 0; i < conceptTriples.length; i++) {
            data += DataCubeURI;
            data += "<http://www.w3.org/1999/02/22-rdf-syntax-ns#seeAlso> ";
            data += "<" + conceptTriples[i] + ">";
            data += ".\n";
            //alert(data);
        }
        /*                                   <textarea id="TopLevelCommentArea"></textarea>
         <textarea id="cubeTitleArea"></textarea>
         <textarea id="cubeDescriptionTitleArea"></textarea>
         <textarea id="cubeProviderTitleArea"></textarea>
         <textarea id="cubeCreatorArea"></textarea>
         <textarea id="cubestudyProjectTitleArea"></textarea>
         <textarea id="cubeStudyProjectDescriptionArea"></textarea>
         <textarea id="cubeStudyProjectSponsorArea"></textarea>
         <textarea id="cubeStudyClinicalSiteArea"></textarea>
         <textarea id="cubesStudyProjectCoordinatorArea"></textarea>*/

        if (document.getElementById("TopLevelCommentArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasComment> ";
            data += "\"" + document.getElementById("TopLevelCommentArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeTitleArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasTitle> ";
            data += "\"" + document.getElementById("cubeTitleArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeDescriptionTitleArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasDescription> ";
            data += "\"" + document.getElementById("cubeDescriptionTitleArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeProviderTitleArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasProvider> ";
            data += "\"" + document.getElementById("cubeProviderTitleArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeCreatorArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasCreator> ";
            data += "\"" + document.getElementById("cubeCreatorArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubestudyProjectTitleArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProject> ";
            data += "\"" + document.getElementById("cubestudyProjectTitleArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeStudyProjectDescriptionArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProjectDescription> ";
            data += "\"" + document.getElementById("cubeStudyProjectDescriptionArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeStudyProjectSponsorArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProjectSponsor> ";
            data += "\"" + document.getElementById("cubeStudyProjectSponsorArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubeStudyClinicalSiteArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyClincalSiteArea> ";
            data += "\"" + document.getElementById("cubeStudyClinicalSiteArea").value + "\"";
            data += ".\n";
        }

        if (document.getElementById("cubesStudyProjectCoordinatorArea").value == '') {
            //alert ("null");
        } else {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProjectCoordinatorArea> ";
            data += "\"" + document.getElementById("cubesStudyProjectCoordinatorArea").value + "\"";
            data += ".\n";
        }

        /*  Multi Center <input type="checkbox" id="multiCenterChkBox" >
         <br/>
         Randomized   <input type="checkbox" id="randomizedChkBox" >
         <br/>
         Double Blind <input type="checkbox" id="doubleBlindChkBox" >
         <br/>
         Double Dummy <input type="checkbox" id="doubleDummyChkBox" >
         <br/>
         Cross Over   <input type="checkbox" id="crossOverChkBox" >
         <br/>
         Single Dose  <input type="checkbox" id="singleDoseChkBox" >  */
        if (document.getElementById("multiCenterChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/multicenter>";
            data += ".\n";
        }

        if (document.getElementById("randomizedChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/randomized>";
            data += ".\n";
        }

        if (document.getElementById("doubleBlindChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/doubleBlind>";
            data += ".\n";
        }

        if (document.getElementById("doubleDummyChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/doubleDummy>";
            data += ".\n";
        }

        if (document.getElementById("crossOverChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/crossOver>";
            data += ".\n";
        }

        if (document.getElementById("singleDoseChkBox").checked) {
            data += DataCubeURI;
            data += "<http://linked2safety-project.eu/DataCube/hasStudyProperties> ";
            data += "<http://linked2safety-project.eu/studyProperty/singleDose>";
            data += ".\n";
        }
        //alert (data);
        document.getElementById("debugTextArea").innerHTML = data;
        storeRDFData("http://www.in.gr2", data);
    }

    function storeRDFData(graphName, rdfData)
    {
        var namedgId = Math.floor(new Date() / 1000);
        //alert(String(namedgId));

        $.ajax({
            url: urlbase + "/" + datasetName + "/data" + "?graph=" + encodeURIComponent('http://www.linked2safety-project.eu/graph/' + String(namedgId)),
            type: "POST",
            data: rdfData,
            processData: false,
            contentType: false,
            success: function(res) {
                document.getElementById("response").innerHTML = res;
                alert("Annotations sent to triple store! Named graph: " + String(namedgId));
            }
        });
        alert("Sent data to triple store\n");
        //location.reload();
    }


    function readSingleFile(evt) {
        fileSubmitted = true;
        var f = evt.target.files[0];
        var data = [];

        if (f) {
            var r = new FileReader();
            r.onload = function(e) {
                var contents = e.target.result;
                var contentParts = contents.split('\n');

                var headers = contentParts[0].split(',');
                var nodeObject = new Object()
                nodeObject.label = headers[0];
                nodesArray.push(nodeObject);

            }

            r.readAsText(f);
            jQuery.ready();
        } else {
            alert("Failed to load file");
        }
    }


    function padCell(headerStr, cellLength)
    {
        var paddStr = "";
        for (var i = 0; i < (headerStr.length - cellLength); i++)
        {
            paddStr += " "
        }
        return paddStr
    }


    function create_subCube(contentParts, variable_indexes)
    {
        //crate datacube from contents
        var header = contentParts[0].split(',')
        //alert(header)

        //create header
        var headerStr = ""
        for (var i = 0; i < header.length - 1; i++)
        {
            if ($.inArray(i, variable_indexes) >= 0)
            {
                continue
            } else {
                headerStr += header[i] + ","
            }
        }

        headerStr += "number\n"
        //aggregate data
        var subCubeRow = new Object()
        for (var i = 1; i < contentParts.length; i++)
        {
            //get row cells
            var contentRow = contentParts[i].split(',')

            var varIndexStr = ""
            for (var j = 0; j < contentRow.length - 1; j++)
            {

                //build subcube index
                if ($.inArray(j, variable_indexes) >= 0)
                {
                    continue
                } else {
                    varIndexStr += contentRow[j] + ","
                }

            }
            //alert(varIndexStr)
            if (varIndexStr.length) {
                if (isNaN(subCubeRow[varIndexStr])) {
                    subCubeRow[varIndexStr] = parseInt(contentRow[contentRow.length - 1].replace(/\s/g, ''))
                } else {
                    //alert(contentRow[contentRow.length-1])
                    //subCubeRow[varIndexStr] += parseInt(contentRow[contentRow.length-1].split('\r').join(''))
                    subCubeRow[varIndexStr] += parseInt(contentRow[contentRow.length - 1].replace(/\s/g, ''))
                }
            }

        }

        var subCubeStr = headerStr
        var key
        var maxLines = 8;
        var countL = 0;
        for (key in subCubeRow) {
            countL++
            if (countL > maxLines)
                break
            subCubeStr += key
            subCubeStr += subCubeRow[key]
            subCubeStr += '\n'
        }
        //alert(JSON.stringify(subCubeRow))
        //alert (headerStr)
        //alert(subCubeStr)
        return subCubeStr
    }

    (function($) {

        var Renderer = function(canvas) {
            var canvas = $(canvas).get(0)
            var ctx = canvas.getContext("2d");
            var particleSystem

            var that = {
                init: function(system) {

                    particleSystem = system


                    particleSystem.screenSize(canvas.width, canvas.height)
                    particleSystem.screenPadding(80)


                    that.initMouseHandling()
                },
                redraw: function() {


                    ctx.fillStyle = "white"
                    ctx.fillRect(0, 0, canvas.width, canvas.height)

                    particleSystem.eachEdge(function(edge, pt1, pt2) {

                        ctx.strokeStyle = "rgba(0,0,0, .333)"
                        ctx.lineWidth = 1
                        ctx.beginPath()
                        ctx.moveTo(pt1.x, pt1.y)
                        ctx.lineTo(pt2.x, pt2.y)
                        ctx.stroke()

                        ctx.fillStyle = "red"
                        ctx.font = '10px Sans-Serif'
                        ctx.fillText("By " + edge.data.label, (pt1.x + pt2.x) / 2, (pt1.y + pt2.y) / 2)
                    })

                    particleSystem.eachNode(function(node, pt) {

                        var w = 10
                        ctx.fillStyle = (node.data.alone) ? "orange" : "black"

                        ctx.fillStyle = "black"
                        ctx.font = '48pt'
                        var rows = node.data.label.split('\n')
                        var headers = rows[0];
                        ctx.fillText(headers, pt.x - w, pt.y + 8)


                        var headerCells = rows[0].split(',')
                        for (var i = 1; i < rows.length; i++) {
                            var dataRowStr = ""
                            var rowCells = rows[i].split(',')

                            for (var j = 0; j < rowCells.length; j++)
                            {
                                dataRowStr += padCell(headerCells[j], rowCells[j].length)
                                dataRowStr += rowCells[j]
                                if (j != rowCells.length - 1) {
                                    dataRowStr += ","
                                }
                            }
                            ctx.fillText(dataRowStr, pt.x - w, pt.y + (i + 1) * 8)
                        }
                    })
                },
                initMouseHandling: function() {
                    // no-nonsense drag and drop (thanks springy.js)
                    var dragged = null;

                    // set up a handler object that will initially listen for mousedowns then
                    // for moves and mouseups while dragging
                    var handler = {
                        clicked: function(e) {
                            var pos = $(canvas).offset();
                            _mouseP = arbor.Point(e.pageX - pos.left, e.pageY - pos.top)
                            dragged = particleSystem.nearest(_mouseP);

                            if (dragged && dragged.node !== null) {
                                // while we're dragging, don't let physics move the node
                                dragged.node.fixed = true
                            }

                            $(canvas).bind('mousemove', handler.dragged)
                            $(window).bind('mouseup', handler.dropped)

                            return false
                        },
                        dragged: function(e) {
                            var pos = $(canvas).offset();
                            var s = arbor.Point(e.pageX - pos.left, e.pageY - pos.top)

                            if (dragged && dragged.node !== null) {
                                var p = particleSystem.fromScreen(s)
                                dragged.node.p = p
                            }

                            return false
                        },
                        dropped: function(e) {
                            if (dragged === null || dragged.node === undefined)
                                return
                            if (dragged.node !== null)
                                dragged.node.fixed = false
                            dragged.node.tempMass = 1000
                            dragged = null
                            $(canvas).unbind('mousemove', handler.dragged)
                            $(window).unbind('mouseup', handler.dropped)
                            _mouseP = null
                            return false
                        }
                    }

                    // start listening
                    $(canvas).mousedown(handler.clicked);

                }

            }
            return that
        }


        $(document).ready(function() {
            if (!fileSubmitted) {
                //alert('not entered')
                return;
            }


        })

        $('input[type=file]').change(function(evt) {

            var sys = arbor.ParticleSystem(1000, 600, 0.5) // create the system with sensible repulsion/stiffness/friction
            sys.parameters({
                stiffness: 1000,
                repulsion: 50,
                gravity: false,
                dt: 0.015
            });
            sys.parameters({gravity: true}) // use center-gravity to make the graph settle nicely (ymmv)
            sys.renderer = Renderer("#viewport") // our newly created renderer will have its .init() method called shortly by sys...

            fileSubmitted = true
            var f = evt.target.files[0]
            var data = []
            var contents
            var contentParts = []

            if (f) {
                var r = new FileReader();
                r.onload = function(e) {



                    contents = e.target.result

                    cubeDatainRDFformat = contents;

                    var contentLines = contents.split('\n');
                    //alert(contentLines);

                    contentParts = contents.split('\n')

                    var dataCubeinTextFormat = ""
                    var dataCubeHeaders = ""
                    var firstDataEntryVisited = false
                    for (var i = 10; i < contentParts.length; i++) {
                        if ((contentParts[i].indexOf('l2s-dim') != -1) && (contentParts[i].indexOf('qb:dimension') == -1)) {
                            var m = contentParts[i].match(/"(.*?)"/);
                            //alert(m[1])
                            dataCubeinTextFormat += (m[1] + ',')
                            if (!firstDataEntryVisited) {
                                var v = contentParts[i].match(/:(.*?) /);
                                dataCubeHeaders += (v[1] + ',')
                            }
                            //alert (v[1])
                        }
                        //alert(dataCubeHeaders);

                        if ((contentParts[i].indexOf('sdmx-measure:Cases') != -1) && (contentParts[i].indexOf('qb:measure') == -1)) {
                            var m = contentParts[i].match(/"(.*?)"/);
                            //alert(m[1])
                            dataCubeinTextFormat += (m[1] + '\n')
                            if (!firstDataEntryVisited) {
                                firstDataEntryVisited = true
                                dataCubeHeaders += ('number\n')
                            }

                        }
                    }

                    //alert(dataCubeHeaders)
                    //alert (dataCubeinTextFormat)
                    //alert(dataCubeHeaders + dataCubeinTextFormat )

                    contentParts = (dataCubeHeaders + dataCubeinTextFormat).split('\n');
                    //alert(contentParts)
                    //alert(contentParts[0]);
                    var headers = [];
                    for (var i = 10; i < contentLines.length; i++) {
                        var rowTripleDims = contentLines[i].split('qb:dimension')
                        if (rowTripleDims.length > 1) {
                            //alert("dim");
                            //numOfDims++;
                            var dimStr = rowTripleDims[1].split(':');
                            dimStr[1] = dimStr[1].replace(/(?:\r\n|\r|\n)/g, '').replace('.', '');
                            //alert(dimStr[1]);

                            headers.push(dimStr[1]);
                            ;
                        }
                    }
                    //var headers = contentParts[0].split(',')
                    //alert (headers);
                    for (var i = 0; i < 31; i++) {
                        document.getElementById("dimLbl_" + i).innerHTML = "";
                        document.getElementById("dimLbl_" + i).hidden = true;
                        document.getElementById("dimCommentArea_" + i).hidden = true;
                        document.getElementById("dimCommentArea_" + i).options.selectedIndex = 0;
                    }
                    for (var i = 0; i < headers.length; i++) {
                        document.getElementById("dimLbl_" + i).innerHTML = headers[i] + ": ";
                        document.getElementById("dimLbl_" + i).hidden = false;
                        document.getElementById("dimCommentArea_" + i).hidden = false;
                    }

                    for (var i = 0; i < 37; i++) {
                        document.getElementById("ch_" + i).checked = false;
                    }
                    //document.getElementById("dimLbl_" + i).innerHTML = headers[0]+": ";

                    var size = headers.length - 1;
                    sys.addNode('base', {alone: true, mass: .25, label: (dataCubeHeaders + dataCubeinTextFormat)})
                    //sys.addNode('all', {label:'ALL'})
                    size = Math.max(size, 4);
                    for (var i = 0; i < size; i++) {
                        var vars = new Array()
                        vars.push(i)
                        var contentsStr = create_subCube(contentParts, vars)
                        sys.addNode(i, {alone: true, mass: .25, label: contentsStr})
                        sys.addEdge('base', i, {label: headers[i]})
                        for (var j = 1; j < size; j++)
                        {
                            //if (j != i) {
                            vars.push(j)
                            //alert(vars)
                            contentsStr = create_subCube(contentParts, vars)
                            sys.addNode(i + 10 * j + 1, {alone: true, mass: .25, label: contentsStr})
                            sys.addEdge(i, i + 10 * j + 1, {label: headers[i] + " " + headers[j]})
                            //sys.addEdge(i+10*j+1, 'all', {label:'aggregation'})
                            vars.pop()
                            //} else {

                            //}
                        }
                    }
                    //alert(headers[0]);

                }

                r.readAsText(f);
            } else {
                alert("Failed to load file")
            }

            sys.graft()
        })

    })(this.jQuery)

//document.getElementById('fileinput').addEventListener('change', readSingleFile, false);  
    document.getElementById('SubmitAnnotationsBtn').addEventListener('click', loaddata, false);
</script>
</body>
</html>
