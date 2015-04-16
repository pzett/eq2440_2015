%WIENER PLUS LMS

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
z=rec1;
y=rec2;
muu = 2e-3;

%%
%VOICE2
clc; clear; close all;
[rec1,fs]=audioread('Voice2.m4a');
rec1=rec1(65000:196000);
[rec2,fs]=audioread('Noise2.m4a');
rec2=rec2(65000:196000);

z = rec1+rec2*0.5;
y=rec2;
muu = 1e-3;

%%

N=1024; % filter order
M=length(z);
xhat=zeros(length(z),1);
C = 1;
D = 1;
thetahat=zeros(1,N/C);
e=zeros(length(z),1);

% Loop
tic
for n=1:M,

	% Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(N/C,1);
    Y(1:min(N/C,n/C),1)=flip(y(max(1,n-N+1):C:n-C+1));
    
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

order=3;
chunksize=64*N;
plength= floor(M/chunksize);
noise = xhat(150000:160000);

acfvv=xcorr(xhat,order,'unbiased');
rvv=acfvv(1+order:(2*order));

xhat2=zeros(1,1);
rxx=zeros(order,1);
Ryy=zeros(order,order);

for j=1:plength
    
    XHAT = xhat((j-1)*chunksize+1:j*chunksize);
    
    acfyy=xcorr(XHAT,order,'unbiased');
    ryy=acfyy(1+order:(2*order));
    rxx=ryy-rvv;
    Ryy=toeplitz(ryy);
    w=Ryy\rxx;

    xhat2 = vertcat(xhat2,filter(w,1,XHAT));
end
%%
shatLMS=z-xhat;
soundsc(z-xhat,fs);
pause(8)
soundsc(xhat2,fs);
PLOT=1;
if PLOT
figure
pwelch(y,3000);
figure
pwelch(xhat2,3000);
end


%%
plot(xhat2)

