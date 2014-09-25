# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import wx

def ShowConfirm(message, title = '', icon = wx.ICON_QUESTION):
    result = False
    dlg = wx.MessageDialog(
        None,
        message,
        title,
        wx.YES_NO | icon
        )
    result = dlg.ShowModal() == wx.ID_YES
    dlg.Destroy()
    return result

def ShowInformation(message, title = '', icon = wx.ICON_INFORMATION):
    dlg = wx.MessageDialog(
        None,
        message,
        title,
        wx.OK | icon
        )
    dlg.ShowModal()
    dlg.Destroy()