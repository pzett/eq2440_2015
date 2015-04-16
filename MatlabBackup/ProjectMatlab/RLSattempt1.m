%RLS-ATTEMPT

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
lambda= 0.999;
N=100;
%%

M=length(z);
xhat=zeros(length(z),1);
thetahat=zeros(1,N);
P=eye(N);
% Loop
tic
for n=1:M,

	% Generate Y(n). Set elements of Y that does not exist to zero
    Y=zeros(N,1);
    Y(1:min(N,n),1)=flip(y(max(1,n-N+1):n));

	% Estimate of x
    xhat(n,1)=thetahat*Y;
    
	% Update K
    K=P*Y./(lambda+Y.'*P*Y);

	% Update P
    P=(P-K*Y.'*P)./lambda;


	% Update the n+1 row in the matrix thetahat which in the 
	% notation in the Lecture Notes corresponds to thetahat(n)

	thetahat=thetahat+(K*(z(n)-xhat(n,1))).';
end
toc




