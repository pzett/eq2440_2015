function [thetahat,xhat]=lms(x,y,N,muu)

% [thetahat,xhat]=lms(x,y,N,muu)
%
%	x			- Data sequence
%	y			- Data sequence
%	N			- Dimension of the parameter vector
%	muu			- Step size
%	thetahat		- Matrix with estimates of theta.
%				  Row n corresponds to the estimate thetahat(n)'
%	xhat			- Estimate of x
%
%
%
%  lms: The Least-Mean Square Algorithm
%
% 	Estimator: xhat(n)=Y^{T}(n)thetahat(n-1)
%
%	thetahat is estimated using LMS.
%
%
%     Author: Xavier Bush and Carolina Millet
%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Initialize xhat and thetahat
M=numel(y);
xhat=zeros(M,1);
thetahat=zeros(N);
ytemp=zeros(N,1);

% Loop

for n=1:(M-N-1)
    
    % Generate Y. Set elements of Y that does not exist to zero
    
    ytemp=y(n:n+N-1);
    ytemp=flip(ytemp,1);
    if n==1
        xhat(n+N)=0; %thetahat(n-1) does not exist in the first iteration
        thetahat=muu*ytemp*(x(n+N)-xhat(n+N));
        
    else
        xhat(n+N)=ytemp'*thetahat;
        if mod(n,3)==0
            thetahat=thetahat+muu*ytemp*(x(n+N)-xhat(n+N));
        end
        
    end
    if (xhat(n+N)>=0)
        xhat(n+N)=floor(xhat(n+N));
    else
        xhat(n+N)=floor(xhat(n+N))+1;
    end
    
    % Estimate of x
    
    
    % Update the n+1 row in the matrix thetahat which in the notation in the Lecture Notes
    % corresponds to thetahat(n)
    
end

% Shift thetahat one step so that row n corresponds to time n

%thetahat=thetahat(2:M+1,:);

end
