unit MainUnit;

{$mode objfpc}{$H+}

{DEFINE Testing}

interface

uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, StdCtrls, Buttons,
  ComCtrls, ExtCtrls, findjava, download, extract;

type

  { TMainForm }

  TMainForm = class(TForm)
    Fix3: TBitBtn;
    Cancel: TBitBtn;
    Fix1: TBitBtn;
    Fix2: TBitBtn;
    InfoLabel: TLabel;
    TopInfoLabel: TLabel;
    Label2: TLabel;
    ProgressBar: TProgressBar;
    Shape1: TShape;
    procedure CancelClick(Sender: TObject);
    procedure Fix1Click(Sender: TObject);
    procedure Fix2Click(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure FormDestroy(Sender: TObject);
    procedure FormShow(Sender: TObject);
  private
    fDownloadURL : String;
    fHomepageURL : String;
    fTempZipFile : String;
    fFindJava : TFindJava;
    fDownloadThread : TJavaLoaderThread;
    fExtractThread : TExtractJava;
    fWindowsOnARM : Boolean;
    Procedure UpdateProgress(Sender: TObject);
    Procedure Step1Done(Sender: TObject);
    Procedure Step2Done(Sender: TObject);
  public

  end;

var
  MainForm: TMainForm;

implementation

uses
  lclintf, ShellApi, testarm;

{$R *.lfm}

{ TMainForm }

procedure TMainForm.FormCreate(Sender: TObject);
begin
  fFindJava:=TFindJava.Create;

  fDownloadURL:='https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/adoptium';
  fHomepageURL:='https://adoptium.net/';
  fTempZipFile:=IncludeTrailingPathDelimiter(GetTempDir(false))+'AdoptiumOpenJDK-Download.zip';

  fWindowsOnARM:=IsARM;
end;

procedure TMainForm.FormDestroy(Sender: TObject);
begin
  fFindJava.Free;
  fDownloadThread.Free;
  fExtractThread.Free;
end;

procedure TMainForm.FormShow(Sender: TObject);
Var
  SimFound : Boolean;
  JavaFound : Boolean;
  UserMode : Boolean;
  ForceAutoDownload : Boolean;
begin
  {$IFDEF Testing}
  SimFound:=True;
  JavaFound:=False;
  {$ELSE}
  SimFound:=fFindJava.IsSimFound;
  JavaFound:=fFindJava.JavaPath<>'';
  {$ENDIF}
  UserMode:=SimFound and fFindJava.IsSimInUserFolder;
  ForceAutoDownload:=False;

  if (Application.ParamCount=1) and (LowerCase(Application.Params[1])='/forceautodownload') then begin
    ForceAutoDownload:=True;
  end;

  Fix2.Enabled:=UserMode and not fWindowsOnARM;

  if not SimFound then begin
    MessageDlg('Warteschlangensimulator not found','No Warteschlangensimulator installation was found.',mtError,[mbOk],0);
    Close;
    Exit;
  end;

  If JavaFound and not ForceAutoDownload then begin
    MessageDlg('Java environment found','A Java environment was found on your system'+#13+'('+fFindJava.JavaPath+').'+#13+'There is no need to download Java.',mtInformation,[mbOk],0);
    Close;
    Exit;
  end;

  if fWindowsOnARM then begin
    InfoLabel.Caption:='A Windows on ARM Java environment cannot be installed automatically.';
    fHomepageURL:='https://docs.microsoft.com/de-de/java/openjdk/download';
    Exit;
  end;

  if UserMode then begin
    InfoLabel.Caption:='Automatic installation is recommended and will not require admin rights.'+#13+'A manually downloaded Java installer will need admin rights for installation.';
  end else begin
    InfoLabel.Caption:='Since Warteschlangensimulator is installed in program folder no automatic Java installation (without admin rights) is possible.';
  end;

  if ForceAutoDownload then begin
    If not UserMode then begin
      MessageDlg('No automatic Java installation possible','Since Warteschlangensimulator is installed in program folder no automatisch Java installation (without admin rights) is possible.',mtError,[mbOk],0);
      Close;
      Exit;
    end;
    TopInfoLabel.Caption:='A Java Development Kit (JDK) installation is needed to execute user-defined Java code in Warteschlangensimulator. This tool will download and install JDK.';;
    InfoLabel.Caption:='Automatic download and installation is started.';
    Fix2Click(Sender);
  end;
end;

procedure TMainForm.Fix1Click(Sender: TObject);
begin
  MessageDlg('Download Java environment','Please download a "AArch64 / ARM64" Java build for your system from the browser window which will be opened next.',mtInformation,[mbOK],0);
  OpenURL(fHomepageURL);
  Close;
end;

procedure TMainForm.Fix2Click(Sender: TObject);
begin
  Fix1.Enabled:=False;
  Fix2.Enabled:=False;
  Fix3.Enabled:=False;
  Cancel.Enabled:=True;

  fDownloadThread.Free;
  fDownloadThread:=TJavaLoaderThread.Create(fDownloadURL,fTempZipFile);
  fDownloadThread.OnProgress:=@UpdateProgress;
  fDownloadThread.OnTerminate:=@Step1Done;
  fDownloadThread.Start;
end;

procedure TMainForm.CancelClick(Sender: TObject);
begin
  fDownloadThread.AbortDownload;
  Cancel.Enabled:=False;
end;

procedure TMainForm.UpdateProgress(Sender: TObject);
begin
  If Sender=fDownloadThread then begin
    ProgressBar.Position:=fDownloadThread.Progress;
  end;
  If Sender=fExtractThread then begin
    ProgressBar.Position:=100+round(fExtractThread.Progress/2);
  end;
end;

procedure TMainForm.Step1Done(Sender: TObject);
begin
  Cancel.Enabled:=false;

  If fDownloadThread.Error<>'' then begin
    MessageDlg('Download failed',fDownloadThread.Error,mtError,[mbOK],0);
    ProgressBar.Position:=0;
    Fix1.Enabled:=True;
    Fix2.Enabled:=True;
    Fix3.Enabled:=True;
    DeleteFile(fTempZipFile);
    exit;
  end;

  If fDownloadThread.Abort then begin
    MessageDlg('Download failed','The download was canceled by the user.',mtError,[mbOK],0);
    ProgressBar.Position:=0;
    Fix1.Enabled:=True;
    Fix2.Enabled:=True;
    Fix3.Enabled:=True;
    DeleteFile(fTempZipFile);
    exit;
  end;

  fExtractThread:=TExtractJava.Create(fTempZipFile,IncludeTrailingPathDelimiter(fFindJava.SimPath)+'JDK');
  fExtractThread.OnProgress:=@UpdateProgress;
  fExtractThread.OnTerminate:=@Step2Done;
  fExtractThread.Start;
end;

procedure TMainForm.Step2Done(Sender: TObject);
const SW_SHOW=5;
begin
  if fExtractThread.Success then begin
    MessageDlg('Java installation complate','A Java environment was downloaded and installed for Warteschlangensimulator. Warteschlangensimulator will be started now.',mtInformation,[mbOk],0);
    ProgressBar.Position:=150;
  end else begin
    MessageDlg('Extracting zip file failed','Extracting the downloaded zip file failed.',mtError,[mbOK],0);
    ProgressBar.Position:=0;
  end;

  Fix1.Enabled:=True;
  Fix2.Enabled:=True;
  Fix3.Enabled:=True;
  DeleteFile(fTempZipFile);

  if fExtractThread.Success then begin
    ShellExecute(Handle,'open',PChar(fFindJava.SimExe),nil,PChar(fFindJava.SimPath),SW_SHOW);
    Close;
  end;
end;

end.

