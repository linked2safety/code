/*
This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
For details see: http://opensource.org/licenses/LGPL-3.0
*/
var checkServerOnlineTicker = null;
function showError(message){
    $( "#dialog-error-encounter" ).find(".error-message").html(message);
    $( "#dialog-error-encounter" ).dialog( "open" );
}

function showLoading(){
     $( "#dialog-page-loading" ).dialog(
      {
            autoOpen: false,
            resizable: false,
            width:200,
            height: 100,
            modal: true,
            closeOnEscape: false,
            open: function(event, ui) { $(this).parent().children().children('.ui-dialog-titlebar-close').hide(); },
            
      });
         
    $( "#dialog-page-loading" ).dialog( "open" );
}

function checkServerOnline(){
	$.ajax({
	 url: "/?check=isAlive",
	 cache: false,
	 async : false,
	 error: function(XMLHttpRequest, textStatus, errorThrown) {
                            window.clearInterval( checkServerOnlineTicker );
                            showError("Could not contact application process.<br/><br/>Please ensure the application is running.");
			}
	});
}

function escapeStr( str ) {
 if( str)
     return str.replace(/([ #;&,.+*~\':"!^$[\]()=>|\/@])/g,'\\$1')
 else
     return str;
}

function validateInputNotEmpty(input, event){
     input = $(input);
     input.removeClass("ui-state-error");
     if ( input.val() == false){
          input.addClass("ui-state-error");
          if (event!=undefined){
               event.preventDefault();
          }
          return false;
     }
     return true;
}