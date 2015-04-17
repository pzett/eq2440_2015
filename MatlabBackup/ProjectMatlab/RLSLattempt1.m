% Recursive Least Square Lattice Filter

clc; clear; close all;
%addpath('mfiles');

% load data
[rec1,fs]=audioread('VOICENOISE1.m4a');
rec1=rec1(900000:905000);
[rec2,fs]=audioread('NOISE1.m4a');
rec2=rec2(900000:905000);
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
M = length(z);

%%

%Initializations
xhat=zeros(length(z),1);
delta=0.05;
N = 10;
alpha = 0.999;

f = zeros(N+2,1);
b=zeros(N+2,M);

gamma=zeros(N+2,1);

beta = zeros(N+2,1);
eta = zeros(N+2,1);
xi = zeros(N+2,1);
h = zeros(N+2,1);

%START INITIALIZATIONS
gamma(1)=1;
f(1)=delta;
b(1,1)=delta;
kf=zeros(N+1,1);
kb=zeros(N+1,1);





for n=3:1:M
    %PER-CYCLE INITIALIZATIONS
    eta(1) = y(n);
    beta(1) = y(n);
    
    f(1) = alpha*f(1) + (y(n))^2;
    b(1,n) = alpha*f(1) + (y(n))^2;
    
    gamma(1) = 1;
    
    h(1) = 0;
    
    xi(1) = z(n);
    
    
    
    for m=2:1:N+1
       %     Previous samples
       f(m-1)=alpha*f(m-1)+gamma(m-1)*eta(m-1)^2;
       
       b(m-1,n-1) = alpha*b(m-1,n-2)+gamma(m-1)*beta(m-1)^2;
       
       eta(m) = eta(m-1)+kf(m)*beta(m-1);
       
       beta(m) = beta(m-1)+kb(m)*eta(m-1);
       
       kf(m) = kf(m)-gamma(m-1)*beta(m-1)*eta(m)/b(m-1,n);
       
       kb(m) = kb(m)-gamma(m-1)*eta(m-1)*beta(m)/f(m-1);
       
       gamma(m)= gamma(m-1) - gamma(m-1)^2*beta(m-1)^2/b(m-1,n);
       
    end
    for m=2:1:N+2
       
       xi(m) = xi(m-1) + h(m-1)*beta(m-1);
       
       h(m-1)=h(m-1) + gamma(m-1)*beta(m-1)*xi(m)/b(m-1,n);
       
    end
       
       xhat(n) = xi(N+2);
       
end
        
     
        
        





