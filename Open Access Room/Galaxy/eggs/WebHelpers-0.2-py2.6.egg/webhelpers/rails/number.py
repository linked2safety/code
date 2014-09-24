"""
Number Helpers
"""
# Last synced with Rails copy at Revision 4537 on Aug 19th, 2006.
import re

def number_to_phone(number, area_code=False, delimiter="-", extension=""):
    """
    Formats a ``number`` into a US phone number string.
    
    The area code can be surrounded by parentheses by setting ``area_code`` to True; default is False
    The delimiter can be set using ``delimiter`` default is "-"
    
    Examples::
    
        >>> number_to_phone(1235551234)
        123-555-1234
        >>> number_to_phone(1235551234, area_code=True)
        (123) 555-1234
        >>> number_to_phone(1235551234, delimiter=" ")
        123 555 1234
        >>> number_to_phone(1235551234, area_code=True, extension=555)
        (123) 555-1234 x 555
    """
    if area_code:
        number = re.sub(r'([0-9]{3})([0-9]{3})([0-9]{4})', r'(\1) \2%s\3' % delimiter, str(number))
    else:
        number = re.sub(r'([0-9]{3})([0-9]{3})([0-9]{4})', r'\1%s\2%s\3' % (delimiter, delimiter), str(number))
    if extension and str(extension).strip():
        number += " x %s" % extension
    return number

def number_to_currency(number, unit="$", precision=2, separator=".", delimiter=","):
    """
    Formats a ``number`` into a currency string. 
    
    ``number``
        Indicates the level of precision
    ``unit``
        Sets the currency type
    ``separator``
        Used to set what the unit separation should be
    ``delimiter``
        The delimiter can be set using the +delimiter+ key; default is ","
    
    Examples::
    
        >>> number_to_currency(1234567890.50)
        $1,234,567,890.50
        >>> number_to_currency(1234567890.506)
        $1,234,567,890.51
        >>> number_to_currency(1234567890.50, unit="&pound;", separator=",", delimiter="")
        &pound;1234567890,50
    """
    if precision < 1:
        separator = ""
    parts = number_with_precision(number, precision).split('.')
    num = unit + number_with_delimiter(parts[0], delimiter)
    if len(parts) > 1:
        num += separator + parts[1]
    return num

def number_to_percentage(number, precision=3, separator="."):
    """
    Formats a ``number`` as into a percentage string. 
    
    ``number``
        Contains the level of precision
    ``separator``
        The unit separator to be used
    
    Examples::
    
        >>> number_to_percentage(100)
        100.000%
        >>> number_to_percentage(100, precision=0)
        100%
        >>> number_to_percentage(302.0574, precision=2)
        302.06%
    """
    number = number_with_precision(number, precision)
    parts = number.split('.')
    if len(parts) < 2:
        return parts[0] + "%"
    else:
        return parts[0] + separator + parts[1] + "%"

def number_to_human_size(size):
    """
    Returns a formatted-for-humans file size.
    
    Examples::
    
        >>> number_to_human_size(123)
        123 Bytes
        >>> number_to_human_size(1234)
        1.2 KB
        >>> number_to_human_size(12345)
        12.1 KB
        >>> number_to_human_size(1234567)
        1.2 MB
        >>> number_to_human_size(1234567890)
        1.1 GB
    """
    if size == 1:
        return "1 Byte"
    elif size < 1024:
        return "%d Bytes" % size
    elif size < (1024**2):
        return "%.1f KB" % (size / 1024.00)
    elif size < (1024**3):
        return "%.1f MB" % (size / 1024.00**2)
    elif size < (1024**4):
        return "%.1f GB" % (size / 1024.00**3)
    elif size < (1024**5):
        return "%.1f TB" % (size / 1024.00**4)
    else:
        return ""

human_size = number_to_human_size

def number_with_delimiter(number, delimiter=","):
    """
    Formats a ``number`` with a ``delimiter``.
    
    Example::
    
        >>> number_with_delimiter(12345678)
        12,345,678
    """
    return re.sub(r'(\d)(?=(\d\d\d)+(?!\d))', r'\1%s' % delimiter, str(number))

def number_with_precision(number, precision=3):
    """
    Formats a ``number`` with a level of ``precision``.
    
    Example::
    
        >>> number_with_precision(111.2345)
        111.235
    """
    formstr = '%01.' + str(precision) + 'f'
    return formstr % number

__all__ = ['number_to_phone', 'number_to_currency', 'number_to_percentage','number_with_delimiter', 
           'number_with_precision', 'number_to_human_size', 'human_size']
