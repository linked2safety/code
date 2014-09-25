$(function(){
   $( "#dialog-owl" ).dialog(
      {
            autoOpen: false,
            height: 300,
            width: 450,
            modal: true,
            buttons: {
               "Accept": function(){
                  $("#form-owl").submit();
               },
               "Cancel": function(){
                  $(this).dialog('close');
               }
            }
      });
});