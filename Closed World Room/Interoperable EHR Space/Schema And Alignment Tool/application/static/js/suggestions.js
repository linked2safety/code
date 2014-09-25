/*
This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
For details see: http://opensource.org/licenses/GPL-3.0
*/
var Suggestions = function (target,options) {
                        this.currentInput = null;
			this.target=$(target);
			this.options=$.extend(
			{
				events:{},
				select:null,
                                find: function( what ){return new Array()},
                                onHide : null,
				selectedClass:'active'
			}
			,options||{});
			if(this.options.panel==null)
				this.options.panel='suggestion-panel-'+new Date().getTime();
			var obj=$(this.options.panel);
                        
			if(obj.length == 0){
				obj=$('<div/>',
                                      {'class':'suggestions popup_msg popup_search',id:this.options.panel
                                    }).insertAfter( this.target );
			}
			this.options.panel=obj;

			obj=$(this.options.container);			
			
			if(obj.length == 0){
				obj=$('<ul/>',{});
                                this.options.panel.append(obj);
				
				var closeButton = $('<a/>', { 'class': 'close-suggestions', href:'#'});
                                closeButton.bind("click", this.closeSuggestions.bind(this));
                                this.options.panel.append(closeButton);
			}
			this.options.container=obj;

			for(var ev in this.options.events)
				this.options.container.bind(ev,this.options.events[ev].bind(this));

			this.hide();
			this.target.bind('keyup',this.keyup.bind(this));
};

Suggestions.prototype.closeSuggestions = function(event){
    event.stopPropagation();
    this.hide();
    return false;
    
};

Suggestions.prototype.keyup = function(event){
                        event.stopPropagation();
			if(event.keyCode==40){
				this.move('down', event);
				return false;
			}
			if(event.keyCode==38){
				this.move('up', event);
				return false;
			}
/*
			if(event.keyCode==27){
				this.hide();
				return false;
			}
*/
                        
                        if (event.keyCode==13){
                                var lis=this.options.container.find('.'+this.options.selectedClass);
                                if (lis.length == 0){
					lis=this.options.container.children();
                                }
                                if (lis.length>0){
					this.select(event.target, lis.first());
                                }
                                this.hide();
				return false;                
                        }
                        
                        var value = $(event.target).val();
			if (value.length>0){
				if(value.substring(value.length-1)==' '){
					this.search( event.target );
				}else{
					if(this.timmerSuggestOptions!=null)
						clearTimeout(this.timmerSuggestOptions);
					this.timmerSuggestOptions=setTimeout(this.search.bind(this),350, event.target);
				}
			}
			return false;
		},

Suggestions.prototype.move = function(how, event){
        var lis=this.options.container.find('.'+this.options.selectedClass);
        var li=null;
        var select=null;
        if(lis.length>0){
                li=lis.first();
                li.removeClass(this.options.selectedClass);
                if(how=='down'){
                        li=li.next();
                        if (li.length == 0)
                                li=this.options.container.children().first();
                }else{
                        li=li.prev();
                        if (li.length == 0)
                                li=this.options.container.children().last();
                }
                li.addClass(this.options.selectedClass);
                select=li;
        }else{
                lis=this.options.container.children();
                if(lis.length>0){
                        if(how=='down')
                                li=$(lis[0]);
                        else
                                li=$(lis[lis.length-1]);
                        li.addClass(this.options.selectedClass);
                        select=li;
                }
        }
        this.select(event.target, select);
        
}

Suggestions.prototype.select = function( input, selectedItem ){
        if (typeof this.options.select=='function'){
                this.options.select.call(this, $(selectedItem), $(input));
        }else{
                $(input).val( $(selectedItem).html().replace(/<.*?>/g, "").trim());
        }
}

Suggestions.prototype.search = function( input ){
        this.options.container.empty();
        input=$(input);
        var list = this.options.find( input.val() );
        if (list.length>0){
                 for (var i in list){
                        this.options.container.append(list[i]);
                 }
                 this.show( input );
        }
}
Suggestions.prototype.hide = function(){
        if (typeof this.options.onHide=='function'){
                 this.options.onHide.call(this);
        }
        this.currentInput = null;
        this.options.container.empty();
        this.options.panel.hide();
}

Suggestions.prototype.show = function( input ){
        this.currentInput = input;
        this.options.panel.insertAfter( input );
        this.options.panel.show();
}