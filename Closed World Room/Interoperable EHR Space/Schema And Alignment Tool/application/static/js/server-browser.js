/*
This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
For details see: http://opensource.org/licenses/GPL-3.0
*/
var ServerBrowser = function (options) {
    this.options = $.extend({
        container                   : null,
        select                      : 'file',
        dialogTitle                 : "Browse",
        dialogHeight                : 500,
        dialogWidth                 : 500,
        onSelected                  : null,
        filterFiles                 : null,
        configName                  : null,
        expireCookieMinutes         : 30,
        cookieServerBrowserLocation : "ServerBrowserLocation"
    }
    ,options||{});
    uuid = "-" + new Date().getTime();
    
    if ( this.options.container==null ){
        this.options.container = $("<div/>", {
                                                'class' : "dialog dialog-server-browser",
                                                'title' : this.options.dialogTitle
                                              }
                                 );
    }
    
    this.options.container = $( this.options.container )
    
    this.inputLocation = $("<input/>",{
                                        'type' : 'text',
                                        'class' : 'location-input ui-button ui-widget ui-state-default ui-corner-all '
    });
    
    this.options.container.append(
                                  $("<label/>",{
                                                'class' : 'location-input'
                                                })
                                    .text( "Location:" )
                                    .append(this.inputLocation)
                                  );
    this.goButton = $("<button>", {
                                    'class' :'location-button ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only'
                                    }).append(
                                              $("<span/>", {'class' : 'ui-button-text'}).text("GO")
                                    );
    this.options.container.append( this.goButton );
    
    this.listDrives = $("<ul/>", {
        'class' : 'list-drives'
    })
    
    this.options.container.append( this.listDrives );
    
    this.listEntries = $("<ul/>", {
                                        'class' : 'list-entries clearfix'
                                    });
    
    this.options.container.append( this.listEntries );
    this.listDrives.on('click', 'a',  this.onSelectedDrive.bind(this));
    this.listEntries.on('click', 'a',  this.onSelectedEntry.bind(this));
    this.goButton.click(this.OnGoButtonClicked.bind(this));
    that = this;
    this.options.container.dialog(
      {
            autoOpen: false,
            height: this.options.dialogHeight,
            width: this.options.dialogWidth,
            modal: true,
            resizable: false,
            position: ['top', 100],
            buttons: {
               "Select": this.onBrowseDialogSelectClicked.bind(this),
               "Cancel": function(){
                  $(this).dialog('close');
               }
            }
      });
}
ServerBrowser.prototype.getInputLocationValue = function(){
    return this.inputLocation.val();
}

ServerBrowser.prototype.onBrowseDialogSelectClicked = function(){    
    var selectedLi = this.listEntries.children('.selected');
    var location=null;
    if ( selectedLi.length == 0 ){
        if (this.options.select == 'file'){
            alert("No file selected!");
            return;
        }else{
            location = this.inputLocation.val();
            if (location==null || location.length==0){
                alert("No item selected!");
                return;
            }
        }
    }
    
    if ( this.options.select == 'file' && selectedLi.hasClass('file') == false ){
        alert("No file selected!");
        return;
    }
    
    if (location==null || location.length==0){
        location = selectedLi.attr("location");
    }
    
    if (typeof this.options.onSelected == 'function'){
      this.options.onSelected.call( null, location );
    }
    
    $(this.options.container).dialog('close');
}

ServerBrowser.prototype.compareCaseInsensitive = function(a, b) {
  if (a.toLowerCase() < b.toLowerCase()) return -1;
  if (a.toLowerCase() > b.toLowerCase()) return 1;
  return 0;
}

ServerBrowser.prototype.loadDrives = function(){
    var that = this;
    $.getJSON('/server-browse/drives', function(data){
        if (data != undefined && data.drives!=undefined){
            that.onReceivedDrives(data.drives);
        }
    });
}

ServerBrowser.prototype.loadEntries = function( location ){
    $.getJSON('/server-browse/index', {'location' : location, 'filterFiles' : this.options.filterFiles, 'configName': this.options.configName}, this.onReceivedEntries.bind(this) );
}

