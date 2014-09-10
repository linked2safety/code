/*
This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
For details see: http://opensource.org/licenses/LGPL-3.0
*/
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
	$( "#dialog-page-loading" ).dialog( "close" );
});

