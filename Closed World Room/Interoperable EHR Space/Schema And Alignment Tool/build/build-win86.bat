@echo off

SET VS90COMNTOOLS=%VS100COMNTOOLS%
SET INIT_DIR=%CD%
SET PATH=%INIT_DIR%\..\python_x86\

python "%INIT_DIR%\pyinstaller-2.1\pyinstaller.py" "%INIT_DIR%\specs\linked2safety.win32.spec"
pause