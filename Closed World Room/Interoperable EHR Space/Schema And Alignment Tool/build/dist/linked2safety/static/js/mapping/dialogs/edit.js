var serverBrowserEdit = null;
function onEditFormSubmit(event){
     validateInputNotEmpty("#form-edit-file", event);
     validateInputNotEmpty("#edit-file-server", event);
}

$(function(){
   $("#form-edit").submit(onEditFormSubmit);
   serverBrowserEdit = new ServerBrowser(
                                          {
                                             filterFiles : '.*\\.xml',
                                             dialogTitle : 'Select Mapping(xml) files',
                                             onSelected  : function(location){
                                                               $("#edit-file").val(null);
                                                               $('#edit-file-server').val(location);
                                                            }
                                          }
                                       );
   $("#tabs-edit").tabs();
   $('#edit-file-server-browse').click(function(){
      serverBrowserEdit.show();
   });
   $("#edit-file").click(function(){$('#edit-file-server').val(null);});
   
   $( "#dialog-edit" ).dialog(
      {
            autoOpen: false,
            height: 350,
            width: 500,
            resizable: false,
            modal: true,
            buttons: {
               "Accept": function(){
                  $("#form-edit").submit();
               },
               "Cancel": function(){
                  $(this).dialog('close');
               }
            }
      });
});