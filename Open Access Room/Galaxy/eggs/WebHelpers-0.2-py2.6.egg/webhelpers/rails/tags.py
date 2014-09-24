"""Tag Helpers"""
# Last synced with Rails copy at Revision 2543 on Aug 20th, 2006.

from webhelpers.util import html_escape
import re

def camelize(name):
    """
    Camelize a ``name``
    """
    def upcase(matchobj):
        return getattr(matchobj.group(0)[1:], 'upper')()
    name = re.sub(r'(_[a-zA-Z])', upcase, name)
    name = name[0].upper() + name[1:]
    return name

def strip_unders(options):
    for x,y in options.iteritems():
        if x.endswith('_'):
            options[x[:-1]] = y
            del options[x]

def tag(name, open=False, **options):
    """
    Create a HTML tag of type ``name``
    
    ``open``
        Set to True if the tag should remain open
    
    All additional keyword args become attribute/value's for the tag. To pass in Python
    reserved words, append _ to the name of the key.
    
    Examples::
    
        >>> tag("br")
        <br />
        >>> tag("input", type="text")
        <input type="text" />
    """
    tag = '<%s%s%s' % (name, (options and tag_options(**options)) or '', (open and '>') or ' />')
    return tag

def content_tag(name, content, **options):
    """
    Create a tag with content
    
    Takes the same keyword args as ``tag``
    
    Examples::
    
        >>> content_tag("p", "Hello world!")
        <p>Hello world!</p>
        >>> content_tag("div", content_tag("p", "Hello world!"), class_="strong")
        <div class="strong"><p>Hello world!</p></div>
    """
    if content is None:
        content = ''
    tag = '<%s%s>%s</%s>' % (name, (options and tag_options(**options)) or '', content, name)
    return tag

def cdata_section(content):
    """
    Returns a CDATA section for the given ``content``.
    
    CDATA sections are used to escape blocks of text containing characters which would
    otherwise be recognized as markup. CDATA sections begin with the string
    ``<![CDATA[`` and end with (and may not contain) the string 
    ``]]>``. 
    """
    if content is None:
        content = ''
    return "<![CDATA[%s]]>" % content

def tag_options(**options):
    strip_unders(options)
    cleaned_options = convert_booleans(dict([(x, y) for x,y in options.iteritems() if y is not None]))
    optionlist = ['%s="%s"' % (x, html_escape(str(y))) for x,y in cleaned_options.iteritems()]
    optionlist.sort()
    if optionlist:
        return ' ' + ' '.join(optionlist)
    else:
        return ''

def convert_booleans(options):
    for attr in ['disabled', 'readonly', 'multiple']:
        boolean_attribute(options, attr)
    return options

def boolean_attribute(options, attribute):
    if options.get(attribute):
        options[attribute] = attribute
    elif options.has_key(attribute):
        del options[attribute]

__all__ = ['tag', 'content_tag', 'cdata_section', 'camelize']
