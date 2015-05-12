clc; clear; close all;
%addpath('mfiles');

% load data
[rec1,fs]=audioread('VOICENOISE1.m4a');
rec1=rec1(900000:1250000);
[rec2,fs]=audioread('NOISE1.m4a');
rec2=rec2(900000:1250000);
%rec1=rec1(900000:904096);
%rec2=rec2(900000:904096);
% model
% rec1= z(n)=s(n)+x(n), s(n)=signal, x(n)=noise
% rec2= y(n)          , y(n)=noise
% x(n), y(n) correlated
% estimate via noise subtraction
% s^(n)=z(n)-x^(n)
downsampleflag = true;
downsamplefactor = 2;

if downsampleflag ==true
    z = downsample(rec1,downsamplefactor);
    y = downsample(rec2,downsamplefactor);
    fs=fs/downsamplefactor;
else
z=rec1;
y=rec2;
end
muu = 5e-3;
clear rec1 rec2
%%

N=1000; % filter order
M=length(z);
xhat=zeros(length(z),1);
C = 1;
D = 1;
thetahat=zeros(1,N/C);
e=zeros(length(z),1);
K = N/C;
% Loop

tic
for n=1:M,

	% Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(K,1);
    Y(1:min(K,n/C),1)=flip(y(max(1,n-N+1):C:n-C+1));
    
	% Estimate of x
    xhat(n,1)=thetahat*Y;

	% Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
	% corresponds to thetahat(n)
    e(n) = z(n)-xhat(n,1);
    if mod(n,D)==0
	thetahat=thetahat+(muu*Y*(e(n))).';
    end
end
toc

%%
% output=MMSESTSA84(signal,fs,IS)
% Shor time Spectral Amplitude Minimum Mean Square Error Method for
% Denoising noisy speech. based on Ephraim et al (1984) paper under the
% same title. signal is the input noisy speech, fs is its sampling
% frequency and IS (which is optional) is the initial silence estimate (The
% default value is 0.25 which means that it is assumed that the first 0.25
% seconds of the signal is speech inactive period and may be used for
% initial noise parameter estimation. IS may also be used as a structure
% for compatibility with my other functions but please do not try to use IS
% it in that way as its functionality is not reliable.
% The output is the restored estimate of clean speech.
% Author: Esfandiar Zavarehei
% Created: Dec-04
% Last Modified: 21-01-05
IS = 0.25;
signal = e;

W=fix(.025*fs); %Window length is 25 ms
SP=0.4; %Shift percentage is 40% (10ms) %Overlap-Add method works good with this value(.4)
wnd=hamming(W);


NIS=fix((IS*fs-W)/(SP*W) +1);%number of initial silence segments

y=segmentation(signal,W,SP,wnd); % This function chops the signal into frames
Y=fft(y);
YPhase=angle(Y(1:fix(W/2)+1,:)); %Noisy Speech Phase
Y=abs(Y(1:fix(W/2)+1,:));%Specrogram
numberOfFrames=size(Y,2);


N=mean(Y(:,1:NIS)')'; %initial Noise Power Spectrum mean
LambdaD=mean((Y(:,1:NIS)').^2)';%initial Noise Power Spectrum variance
alpha=.99; %used in smoothing xi (For Deciesion Directed method for estimation of A Priori SNR)

NoiseCounter=0;
NoiseLength=9;%This is a smoothing factor for the noise updating

G=ones(size(N));%Initial Gain used in calculation of the new xi
Gamma=G;

Gamma1p5=gamma(1.5); %Gamma function at 1.5
X=zeros(size(Y)); % Initialize X (memory allocation)

h=waitbar(0,'Wait...');
tic
for i=1:numberOfFrames
    %%%%%%%%%%%%%%%%VAD and Noise Estimation START
    if i<=NIS % If initial silence ignore VAD
        SpeechFlag=0;
        NoiseCounter=100;
    else % Else Do VAD
        [SpeechFlag, NoiseCounter]=vad(Y(:,i),N,NoiseCounter); %Magnitude Spectrum Distance VAD
    end
    
    if SpeechFlag==0 % If not Speech Update Noise Parameters
        N=(NoiseLength*N+Y(:,i))/(NoiseLength+1); %Update and smooth noise mean
        LambdaD=(NoiseLength*LambdaD+(Y(:,i).^2))./(1+NoiseLength); %Update and smooth noise variance
    end
    %%%%%%%%%%%%%%%%%%%VAD and Noise Estimation END
    
    gammaNew=(Y(:,i).^2)./LambdaD; %A postiriori SNR
    xi=alpha*(G.^2).*Gamma+(1-alpha).*max(gammaNew-1,0); %Decision Directed Method for A Priori SNR
    
    Gamma=gammaNew;
    nu=Gamma.*xi./(1+xi); % A Function used in Calculation of Gain
    
    G= (xi./(1+xi)).*exp(.5*expint(nu)); % Log spectral MMSE [Ephraim 1985]
    X(:,i)=G.*Y(:,i); %Obtain the new Cleaned value
    
    waitbar(i/numberOfFrames,h,num2str(fix(100*i/numberOfFrames)));
end
toc
close(h);
output=OverlapAddCustom(X,YPhase,W,SP*W); %Overlap-add Synthesis of speech


%%

