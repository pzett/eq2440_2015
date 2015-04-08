function [thetahat,xhat]=rls(x,y,N,lambda)

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
M=numel(y);
xhat=zeros(M,1);
thetahat=zeros(N,M-N);
ytemp=zeros(N,1);
I=eye(N);
P=zeros(N,N);
K=zeros(N,M-N);
P0=10000*I;

% Loop

for n=1:(M-N-1)
    
    % Generate Y(n). Set elements of Y that does not exist to zero
    ytemp=y(n:n+N-1);
    ytemp=flip(ytemp,1);
    
    if n==1
        
        % Estimate of x
        xhat(n+N)=ytemp'*zeros(N,1);
        % Update K
        K(:,n)=(P0*ytemp)/(lambda+ytemp'*P0*ytemp);
        % Update P
        P=(P0-K(:,n)*(ytemp'*P0))/lambda;
        % Update the n+1 row in the matrix thetahat which in the 
        % notation in the Lecture Notes corresponds to thetahat(n)
        thetahat(:,n)=K(:,n)*(x(n+N)-xhat(n+N));
        
    else
        % Estimate of x
        xhat(n+N)=ytemp'*thetahat(:,n-1);
        % Update K
        K(:,n)=(P*ytemp)/(lambda+ytemp'*P*ytemp);
        % Update P
        P=(P-K(:,n)*ytemp'*P)/lambda;
        % Update the n+1 row in the matrix thetahat which in the 
        % notation in the Lecture Notes corresponds to thetahat(n)
        thetahat(:,n)=thetahat(:,n-1)+K(:,n)*(x(n+N)-xhat(n+N));
        
        
    end

end


end