ServerBrowser.prototype.onReceivedDrives = function (drives){
    this.listDrives.empty();
    this.options.container.removeClass('has-drives');
    if (drives!=null && drives.length>0){
        drives.sort();
        for (var i in drives){
            this.listDrives.append(
                                   $('<li />')
                                    .append(
                                            $("<a/>", {'href' : '#', 'title' : 'Browse: ' + drives[i]})
                                                .text(drives[i])
                                            )
                                    );
        }
        this.options.container.addClass('has-drives');
    }
}

ServerBrowser.prototype.onReceivedEntries = function (entries){
    if (entries.error!=undefined && entries.error!=null){
        alert(entries.error);
    }else{
        this.listEntries.empty();
        this.inputLocation.val(entries.current);
        if (entries.directories!=null){
            entries.directories.sort( this.compareCaseInsensitive );
            entries.directories.splice(0, 0, entries.parentDirectory);
            for (var i in entries.directories){
                this.listEntries.append(
                                       $('<li />', {'class' : 'directory'})
                                        .append(
                                                $("<a/>", {'href' : '#','class' :'clearfix', 'title' : 'Browse: ' + entries.directories[i]})
                                                    .append($('<span />', {'class': 'ui-icon ui-icon-folder-open'}))
                                                    .append(
                                                            $('<span/>', {'class': 'text'})
                                                            .text(entries.directories[i])
                                                            )
                                            )
                                        .attr("location",entries.current + entries.directories[i] )
                                        );
            }
        }
        if (this.options.select=='file'){
            if (entries.files!=null && entries.files.length>0){
                entries.files.sort( this.compareCaseInsensitive );
                for (var i in entries.files){
                    this.listEntries.append(
                                           $('<li />', {'class' : 'file'})
                                            .append(
                                                    $("<a/>", {'href' : '#','class' :'clearfix', 'title' : 'Select: ' + entries.files[i]})
                                                        .append($('<span />', {'class': 'ui-icon ui-icon-document'}))
                                                        .append(
                                                                $('<span/>', {'class': 'text'})
                                                                .text(entries.files[i])
                                                                )
                                                    )
                                            .attr("location",entries.current + entries.files[i] )
                                            );
                }
            }
        }
    }
}

ServerBrowser.prototype.onSelectedDrive = function ( event ){
    var a = $(event.target);
    if (a.is('a') == false){
        a = $(event.target).find('a');
    }
    if (a.is('a') == false){
        a = $(event.target).closest('a');
    }
    if (a.length>0){
        event.stopPropagation();
        this.loadEntries(a.text());
        return false;
    }
    return true;
}

ServerBrowser.prototype.onSelectedEntry = function ( event ){
    var a = $(event.target);
    if (a.is('a') == false){
        a = $(event.target).find('a');
    }
    
    if (a.is('a') == false){
        a = $(event.target).closest('a');
    }
    if (a.length>0){
        event.stopPropagation();
        var li = a.closest('li');
        if ( li.length>0 ){
            if (li.hasClass("selected")){
                if (li.hasClass("directory")){
                    this.OnSelectedDirectory( li );
                }
                if (li.hasClass("file")){
                    this.OnSelectedFile( li );
                    this.onBrowseDialogSelectClicked();
                }
            }else{
                li.siblings().removeClass("selected");
                li.addClass("selected");
            }
            
            if (li.hasClass("file")){
                this.OnSelectedFile( li );
            }
            return false;
        }
    }
    return true;
}

ServerBrowser.prototype.OnSelectedDirectory = function ( li ){
    this.loadEntries(li.attr("location"));
}

ServerBrowser.prototype.OnSelectedFile = function ( li ){
}

ServerBrowser.prototype.OnGoButtonClicked = function(event){
    this.loadEntries(this.inputLocation.val());
}

ServerBrowser.prototype.show = function(){
    this.loadDrives();
    this.loadEntries( );
    this.options.container.dialog("open");
}

ServerBrowser.prototype.hide = function(){
    this.options.container.dialog("close");
}