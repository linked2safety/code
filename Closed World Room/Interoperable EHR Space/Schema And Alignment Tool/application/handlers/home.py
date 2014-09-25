# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
from handlers import custom
import configs

class Handler( custom.Handler ):
    
    def get(self, **params):
        if self.get_argument("check", None) == "isAlive":
			return ""
        variables = {
		"ApplicationVersion" : configs.application_configs["version"]
	}
        self.render( "home.html", **variables )
    
    def post(self, **params):
        self.get()