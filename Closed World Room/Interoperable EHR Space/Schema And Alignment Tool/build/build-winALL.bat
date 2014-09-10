@echo off

SET VS90COMNTOOLS=%VS100COMNTOOLS%
SET INIT_DIR=%CD%

echo "1. Clean previous builds"
RD /S /Q "%INIT_DIR%\build"
MD "%INIT_DIR%\build"

RD /S /Q "%INIT_DIR%\dist"
MD "%INIT_DIR%\dist"

echo "2. Build x86 application"

SET PATH=%INIT_DIR%\..\python_x86\
python "%INIT_DIR%\pyinstaller-2.1\pyinstaller.py" "%INIT_DIR%\specs\linked2safety.win32.spec"

echo "3. Build x64 application"
SET PATH=%INIT_DIR%\..\python_x64\
python "%INIT_DIR%\pyinstaller-2.1\pyinstaller.py" "%INIT_DIR%\specs\linked2safety.win64.spec"

echo "4. Finished"
pause