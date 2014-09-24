#!/afs/bx.psu.edu/project/pythons/linux-x86_64-ucs4/bin/python2.6

"""
Read a file from stdin, split each line and write fields one per line to 
stdout.

TODO: is this really that useful?
"""

import sys

for line in sys.stdin:
    for field in line.split():
        print field