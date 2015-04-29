%Project 2 of Adaptive Signal Processing
clear all
close all
clc

%Decide de order of the filter

N=1000

%Time analysis of recorded date
[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');
[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');
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

[thetahatlms,xhatlms]=lmsXavi(x,y,N,muu3);
slms=x-xhatlms;



[thetahatlmsThomas,xhatlmsThomas]=lmsThomas(x,y,N,muu3);
slmsThomas=x-xhatlmsThomas;

%% RLS filtering

%Deciding parameters: order and lambda
Nrls=300
lambda=0.999;

%Filtering
tic
[thetahatrls,xhatrls]=rlsXavi(x,y,Nrls,lambda);
srls=x-xhatrls;
toc

%% KALMAN Filtering
dim=80;
%Setting parameters to put in kalman function
x0=zeros(dim,1);
%x0=zeros(numel(Alms),1);
Q0=ones(dim);
%Q0=zeros(numel(Alms));
F=eye(dim);
G=[1;zeros(dim-1,1)]; 
H=y(50001:50001+dim-1)';
R1=1;
R2=5;

[yhatkalman,xhatfilt,xhatpred,P,Q]=kalman(x,F,G,H,R1,R2,x0,Q0);