%WIENER PLUS LMS

clc; clear; close all;

% load data
[rec1,fs]=audioread('VOICENOISE1.m4a');
rec1=rec1(900000:1250000);
[rec2,fs]=audioread('NOISE1.m4a');
rec2=rec2(900000:1250000);

z=rec1;
y=rec2;
muu = 2e-3;
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

%% WIENER FILTER ATTEMPT JONAS SEDIN

order = 20;
csize = 3500;

plength = (size(y,1)-1)/csize;
acfExt = 0;


noise = e(1:5000);

acfvv=xcorr(noise,order,'biased');
rvv = acfvv(1+order:(2*order));




%Some initializations
xhat2 = zeros(1,1);
rxx = zeros(order,plength);
Ryy = zeros(order,order,plength);
w = zeros(order,plength); 


for i = 0:plength-1
    yfilterSeq = e(i*csize+1:(i+1)*csize);
    %NOISE-ACTIVITY-VOISE DETECTION HERE
    variance = var(yfilterSeq);
    %
    if(variance<=0.02)
        acfvv=xcorr(yfilterSeq,order,'biased');
        rvvtemp = acfvv(1+order:(2*order));
        rvv = rvvtemp;
    end
        % ySeq is used for the autocorrelation sequence. It's bigger than
        % the sequence that we filter on, to try and make it jump around
        % less
        if i == 0
            ySeq = e(1:csize+acfExt);
        elseif i == plength-1
                ySeq = e(i*csize+1-acfExt:(i+1)*csize);
            else
            ySeq = e(i*csize+1-acfExt:(i+1)*csize+acfExt); 
        end
 
        %This is where we create the wiener-taps
        acfyy=xcorr(yfilterSeq,yfilterSeq,order,'biased'); % autocorrelation
        ryy=acfyy(1+order:(2*order)); % discard all negative lags
        rxx(:,i+1) = (ryy-rvv);%+ones(order,1)*0.0001;
        Ryy(:,:,i+1) = toeplitz(ryy);
        w(:,i+1) = Ryy(:,:,i+1)\rxx(:,i+1);
        
        %This is where we try to average to wiener taps inorder to reduce
        %the noise from jumping around too much.
        % w(n) = (w(n)+w(n-1)+w(n-2))/3 
        %if i == 0
        %    w(:,i+1) = w1;
        %elseif i == 1
        %        w(:,i+1) = (w1 + w(:,i))/2;
        %    else
        %    w(:,i+1) = (w1 + w(:,i) + w(:,i-1))/3;
        %end

        %Concatenating the filtered sequence with the rest of the filtered
        %sequences
        xhat2 = vertcat(xhat2,filter(w(:,i+1),1,yfilterSeq));

end

%%
plot(xhat2)

soundsc(xhat2,fs)