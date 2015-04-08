%Project 2 of Adaptive Signal Processing
clear all
close all
clc

%Decide de order of the filter

N=200

%Time analysis of recorded date
[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');

delay=0;
start1=750000;
end1=1000000;
y=noise1(start1:end1);
y=y/var(y);
x=voicenoise1(start1+delay:end1+delay);
period=1/fs;
t=[0:numel(y)-1]*period;


%Spectral analysis of recorded data
freq=linspace(0,fs,numel(x));
%plot(freq/1e3,20*log10(abs(xfft(1:end))))

covy=xcov(y);
covy=covy(end-numel(y)+1:end);
SigmaYY=toeplitz(covy(500:530));

%% LMS filtering

lambdavect=eig(SigmaYY);
lambdamax=max(lambdavect);
muu1max=(2/lambdamax);

%Deciding parameter muu
muu2=1e-5;
muu3=1e-4;
muulms=muu3

%Filtering
[thetahatlms,xhatlms]=lms(x,y,N,muu3);
[thetahatlmsThomas,xhatlmsThomas]=lmsThomas(x,y,N,muu3);
slms=x-xhatlms;
slmsThomas=x-xhatlmsThomas;


%% RLS filtering

%Deciding parameters: order and lambda
Nrls=20
lambda=0.997;

%Filtering
[thetahatrls,xhatrls]=rls(x,y,Nrls,lambda);
srls=x-xhatrls;