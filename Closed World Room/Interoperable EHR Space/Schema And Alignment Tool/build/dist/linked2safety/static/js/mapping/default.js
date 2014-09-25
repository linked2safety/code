function refreshAllOwlJsPlumbConnections(){
  $("#mapped-attributes .list li.mapped").each(function(i, elem){
    jsPlumb.repaint($(elem));
  });
}

function refreshAllRawJsPlumbConnections(){
  $("#raw-attributes .list li.mapped").each(function(i, elem){
    jsPlumb.repaint($(elem));
  });
}

function refreshMappingDisplayText(owlAttribute){
  owlAttribute = $(owlAttribute);
  if (owlAttribute.length>0){
    var mappings = owlAttribute.find(".mappings");
    mappings.find(".mapping").remove();
    owlAttribute.find('input[type="hidden"]').each(function(i, input){
      input = $(input);
      var rawAttribute = $("#" + input.attr("value"));
      if (rawAttribute.length>0){
        mappings.append($("<span/>", {
          "class"   : "mapping"
        })
          .append($("<span/>", {
              "rawAttributeId" : rawAttribute.attr("id"),
              "class" : "remove-mapping ui-icon ui-icon-circle-close",
              "title" : "Remove " + rawAttribute.attr("display-name")
            }))
          .append(rawAttribute.attr("display-name"))
        );
      }
    });
  }
}

function unMapRawAttribute(rawAttribute){
   rawAttribute = $(rawAttribute);
   if (rawAttribute.length>0){
     rawAttribute.removeClass('mapped');
     jsPlumb.removeAllEndpoints(rawAttribute);
     jsPlumb.detachAllConnections(rawAttribute);
     
     var owlAttribute = $('#' + rawAttribute.attr('mappedTo'));
     if (owlAttribute.length>0){
        //owlAttribute.find('input.display-raw-attribute-name').attr('value', null);
        var hiddenSelector = 'input[type="hidden"][value="' + rawAttribute.attr('id') + '"]';
        owlAttribute.find(hiddenSelector).remove();
        if (owlAttribute.find('input[type="hidden"]').length == 0){
          owlAttribute.removeClass("mapped");
        }
        refreshMappingDisplayText(owlAttribute);
     }
     rawAttribute.attr("mappedTo", null);
     return true;
   }
   return false;
}

function unMapOwlAttribute(owlAttribute){
    owlAttribute = $(owlAttribute);
    if (owlAttribute.length>0){
      //owlAttribute.find('input.display-raw-attribute-name').attr('value', null);
      jsPlumb.removeAllEndpoints(owlAttribute);
      jsPlumb.detachAllConnections(owlAttribute);
      owlAttribute.find('input[type="hidden"]').each(function(i, input){
        input = $(input);
        var rawAttribute = $('#' + input.attr("value"));
        if (rawAttribute.length>0){
           rawAttribute.removeClass('mapped');
           rawAttribute.attr("mappedTo", null);
        }
        input.remove();
      });
      if (owlAttribute.find('input[type="hidden"]').length == 0 ){
        owlAttribute.removeClass("mapped");
      }
      refreshMappingDisplayText(owlAttribute);
      
     return true
   }
   return false;
}

function drawLineBetween(rawAttribute, owlAttribute)
{
   rawAttribute = $(rawAttribute);
   owlAttribute = $(owlAttribute);
   if (rawAttribute.length>0 && owlAttribute.length>0){
      var rawEndpoint = jsPlumb.addEndpoint(rawAttribute, { anchor:"RightMiddle",  }, { isSource:true, isTarget:false, endpoint: "Blank", style:{fillStyle:'blue'} } );  
      var owlEndpoint = jsPlumb.addEndpoint(owlAttribute, { anchor:"LeftMiddle" }, { isSource:false, isTarget:true, endpoint: "Blank", style:{fillStyle:'blue'} } );  
      jsPlumb.connect({ 
          source: rawEndpoint,
          target: owlEndpoint,
          connector: [ "Bezier", 270 ],
          paintStyle:       { lineWidth:1, strokeStyle:'gray' },
          hoverPaintStyle:  { lineWidth:3, strokeStyle:'red' },
      });
   }
}

