"""
Text Helpers
"""
# Last synced with Rails copy at Revision 4595 on Aug 19th, 2006.
# Purposely left out sanitize, should be included at some point likely using
# BeautifulSoup.

from routes import request_config
from webhelpers.rails.tags import content_tag, tag_options
import webhelpers.textile as textile
import webhelpers.markdown as markdown
import itertools, re

AUTO_LINK_RE = re.compile("""(<\w+.*?>|[^=!:'"\/]|^)((?:http[s]?:\/\/)|(?:www\.))(([\w]+:?[=?&\/.-]?)*\w+[\/]?(?:\#\w*)?)([\.,"'?!;:]|\s|<|$)""")
    
def iterdict(items):
    return dict(items=items, iter=itertools.cycle(items))

def cycle(*args, **kargs):
    """
    Returns the next cycle of the given list
    
    Everytime ``cycle`` is called, the value returned will be the next item
    in the list passed to it. This list is reset on every request, but can
    also be reset by calling ``reset_cycle()``.
    
    You may specify the list as either arguments, or as a single list argument.
    
    This can be used to alternate classes for table rows::
    
        # In Myghty...
        % for item in items:
        <tr class="<% cycle("even", "odd") %>">
            ... use item ...
        </tr>
        % #endfor
    
    You can use named cycles to prevent clashes in nested loops. You'll
    have to reset the inner cycle, manually::
    
        % for item in items:
        <tr class="<% cycle("even", "odd", name="row_class") %>
            <td>
        %     for value in item.values:
                <span style="color:'<% cycle("red", "green", "blue",
                                             name="colors") %>'">
                            item
                </span>
        %     #endfor
            <% reset_cycle("colors") %>
            </td>
        </tr>
        % #endfor
    """
    if len(args) > 1:
        items = args
    else:
        items = args[0]
    name = kargs.get('name', 'default')
    cycles = request_config().environ.setdefault('railshelpers.cycles', {})
    
    cycle = cycles.setdefault(name, iterdict(items))
    
    if cycles[name].get('items') != items:
        cycle = cycles[name] = iterdict(items)
    return cycle['iter'].next()

def reset_cycle(name='default'):
    """
    Resets a cycle
    
    Resets the cycle so that it starts from the first element in the array
    the next time it is used.
    """
    del request_config().environ['railshelpers.cycles'][name]

def truncate(text, length=30, truncate_string='...'):
    """
    Truncates ``text`` with replacement characters
    
    ``length``
        The maximum length of ``text`` before replacement
    ``truncate_string``
        If ``text`` exceeds the ``length``, this string will replace
        the end of the string
    """
    if not text: return ''
    
    new_len = length-len(truncate_string)
    if len(text) > length:
        return text[:new_len] + truncate_string
    else:
        return text

def highlight(text, phrase, hilighter='<strong class="hilight">\\1</strong>'):
    """
    Highlights the ``phrase`` where it is found in the ``text``
    
    The highlighted phrase will be surrounded by the hilighter, by default::
    
        <strong class="highlight">I'm a highlight phrase</strong>
    
    ``highlighter``
        Defines the highlighting phrase. This argument should be a single-quoted string
        with ``\\1`` where the phrase is supposed to be inserted.
        
    Note: The ``phrase`` is sanitized to include only letters, digits, and spaces before use.
    """
    if not phrase or not text:
        return text
    return re.sub(re.compile('(%s)' % re.escape(phrase)), hilighter, text, re.I)

def excerpt(text, phrase, radius=100, excerpt_string="..."):
    """
    Extracts an excerpt from the ``text``
    
    ``phrase``
        Phrase to excerpt from ``text``
    ``radius``
        How many surrounding characters to include
    ``excerpt_string``
        Characters surrounding entire excerpt
    
    Example::
    
        >>> excerpt("hello my world", "my", 3)
        "...lo my wo..."
    """
    if not text or not phrase:
        return text
    
    pat = re.compile('(.{0,%s}%s.{0,%s})' % (radius, re.escape(phrase), radius))
    match = pat.search(text)
    if not match:
        return ""
    return excerpt_string + match.expand(r'\1') + excerpt_string

