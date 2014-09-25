var checkServerOnlineTicker = null;
function showError(message){
    $( "#dialog-error-encounter" ).find(".error-message").html(message);
    $( "#dialog-error-encounter" ).dialog( "open" );
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

$(function(){
    $( "#dialog-error-encounter" ).dialog(
      {
            autoOpen: false,
            resizable: false,
            width:400,
            modal: true,
            closeOnEscape: false,
            open: function(event, ui) { $(this).parent().children().children('.ui-dialog-titlebar-close').hide(); },
            
      });
    //$("[title]").tooltip();
    /*
    $("ul.tooltips li[selector]").each(function(index, li){
        li = $(li);
        var selector = li.attr("selector");
            $(selector).hover(function(e) {
                e.stopPropagation();
                return false;
            });
            $(selector).tooltip(
                                {
                                    items: selector,
                                    content: li.html()
                                });
        
    });
    */
	checkServerOnlineTicker = setInterval(checkServerOnline, 3000);
});

