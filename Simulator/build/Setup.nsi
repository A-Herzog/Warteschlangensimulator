; Include used librarys
; ============================================================

Unicode false

!addplugindir ".\NSIS_Plugins"
!addincludedir ".\NSIS_Plugins"
!include nsDialogs.nsh
!include nsProcess.nsh
!include LogicLib.nsh
!include MUI2.nsh
!include Sections.nsh
!include UAC.nsh
!include NsisMultiUser.nsh ; see http://drizin.io/NSIS-and-Windows-Installers-for-MultiUser-Current-User/ and https://github.com/Drizin/NsisMultiUser
!include NsisMultiUserLang.nsh
!include StdUtils.nsh

!include Version.nsi



; Define program name and version information
; ============================================================

!define PrgName "Warteschlangensimulator"
!define SetupFileName "SimulatorSetup.exe"
!define RegKey "Warteschlangensimulator"
!define Copyright "Alexander Herzog"

OutFile "..\..\Release\${SetupFileName}"

; Settings for NsisMultiUser
!define PRODUCT_NAME "${PrgName}"
!define PROGEXE "Simulator.exe"
!define APP_NAME "${RegKey}"
!define APP_NAME_DISPLAY "${PrgName}"
!define COMPANY_NAME "${Copyright}"



; Initial settings
; ============================================================

Name "${PrgName}"
BrandingText "${PrgName} ${VERSION}"
RequestExecutionLevel user

!insertmacro MUI_RESERVEFILE_LANGDLL

ShowInstDetails nevershow
ShowUninstDetails nevershow

!define UNINSTALL_FILENAME "Uninstall.exe"
!define MULTIUSER_INSTALLMODE_INSTDIR "${APP_NAME}"  ; suggested name of directory to install (under $PROGRAMFILES or $LOCALAPPDATA)
!define MULTIUSER_INSTALLMODE_INSTALL_REGISTRY_KEY "${APP_NAME}"  ; registry key for INSTALL info, placed under [HKLM|HKCU]\Software  (can be ${APP_NAME} or some {GUID})
!define MULTIUSER_INSTALLMODE_UNINSTALL_REGISTRY_KEY "${APP_NAME}"  ; registry key for UNINSTALL info, placed under [HKLM|HKCU]\Software\Microsoft\Windows\CurrentVersion\Uninstall  (can be ${APP_NAME} or some {GUID})
!define MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_VALUENAME "UninstallString"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_VALUENAME "InstallLocation"
!define MULTIUSER_INSTALLMODE_DISPLAYNAME "${APP_NAME_DISPLAY}" ; this is optional... name that will be displayed in add/remove programs (default is ${APP_NAME} ${VERSION})
!define MULTIUSER_INSTALLMODE_ALLOW_ELEVATION   ; allow requesting for elevation... if false, radiobutton will be disabled and user will have to restart installer with elevated permissions
; !define MULTIUSER_INSTALLMODE_DEFAULT_ALLUSERS  ; only available if MULTIUSER_INSTALLMODE_ALLOW_ELEVATION



; Settings for the modern user interface (MUI)
; ============================================================

!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall.bmp"
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp"
!define MUI_FINISHPAGE_TITLE_3LINES

!define MUI_ABORTWARNING
!define MUI_UNABORTWARNING
!define MUI_COMPONENTSPAGE_NODESC

!define MUI_FINISHPAGE_HEADER  "$(LANGNAME_FinishTitle)"
!define MUI_FINISHPAGE_TEXT  "$(LANGNAME_FinishText)"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION ExecAppFile
!define MUI_FINISHPAGE_RUN_TEXT  "$(LANGNAME_FinishRun)"
!define MUI_COMPONENTSPAGE_TEXT_TOP "$(LANGNAME_UnSectionTopInfo)"
!define MUI_COMPONENTSPAGE_TEXT_COMPLIST "$(LANGNAME_UnSectionAction)"
!define MUI_COMPONENTSPAGE_TEXT_INSTTYPE ""

!define MUI_UNABORTWARNING_TEXT "$(LANGNAME_UnAbortWarning)"

!insertmacro MULTIUSER_PAGE_INSTALLMODE
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MULTIUSER_UNPAGE_INSTALLMODE
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_INSTFILES

