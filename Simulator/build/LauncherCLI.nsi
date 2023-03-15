!define PrgName "Warteschlangensimulator"
!define PrgFileName "Simulator"
!define PrgIcon "..\src\main\java\ui\res\Symbol.ico"
!define Copyright "Alexander Herzog"

!include Version.nsi

VIProductVersion "${VERSION}.100"
VIAddVersionKey "ProductName" "${PrgName}"
VIAddVersionKey "FileDescription" "${PrgName} CLI Java Getter"
VIAddVersionKey "LegalCopyright" "${Copyright}"
VIAddVersionKey "CompanyName" "${Copyright}"
VIAddVersionKey "FileVersion" "${VERSION}.100"
VIAddVersionKey "ProductVersion" "${VERSION}.100"
VIAddVersionKey "InternalName" "${PrgName}"

Name "${PrgName} ${VERSION}"
Caption "${PrgName} ${VERSION}"
Icon "${PrgIcon}"
OutFile "${PrgFileName}CLIGetJava.exe"

ManifestDPIAware true
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
; ShowInstDetails show
RequestExecutionLevel user


!include LogicLib.nsh

Section ""
  Call GetJRE
  Pop $R0
  
  System::Call 'kernel32::GetStdHandle(i -11)i.r0' 
  System::Call 'kernel32::AttachConsole(i -1)i.r1' 
  ${If} $0 = 0
  ${OrIf} $1 = 0
  System::Call 'kernel32::AllocConsole()'
  System::Call 'kernel32::GetStdHandle(i -11)i.r0' 
  ${EndIf}

  FileWrite $0 '$R0'
SectionEnd
 
Function FindFiles
  Exch $R5 # callback function
  Exch 
  Exch $R4 # file name
  Exch 2
  Exch $R0 # directory
  Push $R1
  Push $R2
  Push $R3
  Push $R6
 
  Push $R0 # first dir to search
 
  StrCpy $R3 1
 
  nextDir:
    Pop $R0
    IntOp $R3 $R3 - 1
    ClearErrors
    FindFirst $R1 $R2 "$R0\*.*"
    nextFile:
      StrCmp $R2 "." gotoNextFile
      StrCmp $R2 ".." gotoNextFile
 
      StrCmp $R2 $R4 0 isDir
        Push "$R0\$R2"
        Call $R5
        Pop $R6
        StrCmp $R6 "stop" 0 isDir
          loop:
            StrCmp $R3 0 done
            Pop $R0
            IntOp $R3 $R3 - 1
            Goto loop
 
      isDir:
        IfFileExists "$R0\$R2\*.*" 0 gotoNextFile
          IntOp $R3 $R3 + 1
          Push "$R0\$R2"
 
  gotoNextFile:
    FindNext $R1 $R2
    IfErrors 0 nextFile
 
  done:
    FindClose $R1
    StrCmp $R3 0 0 nextDir
 
  Pop $R6
  Pop $R3
  Pop $R2
  Pop $R1
  Pop $R0
  Pop $R5
  Pop $R4
FunctionEnd
 
!macro CallFindFiles DIR FILE CBFUNC
Push "${DIR}"
Push "${FILE}"
Push $0
GetFunctionAddress $0 "${CBFUNC}"
Exch $0
Call FindFiles
!macroend
 
Function FindJava
  Pop $0
  StrCpy $9 $0
  Push "stop"
FunctionEnd
 
