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