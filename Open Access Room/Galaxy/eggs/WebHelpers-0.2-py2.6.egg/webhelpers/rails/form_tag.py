"""
Form Tag Helpers
"""
# Last synced with Rails copy at Revision 4374 on Aug 19th, 2006.

from urls import confirm_javascript_function
from tags import *
from webhelpers.util import html_escape

def form(url, method="post", multipart=False, **options):
    """
    Starts a form tag that points the action to an url. 
    
    The url options should be given either as a string, or as a ``url()``
    function. The method for the form defaults to POST.
    
    Options:

    ``multipart``
        If set to True, the enctype is set to "multipart/form-data".
    ``method``
        The method to use when submitting the form, usually either "get" or 
        "post". If "put", "delete", or another verb is used, a hidden input
        with name _method is added to simulate the verb over post.
    
    """
    if multipart:
        options["enctype"] = "multipart/form-data"
    
    if callable(url):
        url = url()
    else:
        url = html_escape(url)
    
    method_tag = ""
    
    if method in ['post', 'get']:
        options['method'] = method
    else:
        options['method'] = "post"
        method_tag = tag('input', type="hidden", name_="_method", value=method)
    
    options["action"] = url
    return tag("form", True, **options) + method_tag

start_form = form

def end_form():
    """
    Outputs "</form>"
    """
    return "</form>"

def select(name, option_tags='', **options):
    """
    Creates a dropdown selection box
    
    ``option_tags`` is a string containing the option tags for the select box::

        >>> select("people", "<option>George</option>")
        <select id="people" name="people"><option>George</option></select>
    
    Options:
    
    * ``multiple`` - If set to true the selection will allow multiple choices.
    
    """
    o = { 'name_': name, 'id': name }
    o.update(options)
    return content_tag("select", option_tags, **o)

def text_field(name, value=None, **options):
    """
    Creates a standard text field.
    
    ``value`` is a string that will the contents of the text field will be set to
    
    Options:
    
    * ``disabled`` - If set to True, the user will not be able to use this input.
    * ``size`` - The number of visible characters that will fit in the input.
    * ``maxlength`` - The maximum number of characters that the browser will allow the user to enter.
    
    Remaining keyword options will be standard HTML options for the tag.
    """
    o = {'type': 'text', 'name_': name, 'id': name, 'value': value}
    o.update(options)
    return tag("input", **o)

def hidden_field(name, value=None, **options):
    """
    Creates a hidden field.
    
    Takes the same options as text_field
    """
    return text_field(name, value, type="hidden", **options)

def file_field(name, value=None, **options):
    """
    Creates a file upload field.
    
    If you are using file uploads then you will also need to set the multipart option for the form.
    """
    return text_field(name, value=value, type="file", **options)

def password_field(name="password", value=None, **options):
    """
    Creates a password field
    
    Takes the same options as text_field
    """
    return text_field(name, value, type="password", **options)

def text_area(name, content='', **options):
    """
    Creates a text input area.
    
    Options:
    
    * ``size`` - A string specifying the dimensions of the textarea.
    
    Example::
    
        >>> text_area("body", '', size="25x10")
        <textarea name="body" id="body" cols="25" rows="10"></textarea>
    """
    if 'size' in options:
        options["cols"], options["rows"] = options["size"].split("x")
        del options['size']
    o = {'name_': name, 'id': name}
    o.update(options)
    return content_tag("textarea", content, **o)

def check_box(name, value="1", checked=False, **options):
    """
    Creates a check box.
    """
    o = {'type': 'checkbox', 'name_': name, 'id': name, 'value': value}
    o.update(options)
    if checked:
        o["checked"] = "checked"
    return tag("input", **o)

def radio_button(name, value, checked=False, **options):
    """Creates a radio button."""
    o = {'type': 'radio', 'name_': name, 'id': name, 'value': value}
    o.update(options)
    if checked:
        o["checked"] = "checked"
    return tag("input", **o)

def submit(value="Save changes", name='commit', confirm=None, disable_with=None, **options):
    """Creates a submit button with the text ``value`` as the caption.

    Options:

    * ``confirm`` - A confirm message displayed when the button is clicked.
    * ``disable_with`` - The value to be used to rename a disabled version of the submit
      button.
    """

    if confirm:
        onclick = options.get('onclick', '')
        if onclick.strip() and not onclick.rstrip().endswith(';'):
            onclick += ';'
        options['onclick'] = "%s return %s;" % (onclick, confirm_javascript_function(confirm))

    if disable_with:
        options["onclick"] = "this.disabled=true;this.value='%s';this.form.submit();%s" % (disable_with, options.get("onclick", ''))
    o = {'type': 'submit', 'name_': name, 'value': value }
    o.update(options)
    return tag("input", **o)
      
#def image_submit(source, **options):
#    """Displays an image which when clicked will submit the form"""
#    o = {'type': 'image', 'src': image_path_source) }
#    o.update(options)
#    return tag("input", **o)

__all__ = ['form', 'start_form', 'end_form', 'select', 'text_field', 'hidden_field', 'file_field',
           'password_field', 'text_area', 'check_box', 'radio_button', 'submit']