Function ExecAppFile
  UserInfo::getAccountType
  Pop $0
  StrCmp $0 "Admin" ExecAdminMode
  Exec "$INSTDIR\${PROGEXE}"
  Goto ExecDone
  ExecAdminMode:
  !insertmacro UAC_AsUser_ExecShell "" "$INSTDIR\${PROGEXE}" "" "" ""
  ExecDone:
FunctionEnd



; Main settings for different languages
; ============================================================

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

LangString LANGNAME_FinishTitle ${LANG_GERMAN} "Installation des Warteschlangensimulators abgeschlossen"
LangString LANGNAME_FinishText ${LANG_GERMAN} "Der Warteschlangensimulator wurde auf Ihrem Computer installiert.$\r$\n$\r$\nKlicken Sie auf Fertig stellen, um dieses Installationsprogramm zu schließen."
LangString LANGNAME_FinishRun ${LANG_GERMAN} "Warteschlangensimulator jetzt starten"
LangString LANGNAME_LinkUninstall ${LANG_GERMAN} "Warteschlangensimulator deinstallieren oder Konfiguration zurücksetzen"
LangString LANGNAME_UninstallWarning ${LANG_GERMAN} "Das Installationsverzeichnis enthält Dateien, die nicht vom Warteschlangensimulator stammen. Soll das Verzeichnis dennoch vollständig gelöscht werden?"
LangString LANGNAME_UninstallTitle ${LANG_GERMAN} "${PrgName} reparieren / deinstallieren"
LangString LANGNAME_UnSectionTopInfo ${LANG_GERMAN} "Sie können das Programm deinstallieren oder lediglich die Konfiguration auf den Auslieferungszustand zurücksetzen."
LangString LANGNAME_UnSectionAction ${LANG_GERMAN} "Aktion:"
LangString LANGNAME_UnSectionReset ${LANG_GERMAN} "Konfiguration zurücksetzen (Programm nicht deinstallieren)"
LangString LANGNAME_UnSectionUninstall ${LANG_GERMAN} "Programm deinstallieren"
LangString LANGNAME_UnAbortWarning ${LANG_GERMAN} "Wollen Sie das Service- und Deinstallationsprogramm wirklich abbrechen?"
LangString LANGNAME_SimulatorCfg ${LANG_GERMAN} "<?xml version=$\"1.0$\" encoding=$\"UTF-8$\" standalone=$\"no$\"?><Setup><Language>de</Language></Setup>"

LangString LANGNAME_FinishTitle ${LANG_ENGLISH} "Installation of Warteschlangensimulator completed"
LangString LANGNAME_FinishText ${LANG_ENGLISH} " Warteschlangensimulator was installed on your computer.$\r$\n$\r$\nClick Finish to close this installer."
LangString LANGNAME_FinishRun ${LANG_ENGLISH} "Run Warteschlangensimulator now"
LangString LANGNAME_LinkUninstall ${LANG_ENGLISH} "Uninstall Warteschlangensimulator or reset configuration"
LangString LANGNAME_UninstallWarning ${LANG_ENGLISH} "The installation folder contains files which are not installed with Warteschlangensimulator. Do you want to delete this folder anyway?"
LangString LANGNAME_UninstallTitle ${LANG_ENGLISH} "Repair / uninstall ${PrgName}"
LangString LANGNAME_UnSectionTopInfo ${LANG_ENGLISH} "You can uninstall the program or just reset the configuration to the initial state."
LangString LANGNAME_UnSectionAction ${LANG_ENGLISH} "Action:"
LangString LANGNAME_UnSectionReset ${LANG_ENGLISH} "Reset configuration (do not uninstall program)"
LangString LANGNAME_UnSectionUninstall ${LANG_ENGLISH} "Uninstall program"
LangString LANGNAME_UnAbortWarning ${LANG_ENGLISH} "Do you really want to quit the service and uninstallation program?"
LangString LANGNAME_SimulatorCfg ${LANG_ENGLISH} "<?xml version=$\"1.0$\" encoding=$\"UTF-8$\" standalone=$\"no$\"?><Setup><Language>en</Language></Setup>"

!insertmacro MULTIUSER_LANGUAGE_INIT

