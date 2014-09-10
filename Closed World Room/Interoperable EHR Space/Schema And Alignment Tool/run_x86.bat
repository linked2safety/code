REM #This code is licenced under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
REM #For details see: http://opensource.org/licenses/LGPL-3.0
@echo off
echo Starting Linked2Safety Toolset...

set INIT_DIR=%CD%

"%INIT_DIR%\python_x86\python.exe" "%INIT_DIR%\application\webstart.py" "open in browser"
