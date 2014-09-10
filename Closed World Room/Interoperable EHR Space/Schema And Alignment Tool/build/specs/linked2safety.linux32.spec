# -*- mode: python -*-
import os
curdir = os.path.realpath(os.path.join(os.path.dirname(__file__), '..', '..'))
builddir = os.path.join(curdir, 'build')
appdir = os.path.realpath(os.path.join(curdir, '..', 'application'))

import imp
versioning = imp.load_source('module.name', os.path.join(curdir, "versioning.py"))
versioning.UpdateVersion()

print 'Appdir: ', builddir

def extra_datas(relativepath,mydir):
	import glob
	import os
	def rec_glob(p, files):
		for d in glob.glob(p):
			if os.path.isfile(d):
				files.append(d)
			rec_glob(os.path.join(d, "*"), files)
	files = []
	dir = os.path.join(mydir, relativepath, '*')
	print dir
	rec_glob(dir, files)
	extra_datas = []
	for f in files:
		name = f.replace(mydir + os.path.sep, '')
		extra_datas.append((name, f, 'DATA'))
	return extra_datas
##### include mydir in distribution #######
a = Analysis([os.path.join(appdir,'linked2safety.py')],
             pathex=[os.path.join(builddir,'linux32')],
             hiddenimports=[],
             hookspath=None)
			 
a.datas += extra_datas('external', appdir)
a.datas += extra_datas('templates', appdir)
a.datas += extra_datas('resources', appdir)
a.datas += extra_datas('static', appdir)
a.datas += [('application.config.xml', os.path.join(appdir, 'application.config.xml'), 'DATA' )]
a.datas += [('application.local.config.xml', os.path.join(appdir, 'application.local.config.xml'), 'DATA' )]

pyz = PYZ(a.pure)
exe = EXE(pyz,
          a.scripts,
          exclude_binaries=1,
          name=os.path.join('build', 'pyi.linux2', 'linked2safety.linux32', 'linked2safety.linux32.bin'),
          debug=False,
          strip=None,
          upx=True,
          console=True , 
		  icon=os.path.join(appdir, 'static', 'favicon.ico')
		)
coll = COLLECT(exe,
               a.binaries,
               a.zipfiles,
               a.datas,
               strip=None,
               upx=True,
               name=os.path.join(builddir, 'linux32','linked2safety'))
