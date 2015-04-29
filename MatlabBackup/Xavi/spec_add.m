function [PhixyNum,PhixyDen,PhiyyNum,PhiyyDen]=spec_add(A,sigma2,Anoise,sigma2noise)

[PhixxNum,PhixxDen]=filtspec(1,A,sigma2);
[PhieeNum,PhieeDen]=filtspec(1,Anoise,sigma2noise);

PhixyNum = PhixxNum; %Because x and e are independent 'y=x+e'
PhixyDen = PhixxDen;

%Building the combinated/added numerators and denominators to be used
%further
[PhiyyNum,PhiyyDen]=add(PhixxNum,PhixxDen,PhieeNum,PhieeDen);

end