function mapAttributes(rawAttribute, owlAttribute){
   rawAttribute = $(rawAttribute);
   owlAttribute = $(owlAttribute);
   if (rawAttribute.length>0 && owlAttribute.length>0){
      //remove map
      unMapRawAttribute( rawAttribute );
      //unMapOwlAttribute( owlAttribute );
      
      //set map
      var hiddenSelector = 'input[type="hidden"][value="' + rawAttribute.attr('id') + '"]';
      if (owlAttribute.find( hiddenSelector ).length == 0){
        owlAttribute.append( $("<input />", {
                                              "type" : "hidden",
                                              "name" : "owlAttribute[" + owlAttribute.attr('id') + "]",
                                              "value": rawAttribute.attr('id')
                                            }
                              )
          );
      }
      rawAttribute.attr("mappedTo", owlAttribute.attr('id'));
      rawAttribute.addClass("mapped");
      owlAttribute.addClass("mapped");
      refreshMappingDisplayText(owlAttribute);
      return true;
     //owlAttribute.find('input.display-raw-attribute-name').attr('value', rawAttribute.attr('display-name'));
   }
   return false;
}

var objSuggestions = null;
var objOwlSuggestions = null;
 $(function() {
    /*collapse unmapped attributes*/
  $("#mapped-attributes li.mapped-attributes:not(.collapsed):not(:has(li.mapped))")
    .each(
          function(){
            $(this).addClass("collapsed");
            $(this).children("a.expand-collapse-attributes")
            .each(
                  function(){
                    $(this).removeClass("ui-icon-minus");
                    $(this).addClass("ui-icon-plus");
                  }
            );
          }
    );
    
  $("#raw-attributes li.raw-attributes:not(:has(ol.attributes li.mapped))")
    .each(
          function(){
            $(this).addClass("collapsed");
            $(this).children("a.expand-collapse-attributes")
            .each(
                  function(){
                    $(this).removeClass("ui-icon-minus");
                    $(this).addClass("ui-icon-plus");
                  }
            );
          }
    );
          
   $("#button-new").click(function(event){
      $( "#dialog-new" ).dialog("open");
   });
   
   $("#button-edit").click(function(event){
      $( "#dialog-edit" ).dialog("open");
   });
   
   $("#button-save").click(function(event){
      $( "#form-mapped-attributes" ).submit();
   });
   
    $( "#raw-attributes .list" ).on("mouseover", "li:not(.disabled):not(.ui-draggable)", function( event ){
      $(this).draggable({
              cursorAt: {left: -5 },
              helper: "clone",
              cursor: 'move',
              //tolerance: "pointer",
              revert: true,
              accept: "#mapped-attributes .list li",
      });
    });
    
    $('#raw-attributes .list').on( "click", 'a.remove-mappings',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      unMapRawAttribute($(object).closest("li"));
                                      refreshAllOwlJsPlumbConnections();
                                      return false;
                                  });
    
    $("#mapped-attributes .list .attributes").on("mouseover", "li:not(.ui-droppable)", function( event ){
        $(this).droppable({
            accept: "li",
            drop: function( event, ui ) {
                var draggable = $(ui.draggable);
                var droppable = $(this);                      
                
                if (mapAttributes(draggable, droppable)){
                  drawLineBetween(draggable, droppable);
                  refreshAllOwlJsPlumbConnections();
                  event.accept = true;
                }else{
                  event.accept = false;
                }
            }
        });
    });
    
    $('#mapped-attributes .list').on( "click", 'a.remove-mappings',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      unMapOwlAttribute($(object).closest("li"));
                                      refreshAllOwlJsPlumbConnections();
                                      return false;
                                  });
    $("#mapped-attributes .help").tooltip();
    
    $('#mapped-attributes .list').on( "change", 'input.has-range',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      var li = $(object).closest("li");
                                      if (object.attr("checked") == "checked"){
                                        li.addClass("has-range");
                                      }else{
                                        li.removeClass("has-range");
                                        li.find("input.range").val("");
                                      }
                                      refreshAllOwlJsPlumbConnections();
                                      return false;
                                  });
    $('#mapped-attributes .list').on( "click", '.remove-mapping',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      unMapRawAttribute($("#"+object.attr("rawAttributeId")));
                                      refreshAllOwlJsPlumbConnections();
                                      return false;
                                  });

    objSuggestions = new Suggestions('#dialog-add-mapping', {
                                                                events: {
                                                                  'click' : function (event){
                                                                    var li = $(event.target);
                                                                    if (li.is("li")){
                                                                      event.stopPropagation();
                                                                      $(this.currentInput).attr("rawAttributeId", li.attr('rawId'));
                                                                      $(this.currentInput).attr("value", $("#"+li.attr('rawId')).attr("display-name"));
                                                                    }
                                                                  }
                                                                },
                                                                find: function( what ){
                                                                  var search = what.toLowerCase();
                                                                  var newlis = new Array();
                                                                  $( "#raw-attributes .list li:not(.mapped):not(.disabled)")
                                                                          .filter( function(){
                                                                            return $(this).attr('display-name').toLowerCase().indexOf(search)>=0;
                                                                          })
                                                                          .each(function(i, e){
                                                                                var elem = $(e);
                                                                                var html = elem.attr('display-name');
                                                                                var id = elem.attr('id');
                                                                                var title = elem.attr('title');
                                                                                var li = $('<li/>', {
                                                                                          'rawId'       : id,
                                                                                          'displayName' : html,
                                                                                          'title' : title
                                                                                        });
                                                                                li.html(html);
                                                                                newlis.push(li);
                                                                          });
                                                                  return newlis;
                                                                },
                                                                select: function( li, input ){
                                                                  $(input).attr("rawAttributeId", li.attr('rawId'));
                                                                  $(input).attr("value", $("#"+li.attr('rawId')).attr("display-name"));
                                                                  //var owlAttribute =  $(input).closest("li");
                                                                  //mapAttributes(rawAttribute,owlAttribute);
                                                                  //drawLineBetween(rawAttribute, owlAttribute);
                                                                }
                                                            });
    objOwlSuggestions = new Suggestions('#dialog-map-raw-attribute', {
                                                                events: {
                                                                  'click' : function (event){
                                                                    var li = $(event.target);
                                                                    if (li.is("li")){
                                                                      event.stopPropagation();
                                                                      $(this.currentInput).attr("owlAttributeId", li.attr('owlId'));
                                                                      $(this.currentInput).attr("value", $("#"+li.attr('owlId')).attr("display-name"));
                                                                    }
                                                                  }
                                                                },
                                                                find: function( what ){
                                                                  var search = what.toLowerCase();
                                                                  var newlis = new Array();
                                                                  $( "#mapped-attributes .list li:not(.disabled)")
                                                                          .filter( function(){
                                                                            return $(this).attr('display-name').toLowerCase().indexOf(search)>=0;
                                                                          })
                                                                          .each(function(i, e){
                                                                                var elem = $(e);
                                                                                var html = elem.attr('display-name');
                                                                                var id = elem.attr('id');
                                                                                var title = elem.attr('title');
                                                                                var li = $('<li/>', {
                                                                                          'owlId'       : id,
                                                                                          'displayName' : html,
                                                                                          'title' : title
                                                                                        });
                                                                                li.html(html)
                                                                                .append(elem.find(".description").html());
                                                                                newlis.push(li);
                                                                          });
                                                                  return newlis;
                                                                },
                                                                select: function( li, input ){
                                                                  $(input).attr("owlAttributeId", li.attr('owlId'));
                                                                  $(input).attr("value", $("#"+li.attr('owlId')).attr("display-name"));
                                                                  //var owlAttribute =  $(input).closest("li");
                                                                  //mapAttributes(rawAttribute,owlAttribute);
                                                                  //drawLineBetween(rawAttribute, owlAttribute);
                                                                }
                                                            });
     
     $("#mapped-attributes .list li.mapped").each(function(index, element){
      
        $(element).find('input[type="hidden"]').each(function(i, input){
            var rawAttribute = $("#" + $(input).attr("value"));
            if (rawAttribute.length>0){
              mapAttributes(rawAttribute, element);
              drawLineBetween(rawAttribute, element);
            }
        });
    });
    refreshAllOwlJsPlumbConnections();
     
    $( "#dialog-add-mapping" ).dialog(
      {
            autoOpen: false,
            height: 350,
            width: 500,
            resizable: false,
            modal: true,
            buttons: {
               "Accept": function(){
                  var rawId = $("#raw-input-suggestion").attr("rawAttributeId");
                  if (rawId!=null){
                    var rawAttribute = $("#"+rawId);
                    var owlAttribute = $("#"+$(this).attr("owlAttributeId"));
                    if (rawAttribute.length == 0){
                      alert("Invalid RAW attribute!");
                      return;
                    }
                    if (owlAttribute.length == 0){
                      alert("Invalid Owl attribute!");
                      return;
                    }
                    mapAttributes(rawAttribute, owlAttribute);
                    drawLineBetween(rawAttribute, owlAttribute);
                    refreshAllOwlJsPlumbConnections();
                    $(this).dialog('close');
                  }else{
                    alert("No raw attribute selected!");
                  }
               },
               "Cancel": function(){
                  $(this).dialog('close');
               }
            },
      });
    
    $( "#dialog-map-raw-attribute" ).dialog(
      {
            autoOpen: false,
            height: 350,
            width: 500,
            resizable: false,
            modal: true,
            buttons: {
               "Accept": function(){
                  var owlId = $("#owl-input-suggestion").attr("owlAttributeId");
                  if (owlId!=null){
                    var owlAttribute = $("#"+owlId);
                    var rawAttribute = $("#"+$(this).attr("rawAttributeId"));
                    if (rawAttribute.length == 0){
                      alert("Invalid RAW attribute!");
                      return;
                    }
                    if (owlAttribute.length == 0){
                      alert("Invalid Owl attribute!");
                      return;
                    }
                    mapAttributes(rawAttribute, owlAttribute);
                    drawLineBetween(rawAttribute, owlAttribute);
                    refreshAllOwlJsPlumbConnections();
                    $(this).dialog('close');
                  }else{
                    alert("No raw attribute selected!");
                  }
               },
               "Cancel": function(){
                  $(this).dialog('close');
               }
            },
      });
    
        $('#mapped-attributes .list').on( "click", 'a.add-mapping',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      var dialog = $( "#dialog-add-mapping" );
                                      //$("#raw-input-suggestion").attr("value", null);
                                      //$("#raw-input-suggestion").attr("rawAttributeId", null);
                                      dialog.attr("owlAttributeId", object.closest("li").attr("id"));
                                      dialog.dialog("open");
                                      return false;
                                  });
        $('#raw-attributes .list').on( "click", 'a.add-mapping',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      var dialog = $( "#dialog-map-raw-attribute" );
                                      //$("#raw-input-suggestion").attr("value", null);
                                      //$("#raw-input-suggestion").attr("rawAttributeId", null);
                                      dialog.attr("rawAttributeId", object.closest("li").attr("id"));
                                      dialog.dialog("open");
                                      return false;
                                  });
        $('#raw-attributes .list').on( "click", 'a.expand-collapse-attributes',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      if (object.hasClass('ui-icon-minus')){
                                        object.removeClass('ui-icon-minus');
                                        object.addClass('ui-icon-plus');
                                        object.closest("li").addClass("collapsed");
                                      }else{
                                        object.removeClass('ui-icon-plus');
                                        object.addClass('ui-icon-minus');
                                        object.closest("li").removeClass("collapsed");
                                      }
                                      refreshAllRawJsPlumbConnections();
                                      return false;
                                 }
                              );
        $('#mapped-attributes .list').on( "click", 'a.expand-collapse-attributes',
                                 function( event ){
                                      var object = $(event.target);
                                      event.stopPropagation();
                                      if (object.hasClass('ui-icon-minus')){
                                        object.removeClass('ui-icon-minus');
                                        object.addClass('ui-icon-plus');
                                        object.closest("li").addClass("collapsed");
                                      }else{
                                        object.removeClass('ui-icon-plus');
                                        object.addClass('ui-icon-minus');
                                        object.closest("li").removeClass("collapsed");
                                      }
                                      refreshAllOwlJsPlumbConnections();
                                      return false;
                                 }
                                 );
 });