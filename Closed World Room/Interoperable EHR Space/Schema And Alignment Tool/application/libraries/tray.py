# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
import wx
import sys
import threading
import webbrowser
import configs
import MessageBox

def create_menu_item(menu, label, func):
    item = wx.MenuItem(menu, -1, label)
    menu.Bind(wx.EVT_MENU, func, id=item.GetId())
    menu.AppendItem(item)
    return item
 
class TaskBarIcon(wx.TaskBarIcon):
    def __init__(self):
        super(TaskBarIcon, self).__init__()
        self.set_icon(configs.application_configs["application_icon"])
    
    def CreatePopupMenu(self):
        menu = wx.Menu()
        self.MenuItemOpenHome = create_menu_item(menu, configs.L10N('SysTrayMenuItemOpenHome'), self.on_OpenBrowser)
        self.MenuItemOpenMapping = create_menu_item(menu, configs.L10N('SysTrayMenuItemOpenMapping'), self.on_OpenBrowserMapping )
        self.MenuItemOpenTransform = create_menu_item(menu, configs.L10N('SysTrayMenuItemOpenTransform'), self.on_OpenBrowserTransform )
        menu.AppendSeparator()
        self.MenuItemMedDRA = create_menu_item(menu, configs.L10N('SysTrayMenuItemMedDRA'), self.on_CheckMedDRA )
        self.MenuItemMedDRA.SetCheckable( True)
        self.MenuItemMedDRA.Check( configs.application_configs["useMedDRA"] == "true" )
        menu.AppendSeparator()
        self.MenuItemExit = create_menu_item(menu, configs.L10N('SysTrayMenuItemExit'), self.on_exit)
        return menu
    
    def set_icon(self, path):
        icon = wx.IconFromBitmap(wx.Bitmap(path))
        self.SetIcon(icon, configs.L10N('IconTooltip') % { "name" : configs.application_configs["application_name"], "version" : configs.application_configs["version"] })
        
    def on_OpenBrowser(self, event):
        webbrowser.open("http://{0}:{1}/".format(configs.application_configs["address"], configs.application_configs["port"] ),0,True )
    
    def on_OpenBrowserMapping(self, event):
        webbrowser.open("http://{0}:{1}/mapping/".format(configs.application_configs["address"], configs.application_configs["port"] ),0,True )
    
    def on_OpenBrowserTransform(self, event):
        webbrowser.open("http://{0}:{1}/transform/".format(configs.application_configs["address"], configs.application_configs["port"] ),0,True )
        
    def on_exit(self, event):
        if MessageBox.ShowConfirm( message = configs.L10N("ApplicationCloseConfirmation"), title = configs.L10N("WindowTitleConfirmation") ):
            wx.CallAfter(self.Destroy)
            sys.exit(0)
    
    def on_CheckMedDRA(self, event):
        if configs.application_configs["useMedDRA"] == "true":
            configs.application_configs["useMedDRA"] = "false"
        else:
            configs.application_configs["useMedDRA"] = "true"
            
        configs.saveMedDRA( configs.application_configs["useMedDRA"] )
        
        self.MenuItemMedDRA.Check(configs.application_configs["useMedDRA"] == "true")
        
        MessageBox.ShowInformation( message = configs.L10N("ApplicationClosingForNewSettings"), title = configs.L10N("WindowTitleInformation") )
        
        wx.CallAfter(self.Destroy)
        sys.exit( 0 )

class App(wx.App):
    def OnInit(self):
        self.SetTopWindow(wx.Frame(None, -1))
        TaskBarIcon()

        return True
    
class SysTrayRunner(threading.Thread):
     """Run the MainLoop as a thread. Access the frame with self.frame."""
     def __init__(self, autoStart=True):
         threading.Thread.__init__(self)
         self.setDaemon(1)
         self.start_orig = self.start
         self.start = self.start_local
         self.frame = None #to be defined in self.run
         self.lock = threading.Lock()
         self.lock.acquire() #lock until variables are set
         if autoStart:
             self.start() #automatically start thread on init
     def run(self):
         app = App( redirect = False)
         self.lock.release()
 
         app.MainLoop()
 
     def start_local(self):
         self.start_orig()
         #After thread has started, wait until the lock is released
         #before returning so that functions get defined.
         self.lock.acquire()