%Just downsampling and looking at the quality - Jonas Sedin
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
Nn = 2;
z=downsample(rec1,Nn);
y=downsample(rec2,Nn);
muu = 10e-3;
fs=fs/Nn;
%%

N=512; % filter order
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

soundsc(z-xhat,fs)

%%
figure
plot(z-xhat)








