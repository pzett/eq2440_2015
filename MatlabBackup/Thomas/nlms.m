function [thetahat,xhat]=nlms(x,y,N,muu)

% [thetahat,xhat]=nlms(x,y,N,muu)
%
%	x			- Data sequence
%	y			- Data sequence
%	N			- Model order
%	muu			- Step size
%	thetahat		- Matrix with estimates of theta.
%				  Row n corresponds to the estimate thetahat(n)'
%	xhat			- Estimate of x
%
%
%
%  nlms: The Normalized Least-Mean Square Algorithm
%
% 	Estimator: xhat(n)=Y^{T}(n)thetahat(n-1)
%
%	thetahat is estimated using NLMS.
%
%
%     Author:
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Initialize xhat and thetahat
M=length(x);
xhat=zeros(length(x),1);
thetahat=zeros(M+1,N);

% Loop

for n=1:M,
    
    % Generate Y. Set elements of Y that does not exist to zero
    Y=zeros(N,1);
    Y(1:min(N,n),1)=flip(y(max(1,n-N+1):n));

    % Estimate of x
    xhat(n,1)=thetahat(n,:)*Y;
    
    % Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
    % corresponds to thetahat(n)
    c=0.001;
    muunorm=muu/(c+Y.'*Y);
    thetahat(n+1,:)=thetahat(n,:)+(muunorm*Y*(x(n)-xhat(n,1))).';
end

% Shift thetahat one step so that row n corresponds to time n

thetahat=thetahat(2:M+1,:);
