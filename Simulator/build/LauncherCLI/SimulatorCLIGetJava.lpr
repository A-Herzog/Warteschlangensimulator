program SimulatorCLIGetJava;

{$mode objfpc}{$H+}

uses
  {$IFDEF UNIX}{$IFDEF UseCThreads}
  cthreads,
  {$ENDIF}{$ENDIF}
  findjava;

Var
  fFindJava: TFindJava;

{$R *.res}

begin
  fFindJava:=TFindJava.Create;
  writeln(fFindJava.JavaPath);
end.

