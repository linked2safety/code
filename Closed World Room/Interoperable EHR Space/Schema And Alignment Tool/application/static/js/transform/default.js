/*
This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
For details see: http://opensource.org/licenses/LGPL-3.0
*/
var progressBar = null;
var progressbarValue = null;
var serverBrowserRaw = null;
var serverBrowserMapping = null;
var serverBrowserOutput = null;

function updateProgressBar(){
     $.getJSON('/transform/getStatus',
               function(data) {
                    if (data!=undefined){
                         onServerProgressRead(data);
                         if (data.isRunning){
                            setTimeout( updateProgressBar,1500 );  
                         }else{
                              onServerProgressFinished(data);
                         }
                    }
               }
     );
}

function startProcessBar(){
     $("#input-information").hide();
     $("#display-information").show();
     progressBar.show();
     progressBar.progressbar({value:0, max: 100 });
     updateProgressBar();
}

function onServerProgressRead(data){
     progressBar.progressbar({value: data.progress });
     if (progressbarValue!=null){
          progressbarValue.html("Progress " + data.progress + "%")
     }
}

function onServerProgressFinished(data){
     setTimeout(function(){
                    $("#input-information").show();
                    $("#display-information").hide();
                    progressBar.hide();
               }
               , 1500);
     if ( data.isFaulted ){
          alert( data.errors );
     }
     if ( data.isCompleted){
          var location = $("#save-file-location");
          if (location.length == 0 ){
               document.location.href="/transform/downloadOutputFile";
          }else{
               alert("Transformed data file saved to: " + location.val());
          }
     }
}

function onFormSubmit(event){
     validateInputNotEmpty("#raw-file", event);
     validateInputNotEmpty("#raw-file-server", event);
     validateInputNotEmpty("#mapping-file", event);
     validateInputNotEmpty("#mapping-file-server", event);
     validateInputNotEmpty("#output-file-server", event);
}

$(function(){
     serverBrowserRaw = new ServerBrowser(
                                       {
                                          filterFiles : '.*\\.xlsx?',
                                          configName  : 'Raw',
                                          dialogTitle : 'Select Excel files',
                                          onSelected  : function(location){
                                                            $("#raw-file").val(null);
                                                            $('#raw-file-server').val(location);
                                                         }
                                       }
                                    );
     
     serverBrowserMapping = new ServerBrowser(
                                          {
                                             filterFiles : '.*\\.xml',
                                             configName  : 'Mapping',
                                             dialogTitle : 'Select Mapping(xml) files',
                                             onSelected  : function(location){
                                                               $("#mapping-file").val(null);
                                                               $('#mapping-file-server').val(location);
                                                            }
                                          }
                                       );
     serverBrowserOutput = new ServerBrowser(
                                          {
                                             select : 'directory',
                                             configName  : 'Output',
                                             dialogTitle : 'Select where to save transformed and aligned data file',
                                             onSelected  : function(location){
                                                               $('#output-file-server').val(location);
                                                            }
                                          }
                                       );
     $('#raw-file').click(function(event){
          $("#raw-file-radio").attr('checked', 'checked');
          $('#raw-file-server').val(null);
     });
     $('#raw-file-server-browser').click(function(event){
          event.preventDefault();
          $("#raw-file-server-radio").attr('checked', 'checked');
          serverBrowserRaw.show();
     });
     
     $('#mapping-file').click(function(event){
          $("#mapping-file-radio").attr('checked', 'checked');
          $('#mapping-file-server').val(null);
     });
     $('#mapping-file-server-browser').click(function(event){
          event.preventDefault();
          $("#mapping-file-server-radio").attr('checked', 'checked');
          serverBrowserMapping.show();
     });
     
     $('#mapping-file').click(function(event){
          $("#choose-download-radio").attr('checked', 'checked');
          $('#output-file-server').val(null);
     });
          
     $('#output-file-server-browser').click(function(event){
          event.preventDefault();
          $("#choose-download-server-radio").attr('checked', 'checked');
          serverBrowserOutput.show();
     });
     
     $("#form-transform").submit(onFormSubmit);
     
     progressBar = $("#progressbar");
     progressbarValue = $("#progressbarValue");
     if (progressBar.length>0){
          startProcessBar();
     }
})