; Version data in installer exe (has to be placed after language init)
VIProductVersion "${VERSION}.100"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "${PrgName}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${PrgName} Setup"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "${Copyright}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "${Copyright}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${VERSION}.100"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductVersion" "${VERSION}.100"
VIAddVersionKey /LANG=${LANG_ENGLISH} "InternalName" "${SetupFileName}"
VIAddVersionKey /LANG=${LANG_GERMAN} "ProductName" "${PrgName}"
VIAddVersionKey /LANG=${LANG_GERMAN} "FileDescription" "${PrgName} Setup"
VIAddVersionKey /LANG=${LANG_GERMAN} "LegalCopyright" "${Copyright}"
VIAddVersionKey /LANG=${LANG_GERMAN} "CompanyName" "${Copyright}"
VIAddVersionKey /LANG=${LANG_GERMAN} "FileVersion" "${VERSION}.100"
VIAddVersionKey /LANG=${LANG_GERMAN} "ProductVersion" "${VERSION}.100"
VIAddVersionKey /LANG=${LANG_GERMAN} "InternalName" "${SetupFileName}"

UninstallCaption "$(LANGNAME_UninstallTitle)"



; Definition of install / uninstallation sections
; ============================================================

Section "Install" Inst
  call stopSimulator
  
  SetOverwrite try

  SetOutPath "$INSTDIR"
  
  UserInfo::getAccountType
  Pop $0
  StrCmp $0 "Admin" ContinueInstall
  IfFileExists "$INSTDIR\Simulator.cfg" ContinueInstall
    FileOpen $9 "$INSTDIR\Simulator.cfg" w
    FileWrite $9 "$(LANGNAME_SimulatorCfg)"
    FileClose $9

  ContinueInstall:
  
  File "..\..\Release\Simulator.jar"
  File "..\..\Release\Simulator.exe"
  File "..\..\Release\Simulator.sh"
  File "..\..\Release\SimulatorCLI.bat"
  File "..\..\Release\Simulator_Reset.exe"
  File "..\..\Release\JDBC.cfg"
  File "..\tools\JDDE\JavaDDEx64.dll"
  File "..\tools\JDDE\JavaDDE.dll"
  WriteUninstaller "Uninstall.exe"
  
  SetOutPath "$INSTDIR\docs"
  
  File "..\..\Release\docs\*.pdf"
  File "..\..\Release\docs\*.xsd"
  File "..\..\Release\docs\*.dtd"
  File "..\..\Release\docs\*.md"
  File "..\..\Release\docs\*.txt"
  
  RmDir /r $INSTDIR\libs   ; On update: delete old libraries
  
  SetOutPath "$INSTDIR\libs"
  
  File /r "..\..\Release\libs\*.jar"
  
  RmDir /r $INSTDIR\tools   ; On update: delete old tools folder (using other structure)
  
  SetOutPath "$INSTDIR\tools"
  
  File /r "..\..\Release\tools\*.*"
  
  SetOutPath "$INSTDIR\userscripts"
  
  File /r "..\..\Release\userscripts\*.*"
  
  SetOutPath "$INSTDIR\dictionaries"
  
  File /r "..\..\Release\dictionaries\*.*"
  
  SetOutPath "$INSTDIR" ; Otherwise the shortcut will have the dictionaries as working directory
  
  CreateShortCut "$SMPROGRAMS\${PrgName}.lnk" "$INSTDIR\${PROGEXE}"
  
  !insertmacro MULTIUSER_RegistryAddInstallInfo
  !insertmacro MULTIUSER_RegistryAddInstallSizeInfo
  
  ; Remove uninstaller reg keys from old installations
  SetShellVarContext current
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${RegKey}"
  
  ; Remove failed update setup files
  Delete "$APPDATA\Temp\${SetupFileName}.part"
  Delete "$APPDATA\Temp\${SetupFileName}"
  
  IfSilent 0 notSilent
  UserInfo::getAccountType
  Pop $0
  StrCmp $0 "Admin" notSilent
  Exec "$INSTDIR\${PROGEXE}"
  notSilent:
SectionEnd



