%% Project 2
clc; clear; close all;
%addpath('mfiles');

% load data
[rec1,fs]=audioread('rec1.wav');
[rec2,fs]=audioread('rec2.wav');

% model
% rec1= z(n)=s(n)+x(n), s(n)=signal, x(n)=noise
% rec2= y(n)          , y(n)=noise
% x(n), y(n) correlated
% estimate via noise subtraction
% s^(n)=z(n)-x^(n)
z=rec1;
y=rec2;

% plot and sound
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(rec2,3000);
end
%soundsc(rec1,fs);
%soundsc(rec2,fs);

%% Kalman Filter


N=5; % filter order
R1=0.0001*eye(N);
R2=0.00001; %var(y)
[thetahatKF,xhatKF]=kalman(z,y,N,R1,R2);
shatKF=z-xhatKF;
%soundsc(shatKF,fs);
%soundsc(rec1,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatKF2,3000);
end

%% LMS
muu=1000;
N=5; % filter order
[thetahatLMS,xhatLMS]=lms(z,y,N,muu);
shatLMS=z-xhatLMS;
%soundsc(shatLMS,fs);
%soundsc(rec1,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatLMS,3000);
end

%% NLMS
muu=1.5;
N=5; % filter order
[thetahatNLMS,xhatNLMS]=nlms(z,y,N,muu);
shatNLMS=z-xhatNLMS;
%soundsc(shatNLMS,fs);
%soundsc(rec1,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatNLMS,3000);
end

%% RLS
lambda=0.996;
N=5; % filter order
[thetahatRLS,xhatRLS]=rls(z,y,N,lambda);
shatRLS=z-xhatRLS;
%soundsc(shatRLS,fs);
%soundsc(rec1,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatRLS,3000);
end

%% FIR Wiener
N=20;
corrNoise=xcorr(y,N);
corrNoise=corrNoise(N+1:2*N+1);
corrSignal=xcorr(z,N);
corrSignal=corrSignal(N+1:2*N+1);
SigmaYx=corrSignal-corrNoise;
SigmaYY=toeplitz(corrSignal);
wiener=SigmaYY\SigmaYx;
xhatWiener=filter(wiener,1,z);
%soundsc(xhatWiener,fs);
%soundsc(rec1,fs);
PLOT=0;
if PLOT
figure
pwelch(rec1,3000);
figure
pwelch(xhatWiener,3000);
end
