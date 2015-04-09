% test_harness_example.m
%
% This code shows how to use test_harness.
%
% In this trivial example, we want to test a function which takes a sequence of
% real numbers (doubles) and squares them.
% You will need to edit eclipsec and FrameWork variables for your
% installation.
% Function to be tested is square.m
%
% Copyright KTH Royal Institute of Technology, Martin Ohlsson, Per Zetterberg
% This software is provided  ’as is’. It is free to use for non-commercial purposes.
% For commercial purposes please contact Peter Händel (peter.handel@ee.kth.se)
% for a license. For non-commercial use, we appreciate citations of our work,
% please contact, Per Zetterberg (per.zetterberg@ee.kth.se), 
% for how information on how to cite. 



if ~exist('been_here')
    fprintf(1,'Set do_test=true in test_harness(), press any key when ready \n');
    pause
    been_here=1;
end;

% This variable has to point to the correct apk file.
PathToAPKFile=' C:\Users\EQ2440\Desktop\Project Workspace\Task7\bin\FrameWork.apk';

% Generate random data input, and save on the file indata.txt
no_of_real=4096;
data_in=randn(1,no_of_real);
fid=fopen('indata.txt','wt');
fprintf(fid,'%d \n',no_of_real);
for i1=1:no_of_real
    fprintf(fid,'%f \n',data_in(i1));
end;
fclose(fid);
data_in2=randn(1,no_of_real);
fid2=fopen('indata2.txt','wt');
fprintf(fid2,'%d \n',no_of_real);
for i1=1:no_of_real
    fprintf(fid2,'%f \n',data_in2(i1));
end;
fclose(fid2);

% Copy file to sdcard
copy_file_from_working_directory_to_sdcard('indata.txt');
copy_file_from_working_directory_to_sdcard('indata2.txt');

pause(1);

% Install the APK
cmd_str=['adb install -r ',PathToAPKFile];
system(cmd_str);

pause(1);

% Run the framework
cmd_str='adb shell am start -a android.intent.action.MAIN -n se.kth.android.FrameWork/se.kth.android.FrameWork.FrameWork';
system(cmd_str);

pause(10);

% Copy the results back to working directory
copy_file_from_sdcard_to_working_directory('outdata.txt');

% Read the results from the file
data_out_phone=load('outdata.txt');
muu=0.001;
order=400;
[thetahat,data_out_matlab]=lmsXavi(data_in',data_in2',order,muu);

figure(1);
hold off
plot(data_in,data_out_matlab,'x');
hold on
plot(data_in,data_out_phone,'o');
xlabel('Input values');
ylabel('Output values');
legend('Matlab result','Phone result');
title('Compare matlab and phone, should be identical');
grid
figure(1);