Function GetJRE
  ;  Find JRE (javaw.exe)
  ;  0 - in QS_JAVA_HOME environment variable
  ;  1a- in .\jdk directory (JRE Installed with application)
  ;  1b- in .\jre directory (JRE Installed with application)
  ;  2 - in JAVA_HOME environment variable
  ;  3a- jdk in the registry
  ;  3b- jre in the registry
  ;  4 - in C:\Program Files (Arm)\Microsoft
  ;  5a- in C:\Program Files\Eclipse Adoptium
  ;  5b- in C:\Program Files\Eclipse Foundation
  ;  5c- in C:\Program Files\AdoptOpenJDK and its subfolders  
  ;  5d- in C:\Program Files\Java and its subfolders
  ;  5e- in C:\Program Files\Amazon Corretto and its subfolders  
  ;  5f- in C:\Program Files\Zulu and its subfolders
  ;  5g- in C:\Program Files\Microsoft and its subfolders  
  ;  6 - Search javaw.exe on PATH  
  ;  7 - in C:\Program Files and its subfolders
  ;  8 - try to call javaw.exe without path
 
  Push $R0
  Push $R1
  
  ; Umgebungsvariable "QS_JAVA_HOME"
  ClearErrors
  ReadEnvStr $R0 "QS_JAVA_HOME"
  DetailPrint "QS_JAVA_HOME=$R0"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors +2 0
  IfFileExists $R0 JreFound
 
  ; Unterverzeichnis "jdk" 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jdk\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
  
  ; Unterverzeichnis "jre"
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ; Umgebungsvariable "JAVA_HOME"
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  DetailPrint "JAVA_HOME=$R0"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors +2 0
  IfFileExists $R0 JreFound

  ; Registry-Eintrag zur JDK
  ClearErrors
  ReadRegStr $R1 HKLM64 "SOFTWARE\JavaSoft\JDK\" "CurrentVersion"
  ReadRegStr $R0 HKLM64 "SOFTWARE\JavaSoft\JDK\$R1" "JavaHome"
  DetailPrint "RegJDK current: $R1"
  DetailPrint "RegJDK path: $R0"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
  
  ; Registry-Eintrag zur JRE
  ClearErrors
  ReadRegStr $R1 HKLM64 "SOFTWARE\JavaSoft\Java Runtime Environment\" "CurrentVersion"
  ReadRegStr $R0 HKLM64 "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  DetailPrint "RegJRE current: $R1"
  DetailPrint "RegJRE path: $R0"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
  
  ; Unter "C:\Program Files (Arm)\Microsoft" suchen
  !insertmacro CallFindFiles "C:\Program Files (Arm)\Microsoft" javaw.exe FindJava
  DetailPrint "Microsoft (Arm): $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
  
  ; Unter "C:\Program Files\Eclipse Adoptium" suchen
  !insertmacro CallFindFiles "$PROGRAMFILES64\Eclipse Adoptium" javaw.exe FindJava
  DetailPrint "EclipseAdoptium: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound  
     
  ; Unter "C:\Program Files\Eclipse Foundation" suchen
  !insertmacro CallFindFiles "$PROGRAMFILES64\Eclipse Foundation" javaw.exe FindJava
  DetailPrint "EclipseFoundation: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
	 
  ; Unter "C:\Program Files\AdoptOpenJDK" suchen
  !insertmacro CallFindFiles $PROGRAMFILES64\AdoptOpenJDK javaw.exe FindJava
  DetailPrint "AdoptOpenJDK: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
 
  ; Unter "C:\Program Files\Java" suchen
  !insertmacro CallFindFiles $PROGRAMFILES64\Java javaw.exe FindJava
  DetailPrint "Java: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
  
  ; Unter "C:\Program Files\Amazon Corretto" suchen
  !insertmacro CallFindFiles "$PROGRAMFILES64\Amazon Corretto" javaw.exe FindJava
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
  
  ; Unter "C:\Program Files\Zulu" suchen
  !insertmacro CallFindFiles "$PROGRAMFILES64\Zulu" javaw.exe FindJava
  DetailPrint "Zulu: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound
  
  ; Unter "C:\Program Files\Microsoft" suchen
  !insertmacro CallFindFiles "$PROGRAMFILES64\Microsoft" javaw.exe FindJava
  DetailPrint "Microsoft: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound

  ; Such im Suchpfad
  SearchPath $9 "javaw.exe"
  DetailPrint "Path: $9"
  StrCpy $R0 $9
  IfErrors 0 JreFound
  
  ; Unter "C:\Program Files" suchen
  !insertmacro CallFindFiles $PROGRAMFILES64 javaw.exe FindJava
  DetailPrint "ProgramFiles: $9"
  StrCmp $9 "" +3 0
  StrCpy $R0 $9
  Goto JreFound

  ; Versuch eines direkten Aufrufs
  StrCpy $R0 "javaw.exe"
  
 JreFound:
  Pop $R1
  DetailPrint "Result: $R0"
  Exch $R0
FunctionEnd