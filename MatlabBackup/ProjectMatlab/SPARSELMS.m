%SPARSELMS

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


%%
muu=1e-3;
N=1000; % filter order
M=length(z);
xhat=zeros(length(z),1);
sparseC = 1;

thetahat=zeros(1,N/sparseC);



% Loop
tic
for n=1:M,

	% Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(N/sparseC,1);
    Y(1:min(N/sparseC,n/sparseC),1)=flip(rec2(max(1,n-N+1):sparseC:n-sparseC+1));
    
	% Estimate of x
    xhat(n,1)=thetahat*Y;

	% Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
	% corresponds to thetahat(n)

	thetahat=thetahat+(muu*Y*(rec1(n)-xhat(n,1))).';
    
end
toc

%%
shatLMS=z-xhat;
soundsc(z-xhat,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatLMS,3000);
end
plot(z)