def word_wrap(text, line_width=80):
    """
    Word wrap long lines to ``line_width``
    """
    text = re.sub(r'\n', '\n\n', text)
    return re.sub(r'(.{1,%s})(\s+|$)' % line_width, r'\1\n', text).strip()

def simple_format(text):
    """
    Returns ``text`` transformed into HTML using very simple formatting rules
    
    Surrounds paragraphs with ``<p>`` tags, and converts line breaks into ``<br />``
    Two consecutive newlines(``\\n\\n``) are considered as a paragraph, one newline (``\\n``) is
    considered a linebreak, three or more consecutive newlines are turned into two newlines.
    """
    text = re.sub(r'(\r\n|\n|\r)', r'\n', text)
    text = re.sub(r'\n\n+', r'\n\n', text)
    text = re.sub(r'(\n\n)', r'</p>\1<p>', text)
    text = re.sub(r'([^\n])(\n)([^\n])', r'\1\2<br />\3', text)
    text = content_tag("p", text).replace('</p><p></p>', '</p>')
    text = re.sub(r'</p><p>', r'</p>\n<p>', text)
    return text

def auto_link(text, link="all", **href_options):
    """
    Turns all urls and email addresses into clickable links. 
    
    ``link``
        Used to determine what to link. Options are "all", "email_addresses", or "urls"
    
    Example::
    
        >>> auto_link("Go to http://www.planetpython.com and say hello to guido@python.org")
        'Go to <a href="http://www.planetpython.com">http://www.planetpython.com</a> and say
        hello to <a href="mailto:guido@python.org">guido@python.org</a>'
    """
    if not text:
        return ""
    if link == "all":
        return auto_link_urls(auto_link_email_addresses(text), **href_options)
    elif link == "email_addresses":
        return auto_link_email_addresses(text)
    else:
        return auto_link_urls(text, **href_options)

def auto_link_urls(text, **href_options):
    extra_options = tag_options(**href_options)
    def handle_match(matchobj):
        all = matchobj.group()
        a, b, c, d = matchobj.group(1,2,3,5)
        if re.match(r'<a\s', a, re.I):
            return all
        text = b + c
        if b == "www.":
            b = "http://www."
        return '%s<a href="%s%s"%s>%s</a>%s' % (a, b, c, extra_options, text, d)
    return re.sub(AUTO_LINK_RE, handle_match, text)

def auto_link_email_addresses(text):
    def fix_email(match):
        text = matchobj.group()
        return '<a href="mailto:%s>%s</a>' % (text, text)
    return re.sub(r'([\w\.!#\$%\-+.]+@[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)+)', r'<a href="mailto:\1">\1</a>', text)

def strip_links(text):
    """
    Turns all links into words
    
    Example::
    
        >>> strip_links("<a href="something">else</a>")
        "else"
    """
    return re.sub(r'<a\b.*?>(.*?)<\/a>', r'\1', text, re.M)

def textilize(text, sanitize=False):
    """Format the text with Textile formatting
    
    This function uses the `PyTextile library <http://dealmeida.net/>`_ which is included with WebHelpers.
    
    Additionally, the output can be sanitized which will fix tags like <img />,
    <br /> and <hr /> for proper XHTML output.
    
    """
    texer = textile.Textiler(text)
    return texer.process(sanitize=sanitize)

def markdown(text):
    """Format the text with MarkDown formatting
    
    This function uses the `Python MarkDown library <http://www.freewisdom.org/projects/python-markdown/>`_
    which is included with WebHelpers.
    
    """
    return markdown.markdown(text)

__all__ = ['cycle', 'reset_cycle', 'truncate', 'highlight', 'excerpt', 'word_wrap', 'simple_format',
           'auto_link', 'strip_links', 'textilize', 'markdown']
