unit extract;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, zipper;

Type

{ TExtractJava }

 TExtractJava=class(TThread)
  private
    fZipFile : String;
    fTempFolder : String;
    fFolder : String;
    fUnZipper: TUnZipper;
    fProgress : Integer;
    fOnProgress : TNotifyEvent;
    fSuccess : Boolean;
    function GetSubFolder(ParentFolder: String): String;
    procedure ProgressRun;
    Procedure UnZipProgress(Sender : TObject; Const Pct : Double);
  protected
    procedure Execute; override;
  public
    Constructor Create(aZipFile : String; aFolder : String);
  published
    property Progress : Integer read fProgress;
    property Success : Boolean read fSuccess;
    property OnProgress : TNotifyEvent read fOnProgress write fOnProgress;
end;


implementation

{ TExtractJava }

constructor TExtractJava.Create(aZipFile: String; aFolder: String);
begin
  inherited Create(true);
  fZipFile:=aZipFile;
  fTempFolder:=IncludeTrailingPathDelimiter(GetTempDir(false))+'JavaTemp';
  fFolder:=aFolder;
  fProgress:=0;
  fSuccess:=False;
end;

function TExtractJava.GetSubFolder(ParentFolder : String) : String;
Var
  I : Integer;
  Rec : TSearchRec;
begin
  Result:='';
  ParentFolder:=IncludeTrailingPathDelimiter(ParentFolder);
  I:=FindFirst(ParentFolder+'*.*',faDirectory,Rec);
  try
    While I=0 do begin
      if (Rec.Name<>'.') and (Rec.Name<>'..') then begin
        Result:=ParentFolder+Rec.Name;
        Exit;
      end;
      I:=FindNext(Rec);
    end;
  finally
    FindClose(Rec);
  end;
end;

procedure TExtractJava.Execute;
Var
  SubFolder : String;
begin
  fUnZipper:=TUnZipper.Create;
  try
    fUnZipper.OnProgress:=@UnZipProgress;
    fUnZipper.FileName:=fZipFile;
    mkdir(fTempFolder);
    fUnZipper.OutputPath:=fTempFolder;
    fUnZipper.UnZipAllFiles();

    SubFolder:=GetSubFolder(fTempFolder);
    if SubFolder='' then Exit;

    RenameFile(SubFolder,fFolder);

    rmdir(fTempFolder);

    fSuccess:=True;
  finally
    fUnZipper.Free;
  end;
end;

procedure TExtractJava.UnZipProgress(Sender: TObject; const Pct: Double);
Var
  NewProgress : Integer;
begin
  NewProgress:=round(Pct*100);
  if NewProgress>fProgress then begin
    fProgress:=NewProgress;
    synchronize(@ProgressRun);
  end;
end;

procedure TExtractJava.ProgressRun;
begin
  if Assigned(fOnProgress) then fOnProgress(self);
end;


end.

