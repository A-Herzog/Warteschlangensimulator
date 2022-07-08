unit testarm;

{$mode objfpc}{$H+}

interface

function IsARM : boolean;

implementation

uses
  Windows, SysUtils;

type
  TImageFileMachine = ushort;
  TIsWow64Process2 = function(Handle: THandle; var ProcessMachine,NativeMachine: TImageFileMachine) : LongBool; cdecl;

const
  IMAGE_FILE_MACHINE_ARM64=$AA64;

function IsARM : Boolean;
  var
    FarProc: Pointer;
    F: TIsWow64Process2;
    P,N: TImageFileMachine;
  begin
    FarProc:=Pointer(GetProcAddress(GetModuleHandle('kernel32.dll'),'IsWow64Process2'));
    if not Assigned(FarProc) then begin
      Result:=false;
      exit;
    end;

    F:=TIsWow64Process2(FarProc);
    P:=0;
    N:=0;
    if not F(GetCurrentProcess,P,N) then
      Result:=false
    else
      Result:=(N=IMAGE_FILE_MACHINE_ARM64);
  end;

end.

