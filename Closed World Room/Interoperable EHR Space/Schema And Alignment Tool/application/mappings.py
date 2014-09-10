# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
from handlers import home, mapping, transform, browser
handlers = [
    ("/", home.Handler),
    ("/mapping/?", mapping.Handler),
    ("/transform/?", transform.Handler),
    ("/transform/(?P<action>[^\/]+)/?", transform.Handler),
    ("/server-browse/?(?P<action>[^\/]+)/?", browser.Handler),
]