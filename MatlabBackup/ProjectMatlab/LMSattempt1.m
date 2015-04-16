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
muu = 5e-3;
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

N=1024; % filter order
M=length(z);
C = 2;
xhat2=zeros(length(z),1);
thetahat2=zeros(1,N/C);

% Loop



tic
for n=1:M/2,

	% Generate Y. Set elements of Y that does not exist to zero
    Y1=zeros(N/C,1);
    Y1(1:min(N/C,(2*n-1)/C),1)=flip(y(max(1,2*n-N):C:2*n-C));
    
    Y2=zeros(N/C,1);
    Y2(1:min(N/C,(2*n)/C),1)=flip(y(max(1,2*n-N+1):C:2*n-C+1));
    
	% Estimate of x
    xhat2(2*n-1,1)=thetahat2*Y1;
    xhat2(2*n,1)=thetahat2*Y2;
    
	% Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
	% corresponds to thetahat(n)
    thetahat2=thetahat2+(muu*Y2*(z(2*n)-xhat2(2*n,1))).';
end
toc

%%
% M-ESTIMATE LMM algorithm HAMPELS three-part redescending M-estimate


N=1024; % filter order
M=length(z);
xhat4=zeros(length(z),1);
C = 1;
D = 1;
thetahat=zeros(1,N/C);
threshold1 = 0.2;
threshold2 = 0.6;
counter1 = 0;
counter2 = 0;


% Loop
tic
for n=1:M,

	% Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(N/C,1);
    Y(1:min(N/C,n/C),1)=flip(y(max(1,n-N+1):C:n-C+1));
    
	% Estimate of x
    xhat4(n,1)=thetahat*Y;

	% Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
	% corresponds to thetahat(n)
    e = z(n)-xhat4(n,1);
    if abs(e)<threshold1
        thetahat=thetahat+(muu*Y*e).';
        counter1 = counter1+1;
    elseif abs(e)<threshold2
        thetahat=thetahat+(muu*Y*threshold1*sign(e)).';
        counter2 = counter2+1;
    else
    end
end
toc


%%
shatLMS=z-xhat;
soundsc(z-xhat,fs);
pause(8)
soundsc(z,fs);
PLOT=1;
if PLOT
figure
pwelch(y,3000);
figure
pwelch(shatLMS,3000);
end


%%
MSE  = sum((rec1-(z-xhat4)).^2)/length(z);


%%
figure
plot(z-xhat4)
