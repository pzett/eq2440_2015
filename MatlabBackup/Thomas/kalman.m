function [thetahat,xhat]=kalman(x,y,N,R1,R2)

% [thetahat,xhat]=rls(x,y,N,lambda)
%
%	x			- Data sequence
%	y			- Data sequence
%	N			- Dimension of the parameter vector
%	lambda			- Forgetting factor
%	thetahat		- Matrix with estimates of theta. 
%				  Row n corresponds to time n-1
%	xhat			- Estimate of x for n=1
%
%
%
%  rls: Recursive Least-Squares Estimation
%
% 	Estimator: xhat(n)=Y^{T}(n)thetahat(n-1)
%
%	thetahat is estimated using RLS. 
%
%	Initalization:	P(0)=10000*I, thetahat(0)=0
%
%     
%     Author: 
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Initialize P, xhat and thetahat
M=length(x);
xhat=zeros(length(x),1);
thetahat=zeros(M+1,N);
P=1*eye(N);
% Loop

for n=1:M,

	% Generate Y(n). Set elements of Y that does not exist to zero
    Y=zeros(N,1);
    Y(1:min(N,n),1)=flip(y(max(1,n-N+1):n));

	% Estimate of x
    xhat(n,1)=thetahat(n,:)*Y;
    
	% Update K
    K=P*Y./(Y.'*P*Y+R2);

	% Update P
    P=P-K*Y.'*P+R1;


	% Update the n+1 row in the matrix thetahat which in the 
	% notation in the Lecture Notes corresponds to thetahat(n)

	thetahat(n+1,:)=thetahat(n,:)+(K*(x(n)-xhat(n,1))).';
end

% Shift thetahat one step so that row n corresponds to time n

thetahat=thetahat(2:M+1,:);
