%% Here we are goin to do some Wiener Filtering
clear all
close all
clc

[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');
[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');
[noise1,fs]=audioread('NOISE1.m4a');
[voicenoise1,fs]=audioread('VOICENOISE1.m4a');

ini=750000;
fin=1400000;
data=voicenoise1(ini:fin);

T=1/fs;
window=10e-3/T;
data=data(1:window*floor(numel(data)/window));
t=linspace(1,fin*T,numel(data));

%plot(t,data)
varref=0.008;
svoiced=0;
sunvoiced=0;
x=zeros(1,numel(window));
a=0;
b=0;

sigma=zeros(1,floor(numel(data)/window));

for j=1:floor((numel(data)/window))
    sigma(1,j)=var(data(1+(j-1)*window:j*window));
end

figure(1)
subplot(2,1,1)
plot(data)
xlabel('Time(s)')
ylabel('Recording')

subplot(2,1,2)
plot(sigma)
xlabel('Blocks')
ylabel('Variance')

k=0; %index of snoise
m=0; %index of svoice

voiced=zeros(1,numel(data));
unvoiced=zeros(1,numel(data));
onezero=zeros(1,numel(data));

for i=1:(numel(data)/window)
    sigcomp=var(data(1+(i-1)*window:i*window));
    
    if sigcomp<varref
        k=k+1;
        sunvoiced(k)=sigcomp;
        x=data(1+(i-1)*window:i*window);
        
        if i==1 || a==1
           unvoiced(1,1:window)=x';
           b=1;
           a=0;
        else
            unvoiced(1,1+(k-1)*window:k*window)=x';
            
        end
        
    else
        onezero(1+(i-1)*window:i*window)=ones(1,window);
        m=m+1;
        svoiced(m)=sigcomp;
        x=data(1+(i-1)*window:i*window);
        
        if i==1 || b==1;
            voiced(1,1:window)=x';
            a=1;
            b=0;
        else
            voiced(1,1+(m-1)*window:m*window)=x';
        end
        
    end
end

voiced=voiced(1:m*window);
unvoiced=unvoiced(1:k*window);

figure
plot(data)
hold all
plot(onezero)

P=16; %Order of the AR processes

%Generating AR process of X estimated with frames with voice and noise
[A,sigma2]=arcov(voiced,P);

%Generating AR process of e(noise) estimated with noisy frames
[Anoise,sigma2noise]=arcov(unvoiced,P);

 %% Second Wiener Filter:CAUSAL
    m=0;%Delay of the causal
    [PhixyNum, PhixyDen, PhiyyNum, PhiyyDen]=spec_add(A,sigma2,Anoise,sigma2noise);
    [xhat,num,den]=cw(data,PhixyNum,PhixyDen,PhiyyNum,PhiyyDen,m);
    xhatcausal=xhat;
    numcausal=num;
    dencausal=den;
    
    
 %% Third Wiener Filter: NON-CAUSAL
 
     [PhixyNum, PhixyDen, PhiyyNum, PhiyyDen]=spec_add(A,sigma2,Anoise,sigma2noise);
     
     numnonc=conv(PhixyNum,PhiyyDen);
     dennonc=conv(PhixyDen, PhiyyNum);
     xhatnonc=ncfilt(numnonc,dennonc,data);