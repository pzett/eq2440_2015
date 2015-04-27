%VOLTERRA LMS - Jonas Sedin
% I tried some Volterra-like filter, but I am pretty sure I am doing it
% wrong

clc; clear; close all;
%addpath('mfiles');

% load data
[rec1,fs]=audioread('VOICENOISE1.m4a');
z=rec1(900000:1250000);
[rec2,fs]=audioread('NOISE1.m4a');
y=rec2(900000:1250000);
%rec1=rec1(900000:904096);
%rec2=rec2(900000:904096);
% model
% rec1= z(n)=s(n)+x(n), s(n)=signal, x(n)=noise
% rec2= y(n)          , y(n)=noise
% x(n), y(n) correlated
% estimate via noise subtraction
% s^(n)=z(n)-x^(n)
muu1 = 5e-3;
muu2 = 1e-2;
muu3 = 9e-2;

N=1024; % filter order
M=length(z);
xhat=zeros(length(z),1);
C = 1;
D = 1;
thetahat=zeros(1,N/C);
thetahat2=zeros(1,N/C);
thetahat3=zeros(1,N/C);
e=zeros(length(z),1);

% Loop
tic
for n=1:M,

	% Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(N/C,1);
    Y(1:min(N/C,n/C),1)=flip(y(max(1,n-N+1):C:n-C+1));
    Y2 = Y.^2;
    Y3 = Y2.*Y;
    
	% Estimate of x
    xhat(n,1)=thetahat2*Y2+thetahat2*Y2+thetahat3*Y3;

	% Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
	% corresponds to thetahat(n)
    e(n) = z(n)-xhat(n,1);
    if mod(n,D)==0
	thetahat=thetahat+(muu1*Y*(e(n))).';
    thetahat2=thetahat2+(muu2*Y2*(e(n))).';
    thetahat3=thetahat3+(muu3*Y3*(e(n))).';
    end
end
toc


