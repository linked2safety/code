var serverBrowserNew = null;

function onNewFormSubmit(event){
     validateInputNotEmpty("#raw-file", event);
     validateInputNotEmpty("#raw-file-server", event);
}

$(function(){
   $("#form-new").submit(onNewFormSubmit);
   serverBrowserNew = new ServerBrowser(
                                          {
                                             filterFiles : '.*\\.xls',
                                             dialogTitle : 'Select Excel files',
                                             onSelected  : function(location){
                                                               $("#raw-file").val(null);
                                                               $('#raw-file-server').val(location);
                                                            }
                                          }
                                       );
   $("#tabs-new").tabs();
   $('#raw-file-server-browse').click(function(){
      serverBrowserNew.show();
   });
   $("#raw-file").click(function(){$('#raw-file-server').val(null);});
   
   $( "#dialog-new" ).dialog(
      {
            autoOpen: false,
            height: 350,
            width: 500,
            resizable: false,
            modal: true,
            buttons: {
               "Accept": function(){
                  validateInputNotEmpty("#output-file-server");
                  $("#form-new").submit();
               },
               "Cancel": function(){
                  $(this).dialog('close');
               }
            }
      });
});