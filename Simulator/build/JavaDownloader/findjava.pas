unit findjava;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils;

Type

{ TFindJava }

 TFindJava=class
  private
    fOwnPath : String;
    fSimPath : String;
    fSimExe : String;
    fJavaPath : String;
    fIsSimFound : Boolean;
    fIsSimInProgramFolder : Boolean;
    fIsSimInUserFolder : Boolean;
    Procedure InitPaths;
    Function IsJavaPath(Path : String) : Boolean;
    Function IsJavaInSubFolder(BasePath : String; Level : Integer) : Boolean;
    Function SearchRegistry(Mode : String) : Boolean;
    Procedure SearchJava;
  public
    Constructor Create;
  published
    property OwnPath : String read fOwnPath;
    property SimPath : String read fSimPath;
    property JavaPath : String read fJavaPath;
    property SimExe : String read fSimExe;
    property IsSimFound : Boolean read fIsSimFound;
    property IsSimInProgramFolder : Boolean read fIsSimInProgramFolder;
    property IsSimInUserFolder : Boolean read fIsSimInUserFolder;
end;

implementation

uses Forms, Registry;

constructor TFindJava.Create;
begin
  inherited Create;
  InitPaths;
  SearchJava;
end;

procedure TFindJava.InitPaths;
Var
  exe : String;
  i : Integer;
begin
  exe:=Application.ExeName;

  i:=exe.LastIndexOf(PathDelim);
  fOwnPath:=copy(exe,1,i+1);

  fSimPath:=copy(exe,1,i);
  i:=fSimPath.LastIndexOf(PathDelim);
  fSimPath:=copy(fSimPath,1,i+1);

  fSimExe:=fSimPath+'Simulator.exe';

  fIsSimFound:=FileExists(fSimExe);
  fIsSimInProgramFolder:=LowerCase(fSimPath).StartsWith('c:\program files\');
  fIsSimInUserFolder:=not fIsSimInProgramFolder;
end;

function TFindJava.IsJavaPath(Path: String): Boolean;
Var
  FullPath : String;
  JavaMainFile1 : String;
  JavaMainFile2 : String;
begin
  FullPath:=IncludeTrailingPathDelimiter(Path);
  JavaMainFile1:=FullPath+'javaw.exe';
  JavaMainFile2:=FullPath+'bin'+PathDelim+'javaw.exe';
  Result:=FileExists(JavaMainFile1);
  If Result then begin fJavaPath:=JavaMainFile1; Exit; end;
  Result:=FileExists(JavaMainFile2);
  If Result then begin fJavaPath:=JavaMainFile2; Exit; end;
end;

function TFindJava.IsJavaInSubFolder(BasePath: String; Level: Integer): Boolean;
Var
  Rec : TSearchRec;
  I : Integer;
  Path : String;
begin
  Result:=False;
  BasePath:=IncludeTrailingPathDelimiter(BasePath);
  I:=FindFirst(BasePath+'*.*',faDirectory,Rec);
  try
    While I=0 do begin
      if (Rec.Name<>'.') and (Rec.Name<>'..') then begin
        Path:=BasePath+Rec.Name;
        If IsJavaPath(Path) then begin
          Result:=True;
          Exit;
        end;
        if Level>1 then begin
          if IsJavaInSubFolder(Path,Level-1) then begin
            Result:=True;
            Exit;
          end;
        end;
      end;
      I:=FindNext(Rec);
    end;
  finally
    FindClose(rec);
  end;
end;

function TFindJava.SearchRegistry(Mode: String): Boolean;
Var
  BaseKey : String;
  Registry : TRegistry;
  Version : String;
  JavaHome : String;
begin
  BaseKey:='SOFTWARE\JavaSoft\'+Mode;
  Result:=False;

  Registry:=TRegistry.Create(KEY_READ);
  try
    Registry.RootKey:=HKEY_LOCAL_MACHINE;
    If Registry.OpenKey(BaseKey,false) then begin
      Version:=Registry.ReadString('CurrentVersion');
      Registry.CloseKey();
      If Version<>'' then begin
        If Registry.OpenKey(BaseKey+'\'+Version,false) then begin
          JavaHome:=Registry.ReadString('JavaHome');
          Registry.CloseKey();
          If IsJavaPath(JavaHome) then begin
            Result:=True;
            Exit;
          end;
        end;
      end;
    end;
  finally
    Registry.Free;
  end;
end;

procedure TFindJava.SearchJava;
Var
  Paths : TStringArray;
  I : Integer;
begin
  fJavaPath:='';

  { 1a- in .\jdk directory (JRE Installed with application) }
  If IsJavaPath(SimPath+'jdk') then Exit;

  { 1b- in .\jre directory (JRE Installed with application) }
  If IsJavaPath(SimPath+'jre') then Exit;

  { 2 - in JAVA_HOME environment variable }
  If IsJavaPath(GetEnvironmentVariable('JAVA_HOME')) then Exit;

  { 3a- jdk in the registry }
  If SearchRegistry('JDK') then Exit;

  { 3b- jre in the registry }
  If SearchRegistry('Java Runtime Environment') then Exit;

  { 4a- in C:\Program Files\AdoptOpenJDK and its subfolders}
  If IsJavaInSubFolder('C:\Program Files\AdoptOpenJDK',1) then Exit;

  { 4b- in C:\Program Files\Java and its subfolders }
  If IsJavaInSubFolder('C:\Program Files\Java',1) then Exit;

  { 4c- in C:\Program Files\Amazon Corretto and its subfolders }
  If IsJavaInSubFolder('C:\Program Files\Amazon Corretto',1) then Exit;

  { 4d- in C:\Program Files and its subfolders }
  If IsJavaInSubFolder('C:\Program Files\',2) then Exit;

  { 5 - assume javaw.exe in current dir or PATH }
  Paths:=GetEnvironmentVariable('PATH').Split(';');
  For I:=0 to SizeOf(Paths) do begin
    If IsJavaPath(Paths[I]) then Exit;
  end;
end;

end.

