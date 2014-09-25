# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
from handlers import home, mapping, transform, browser
handlers = [
    ("/", home.Handler),
    ("/mapping/?", mapping.Handler),
    ("/transform/?", transform.Handler),
    ("/transform/(?P<action>[^\/]+)/?", transform.Handler),
    ("/server-browse/?(?P<action>[^\/]+)/?", browser.Handler),
]