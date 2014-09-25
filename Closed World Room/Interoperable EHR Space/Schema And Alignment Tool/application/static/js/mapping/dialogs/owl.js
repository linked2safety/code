/*
This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
For details see: http://opensource.org/licenses/GPL-3.0
*/
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