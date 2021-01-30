!define PrgName "Warteschlangensimulator"
!define PrgFileName "Simulator_Reset"
!define PrgIcon "..\src\main\java\ui\res\Symbol.ico"
!define Copyright "Alexander Herzog"

!include Version.nsi

VIProductVersion "${VERSION}.100"
VIAddVersionKey "ProductName" "${PrgName}"
VIAddVersionKey "FileDescription" "${PrgName} configuration reset"
VIAddVersionKey "LegalCopyright" "${Copyright}"
VIAddVersionKey "CompanyName" "${Copyright}"
VIAddVersionKey "FileVersion" "${VERSION}.100"
VIAddVersionKey "ProductVersion" "${VERSION}.100"
VIAddVersionKey "InternalName" "${PrgName}"

Name "${PrgName} ${VERSION}"
Caption "${PrgName} ${VERSION}"
Icon "${PrgIcon}"
OutFile "${PrgFileName}.exe"

ManifestDPIAware true
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
; ShowInstDetails show
RequestExecutionLevel user

Section ""
  MessageBox MB_YESNO "Do you want to reset the configuration of Warteschlangensimulator?" IDNO end
  
  ExecWait "Simulator.exe Reset"
  
  end:
SectionEnd