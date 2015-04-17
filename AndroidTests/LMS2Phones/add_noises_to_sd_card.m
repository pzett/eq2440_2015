%%Used to add the noises to the SD card

no_of_real=4096;

[drillinglong,Fs]=audioread('drillinglong.m4a');
drillinglong=drillinglong(900001:900000+no_of_real);
drillinglong=floor((drillinglong.*2^10)./max(drillinglong)); %Scaling the sound
fid1=fopen('drillinglong.txt','wt');
fprintf(fid1,'%d \n',no_of_real);
for i1=1:no_of_real
    fprintf(fid1,'%f \n',drillinglong(i1));
end;
fclose(fid1);

[drillingshort,Fs]=audioread('drillingshort.m4a');
drillingshort=drillingshort(150001:150000+no_of_real);
drillingshort=floor((drillingshort.*2^10)./max(drillingshort)); %Scaling the sound
fid2=fopen('drillingshort.txt','wt');
fprintf(fid2,'%d \n',no_of_real);
for i1=1:no_of_real
    fprintf(fid2,'%f \n',drillingshort(i1));
end;
fclose(fid2);

[drillingontable,Fs]=audioread('drillingontable.m4a');
drillingontable=drillingontable(400001:400000+no_of_real);
drillingontable=floor((drillingontable.*2^10)./max(drillingontable)); %Scaling the sound
fid3=fopen('drillingontable.txt','wt');
fprintf(fid3,'%d \n',no_of_real);
for i1=1:no_of_real
    fprintf(fid3,'%f \n',drillingontable(i1));
end;
fclose(fid3);

copy_file_from_working_directory_to_sdcard('drillinglong.txt');
copy_file_from_working_directory_to_sdcard('drillingshort.txt');
copy_file_from_working_directory_to_sdcard('drillingontable.txt');
%%
soundsc(drillinglong,Fs);
pause(1);
soundsc(drillingshort,Fs);
pause(1);
soundsc(drillingontable,Fs);
pause(1);