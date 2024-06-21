@echo off

rem This batch files tries to find a Java environment and then
rem to start Warteschlangensimulator using this environment.

rem Find Java in:
rem 0 - in QS_JAVA_HOME environment variable
rem 1a- in .\jdk directory (JDK installed with application)
rem 1b- in .\jre directory (JRE installed with application)
rem 2 - in JAVA_HOME environment variable
rem 3a- in registry (Javasoft JDK)
rem 3b- in registry (Javasoft JRE)
rem 4 - in C:\Program Files (Arm)\Microsoft
rem 5a- in C:\Program Files\Eclipse Adoptium
rem 5b- in C:\Program Files\Eclipse Foundation
rem 5c- in C:\Program Files\AdoptOpenJDK and its subfolders  
rem 5d- in C:\Program Files\Java and its subfolders
rem 5e- in C:\Program Files\Amazon Corretto and its subfolders  
rem 5f- in C:\Program Files\Zulu and its subfolders
rem 5g- in C:\Program Files\Microsoft and its subfolders  
rem 6 - Search java on PATH  
rem 7 - in C:\Program Files and its subfolders

set java_launcher=javaw.exe

rem Environment variable "QS_JAVA_HOME"
if not "%QS_JAVA_HOME%"=="" (
  for /f "delims=" %%a in ('dir /s /b "%QS_JAVA_HOME%\%java_launcher%"') do (
    set "qs_java_name=%%a"
	goto found_java
  )
)

rem Java in "jdk" sub folder
if exist "%~dp0jdk\bin\%java_launcher%" (
  set "qs_java_name=%~dp0jdk\bin\%java_launcher%"
  goto found_java
)

rem Java in "jre" sub folder
if exist "%~dp0jre\bin\%java_launcher%" (
  set "qs_java_name=%~dp0jre\bin\%java_launcher%"
  goto found_java
)

rem Environment variable "JAVA_HOME"
if not "%JAVA_HOME%"=="" (
  for /f "delims=" %%a in ('dir /s /b "%JAVA_HOME%\%java_launcher%"') do (
    set "qs_java_name=%%a"
	goto found_java
  )
)

rem Search JDK in registry
for /F "Skip=1 Tokens=2*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v CurrentVersion /reg:64 2^>nul') do set javasoft_current_version=%%B
if "%javasoft_current_version%"=="" goto no_javasoft_jdk
for /F "Skip=1 Tokens=2*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\%javasoft_current_version%" /v JavaHome /reg:64 2^>nul') do set javasoft_home=%%B
if "%javasoft_home%"=="" goto no_javasoft_jdk
set qs_java_name=%javasoft_home%\bin\%java_launcher%
goto found_java
:no_javasoft_jdk
set javasoft_current_version=
set javasoft_home=

rem Search JRE in registry
for /F "Skip=1 Tokens=2*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment" /v CurrentVersion /reg:64 2^>nul') do set javasoft_current_version=%%B
if "%javasoft_current_version%"=="" goto no_javasoft_jre
for /F "Skip=1 Tokens=2*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\%javasoft_current_version%" /v JavaHome /reg:64 2^>nul') do set javasoft_home=%%B
if "%javasoft_home%"=="" goto no_javasoft_jre
set qs_java_name=%javasoft_home%\bin\%java_launcher%
goto found_java
:no_javasoft_jre
set javasoft_current_version=
set javasoft_home=

rem Search in "C:\Program Files (Arm)\Microsoft"
if exist "C:\Program Files (Arm)\Microsoft\" (
  for /f "delims=" %%a in ('dir /s /b "C:\Program Files (Arm)\Microsoft\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Eclipse Adoptium"
if exist "%ProgramFiles%\Eclipse Adoptium" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\Eclipse Adoptium\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Eclipse Foundation"
if exist "%ProgramFiles%\Eclipse Foundation" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\Eclipse Foundation\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\AdoptOpenJDK"
if exist "%ProgramFiles%\AdoptOpenJDK" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\AdoptOpenJDK\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Java"
if exist "%ProgramFiles%\AdoptOpenJDK" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\AdoptOpenJDK\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Amazon Corretto"
if exist "%ProgramFiles%\Amazon Corretto" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\Amazon Corretto\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Zulu"
if exist "%ProgramFiles%\Zulu" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\Zulu\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\Microsoft"
if exist "%ProgramFiles%\Microsoft" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\Microsoft\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in path
where /Q %java_launcher%
if %errorlevel% == 0 (
  for /f "delims=" %%a in ('where %java_launcher%') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

rem Search in "C:\Program Files\"
if exist "%ProgramFiles%" (
  for /f "delims=" %%a in ('dir /s /b "%ProgramFiles%\%java_launcher%"') do (
    set "qs_java_name=%%a"
    goto found_java
  )
)

if exist "%~dp0tools\JavaDownloader.exe" (
  start "" "%~dp0tools\JavaDownloader.exe"
) else (
  echo No Java environment found.
  echo Cannot start Warteschlangensimulator.
  echo You can download Java from adoptium.net.
  pause
)
goto end

:found_java
start /B "" "%qs_java_name%" -jar %~dp0Simulator.jar %1 %2 %3 %4 %5
set qs_java_name=

:end
set java_launcher=
