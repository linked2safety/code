REM This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
REM For details see: http://opensource.org/licenses/GPL-3.0
@echo off
SET VS90COMNTOOLS=%VS100COMNTOOLS%
set INIT_DIR=%CD%

"%INIT_DIR%\..\python_x86\python.exe" "%INIT_DIR%\install.py"
