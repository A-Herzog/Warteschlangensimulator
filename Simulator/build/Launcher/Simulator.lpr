program Simulator;

{$mode objfpc}{$H+}

uses
  {$IFDEF UNIX}{$IFDEF UseCThreads}
  cthreads,
  {$ENDIF}{$ENDIF}
  ShellAPI,
  findjava;

const SW_SHOW=5;

Var
  fFindJava: TFindJava;
  JavaExe: String;
  handle: THandle;
  param: String;
  i: Integer;

{$R *.res}

begin
  fFindJava:=TFindJava.Create;

  if not fFindJava.IsSimFound then Exit;

  JavaExe:=fFindJava.JavaPath;
  if JavaExe='' Then JavaExe:='javaw.exe';

  param:='';
  for i:=0 to ParamCount-1 do begin
      param:=param+' '+ParamStr(i);
  end;
  handle:=ShellExecute(0,'open',PChar('"'+JavaExe+'"'),PChar('-jar Simulator.jar'+param),nil,SW_SHOW);
  if handle<=32 then begin
    ShellExecute(0,'open',PChar('"'+fFindJava.OwnPath+'tools\JavaDownloader.exe"'),nil,nil,SW_SHOW);
  end;
end.