Section "un.Uninstall" uninst
  SetDetailsPrint none
  
  call un.stopSimulator
  
  RmDir /r $INSTDIR\jre
  RmDir /r $INSTDIR\jdk

  Delete "$INSTDIR\Simulator.sh"
  Delete "$INSTDIR\SimulatorCLI.bat"
  Delete "$INSTDIR\Simulator.exe"
  Delete "$INSTDIR\Simulator.old"
  Delete "$INSTDIR\Simulator.new"
  Delete "$INSTDIR\Simulator.jar"
  Delete "$INSTDIR\Simulator_Reset.exe"
  Delete "$INSTDIR\Simulator.cfg"
  Delete "$INSTDIR\User-spelling.cfg"
  Delete "$INSTDIR\JDBC.cfg"
  Delete "$INSTDIR\Certificate.cfg"
  Delete "$INSTDIR\Training.dat"
  Delete "$INSTDIR\JavaDDEx64.dll"
  Delete "$INSTDIR\JavaDDE.dll"
  Delete "$INSTDIR\Uninstall.exe"
  Delete "$INSTDIR\.pdfbox.cache"
  
  RmDir /r $INSTDIR\docs
  RmDir /r $INSTDIR\libs
  RmDir /r $INSTDIR\tools
  RmDir /r $INSTDIR\userscripts
  RmDir /r $INSTDIR\dictionaries
  
  Delete "$APPDATA\Temp\${SetupFileName}.part"
  Delete "$APPDATA\Temp\${SetupFileName}"
  Delete "$APPDATA\Temp\SimulatorSetupWork.exe"
  
  Push "$INSTDIR"
  Call un.isEmptyDir
  Pop $0
  StrCmp $0 1 RemoveInstDir
  MessageBox MB_YESNO $(LANGNAME_UninstallWarning) IDYES RemoveInstDir IDNO RemoveInstDirDone
  RemoveInstDir:
  RmDir /r $INSTDIR
  RemoveInstDirDone:

  Delete "$SMPROGRAMS\${PrgName}.lnk"
  
  !insertmacro MULTIUSER_RegistryRemoveInstallInfo
  
  ; Remove uninstaller reg keys from old installations
  SetShellVarContext current
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${RegKey}"
  
  SetAutoClose true
SectionEnd



Section /o "un.ResetConfig" reset
  SetDetailsPrint none
  
  call un.stopSimulator
  
  SetOutPath "$INSTDIR"
  ExecWait "$INSTDIR\${PROGEXE} Reset"
  
  SetAutoClose true
SectionEnd



; Additional functions
; ============================================================

Function .onInit  
  !insertmacro MULTIUSER_INIT
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd



Function un.onInit
  !insertmacro MULTIUSER_UNINIT
  !insertmacro MUI_LANGDLL_DISPLAY
  
  ; LangString not available in onInit, so we need a workaround
  ${Switch} $LANGUAGE
    ${Case} '1031'
      SectionSetText ${uninst} "Programm deinstallieren"
      SectionSetText ${reset} "Konfiguration zurücksetzen (Programm nicht deinstallieren)"
      ${Break}
    ${Default}
      SectionSetText ${uninst} "Uninstall program"
      SectionSetText ${reset} "Reset configuration (do not uninstall program)"
      ${Break}
  ${EndSwitch}
  StrCpy $1 ${uninst}
FunctionEnd



Function un.onSelChange
  !insertmacro StartRadioButtons $1
    !insertmacro RadioButton ${uninst}
    !insertmacro RadioButton ${reset}
  !insertmacro EndRadioButtons
FunctionEnd



Function un.isEmptyDir
  ; Stack ->                    ; Stack: <directory>
  Exch $0                       ; Stack: $0
  Push $1                       ; Stack: $1, $0
  FindFirst $0 $1 "$0\*.*"
  strcmp $1 "." 0 _notempty
    FindNext $0 $1
    strcmp $1 ".." 0 _notempty
      ClearErrors
      FindNext $0 $1
      IfErrors 0 _notempty
        FindClose $0
        Pop $1                  ; Stack: $0
        StrCpy $0 1
        Exch $0                 ; Stack: 1 (true)
        goto _end
     _notempty:
       FindClose $0
       Pop $1                   ; Stack: $0
       StrCpy $0 0
       Exch $0                  ; Stack: 0 (false)
  _end:
FunctionEnd



Function stopSimulator
  ${nsProcess::KillProcess} "Javaw.exe" $R0
  ${nsProcess::KillProcess} "${PROGEXE}" $R0  
  Sleep 1000
FunctionEnd



Function un.stopSimulator
  ${nsProcess::KillProcess} "Javaw.exe" $R0
  ${nsProcess::KillProcess} "${PROGEXE}" $R0  
  ${nsProcess::KillProcess} "${SetupFileName}" $R0
  Sleep 1000
FunctionEnd