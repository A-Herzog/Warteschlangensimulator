unit download;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, fphttpclient;

Type

{ TJavaLoaderThread }

 TJavaLoaderThread=class(TThread)
  private
    fURL : String;
    fTempFile : String;
    fProgress : Integer;
    fError : String;
    fAbort : Boolean;
    fHTTPClient : TFPHTTPClient;
    fOnProgress : TNotifyEvent;
    Procedure DataReceived(Sender : TObject; Const ContentLength, CurrentPos : Int64);
    Procedure ProgressRun;
  protected
    procedure Execute; override;
  public
    Constructor Create(aURL : String; aTempFile : String);
    Destructor Destroy; override;
    Procedure AbortDownload;
  published
    property Progress : Integer read fProgress;
    property Error : String read fError;
    property Abort : Boolean read fAbort;
    property OnProgress : TNotifyEvent read fOnProgress write fOnProgress;
end;

implementation

constructor TJavaLoaderThread.Create(aURL: String; aTempFile: String);
begin
  inherited Create(true);
  fURL:=aURL;
  fTempFile:=aTempFile;
  fProgress:=0;
  fError:='';
  fAbort:=False;
  fHTTPClient:=TFPHTTPClient.Create(nil);
  fOnProgress:=nil;
end;

destructor TJavaLoaderThread.Destroy;
begin
  fHTTPClient.Free;
  inherited Destroy;
end;

procedure TJavaLoaderThread.Execute;
Var
   Output : TFileStream;
begin
  try
    Output:=TFileStream.Create(fTempFile,fmCreate+fmOpenWrite);
    try
       fHTTPClient.AllowRedirect:=true;
       fHTTPClient.OnDataReceived:=@DataReceived;
       fHTTPClient.Get(fURL,Output);
     finally
       Output.Free;
     end;
  except
    on E: Exception do begin
      fError:=E.Message;
      fProgress:=0;
    end;
  end;
  fProgress:=100;
end;

procedure TJavaLoaderThread.AbortDownload;
begin
  fHTTPClient.Terminate;
  fAbort:=True;
end;

procedure TJavaLoaderThread.DataReceived(Sender: TObject; const ContentLength,
  CurrentPos: Int64);
Var
   NewProgress : Integer;
begin
  if ContentLength<1 then exit;
  NewProgress:=Round(100*CurrentPos/ContentLength);
  if NewProgress>fProgress then begin
    fProgress:=NewProgress;
    synchronize(@ProgressRun);
  end;
end;

procedure TJavaLoaderThread.ProgressRun;
begin
  if Assigned(fOnProgress) then fOnProgress(self);
end;

end.

