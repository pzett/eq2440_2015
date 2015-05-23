/* Copyright KTH Royal Institute of Technology, Martin Ohlsson, Per Zetterberg
 * This software is provided  �as is�. It is free to use for non-commercial purposes.
 * For commercial purposes please contact Peter H�ndel (peter.handel@ee.kth.se)
 * for a license. For non-commercial use, we appreciate citations of our work,
 * please contact, Per Zetterberg (per.zetterberg@ee.kth.se), 
 * for how information on how to cite. */ 

package se.kth.android.StudentCode;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;







import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.detector.Detector;

//import org.apache.commons.net.ntp.TimeInfo;
//import org.apache.commons.net.ntp.TimeStamp;



















//import se.kth.android.FrameWork.FrameWork;
import se.kth.android.FrameWork.StudentCodeBase;

//import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.net.wifi.ScanResult;


public class StudentCode extends StudentCodeBase {

	/* Variables needed for playing sound example */
	boolean init_done=false;
	boolean file_loaded=false;
	byte[] the_sound_file_contents=null;
	ByteBuffer the_sound_file_contents_bb=null; 
	byte[] the_sound_file_contents2=null;
	ByteBuffer the_sound_file_contents_bb2=null; 

	short buffer[]; 
	String d_filename=null;

	// This is called before any other functions are initialized so that parameters for these can be set
	public void init()
	{ 
		// Name your project so that messaging will work within your project
		projectName = "DemoProject";

		// Add sensors your project will use
		useSensors =  SOUND_IN;// CAMERA;//CAMERA_RGB;//WIFI_SCAN | SOUND_OUT; //GYROSCOPE;//SOUND_IN|SOUND_OUT;//WIFI_SCAN | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT;//TIME_SYNC|SOUND_IN;//TIME_SYNC | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT | SOUND_IN;

		// Set sample rate for sound in/out, 8000 for emulator, 8000, 11025, 22050 or 44100 for target device
		//For voice
		sampleRate = 11025;
		//For music
		//sampleRate=22050;

		// If CAMERA_RGB or CAMERA, use camera GUI?
		useCameraGUI=false;
		useAutoFocus=true;

		// Enable or disable logging of sensor data to memory card
		loggingOn = false;

		// If message communication is used between phones in the project, enable it here and set server address, type and group names
		useMessaging = true;   
		messageServer = "192.168.1.11";  
		messageServerType = PHONE_SERVER;//LINUX_MESSAGE_SERVER; // WEB_MESSAGE_SERVER

		String temp[] =  {"sender","noise","receiver"};
		messageGroups = temp;  
		//messageGroups=null;

		// If using time synchronization set the NTP time server address 
		//ntpServer = "192.168.1.5";
		//ntpServer = "192.168.5.11";

		// Set the approximate interval in milliseconds for your need for calls to your process function
		processInterval = 1;

		// If you access and modify data structures from several sensor functions and/or process you may need to make the calls
		// be performed in series instead of simultaneous to prevent exception when one function changes data at the same time as another 
		// reads it. If this is the case set useConcurrentLocks to true
		useConcurrentLocks = false;

		// If you want a text on screen before start is pressed put it here
		introText = "This is the awesome version of the Framework";

		// Stuff for the playing of sound example
		init_done=true;
		buffer=new short[1024]; // 1024 samples sent to codec at a time

		userInputString=true;
	}

	// This is called when the user presses start in the menu, reinitialize any data if needed
	public void start()
	{	
		//State Variables
		state=WAITING;	//Starting state

		//LMS Variables
		orderLMS=100;	//Order of the LMS algorithm
		thetahat=new double[orderLMS];	//Taps of the LMS algorithm
		mu=(float) 0.01;	//The mu variable of the LMS
		modifiedmu=mu;
		deltaMu=(float) 0.001; //Shifting value for mu
		firstLMS=true;	//Tells if it's the first execution of the LMS algorithm
		maxTot=0;	//Maximum of all taps for all times

		//Log-Spectral-MMSE Variables
		firstVD=true;
		numberSubdivision=8;

		//Buffering Variables
		senderBuffer=new ArrayList<short[]>();	//ArrayList of the signal buffers
		noiseBuffer=new ArrayList<short[]>();	//ArrayList of the noise buffers
		toFilterBuffer=new ArrayList<short[]>();
		i1=0;	//Counts the number of recorded buffers for the signal
		i2=0;	//Counts the number of recorded buffers for the noise
		started1=false; //Tells if sender phone is online
		started2=false;	//Tells if noise phone is online

		//Delay Variables
		diffCalculated=false;	//Tells if the difference of ArrayLists length has been calculated
		diff=0;	//Difference of length
		meanDiff=0;	//Mean difference
		counter=0;	//Count the number of time we average
		delay=0;	//Delay in number of samples

		//Synchronization Variables
		received=false;	//Tells if we have received the 10 buffers for the WAITING state
		noiseCancellation=false;	//Tells if we apply the noise cancellation or not

		//Recording Variables
		recordNoise=false;	//Tells if we record the noise estimate on .txt file or not
		out = new SimpleOutputFile();	//The .txt file we use
		out.open("outnoise.txt");	//We open the .txt file for writting

	}

	// This is called when the user presses stop in the menu, do any post processing here
	public void stop()	
	{
	}


	//Information Variables
	String triggerTime;
	String gpsData;
	String gyroData;
	String magneticData;
	String proximityData;
	String lightData;
	String screenData;
	String messageData;
	String wifi_ap = "Start value";

	//State Variables
	int state;
	public static final int WAITING=1;
	public static final int CORRELATION=2;
	public static final int EMPTY=3;
	public static final int PLAY=4;

	//LMS Variables
	double[] thetahat;
	int orderLMS;
	float mu;
	float modifiedmu;
	float deltaMu;
	boolean firstLMS;
	int timeLMS;
	double maxTot;

	//Voice Detection Variables
	boolean firstVD;
	int numberSubdivision;
	int W;
	double[] N;
	int noiseCounter;
	int noiseLength;
	double[] G;
	double[] gamma;
	double alpha;
	int noiseMargin;
	int hangover;
	boolean speechFlag;
	int timeVD;
	FastFourierTransformer fft;
	double[] segmentation;
	ArrayList<Complex[]> segments;
	ArrayList<double[]> norms;
	double[] norm;


	//Buffering Variables
	ArrayList<short[]> senderBuffer;
	ArrayList<short[]> noiseBuffer;
	ArrayList<short[]> toFilterBuffer;
	short[] toFilter;
	double[] previousBuffer;
	int i1;
	int i2;
	boolean started1;
	boolean started2;
	int bufferLength;
	int senderID;

	//Delay Variables
	boolean diffCalculated;
	int diff;
	float meanDiff;
	int counter;
	int delay;
	int timeDelay;

	//Synchronization Variables
	boolean received;
	boolean noiseCancellation;

	//Recording Variables
	boolean recordNoise;
	SimpleOutputFile out;
	SimpleOutputFile valueMu;
	long startRecord;


	// Fill in the process function that will be called according to interval above
	public void process()
	{ 
		set_output_text(messageData);	
		//Detect if the phone is the noise, the receiver or the signal
		if(myGroupID==0){
			messageData="I am the signal";
		}else if (myGroupID==1){
			messageData="I am the noise";
		}else if(this.myGroupID==2){
			switch(state){
			case WAITING:
				//WAITING for 10 buffers to be kept in memory
				messageData="Waiting for enough buffers";
				if(received){
					//When the 10 buffers are received, we calculate the average difference of 
					//the length of the ArrayLists of the noise and signal 
					//(this gives an approximation of the delay between the two)
					if(!diffCalculated){
						diff=senderBuffer.size()-noiseBuffer.size();
						diffCalculated=true;
					}else{
						if(counter<1000){
							meanDiff+=((double)(senderBuffer.size()-noiseBuffer.size()))/1000;
							counter++;
						}else{
							diff=Math.round(meanDiff);
							received=false;
							i1=0;i2=0;
							toFilter=new short[bufferLength];
							state=CORRELATION;
						}
					}
				}else{
					//Synchronize the moment when we start to record the buffers
					if (senderID==0){
						started1=true;
					}
					if (senderID==1){
						started2=true;
					}
				}
				break;
			case CORRELATION:
				//Calculates the cross-correlation between the 10 buffers in memory
				//and returns the delay by finding the maximum of the cross-correlation
				messageData="Calculating the correlation";
				short[] signalBig=new short[10*bufferLength];
				short[] noiseBig=new short[10*bufferLength];
				int k;
				int j;
				if (diff>=0){
					for (j=10;j-->0;){
						for (k=bufferLength;k-->0;){
							signalBig[k+j*bufferLength]=senderBuffer.get(j+diff)[k];
							noiseBig[k+j*bufferLength]=noiseBuffer.get(j)[k];
						}
					}
				}else{
					for (j=10;j-->0;){
						for (k=bufferLength;k-->0;){
							signalBig[k+j*bufferLength]=senderBuffer.get(j)[k];
							noiseBig[k+j*bufferLength]=noiseBuffer.get(j-diff)[k];
						}
					}
				}
				delay=calculateDelay(signalBig,noiseBig);
				state=EMPTY;
				break;
			case EMPTY:
				//Remove some useless buffers in order to minimize the delay
				messageData="Emptying buffers";
				while(senderBuffer.size()>2+Math.abs(diff) && noiseBuffer.size()>2+Math.abs(diff)){
					senderBuffer.remove(0);
					noiseBuffer.remove(0);
				}
				state=PLAY;
				break;
			case PLAY:
				//Use the recorded buffers to execute the LMS algorithm and play the result
				short[] signal=new short[bufferLength+orderLMS];
				short[] noise=new short[bufferLength+orderLMS];
				int i;
				if (senderBuffer.size()>2+Math.abs(diff) && noiseBuffer.size()>2+Math.abs(diff)){
					if(diff>=0){
						for (i=bufferLength;--i>=0;){
							//Get the signal part
							signal[i+orderLMS]=senderBuffer.get(1+diff)[i];
							if (i<orderLMS){
								signal[orderLMS-i-1]=senderBuffer.get(diff)[bufferLength-i-1];
							}
							//Get the part to play
							toFilter[i]=senderBuffer.get(1+diff)[i];
							//Get the noise part, taking in consideration the delay
							if (delay>0){
								if (i<orderLMS+delay){
									noise[orderLMS+delay-i-1]=noiseBuffer.get(0)[bufferLength-i-1];
								}
								if (i+delay<bufferLength){
									noise[i+orderLMS+delay]=noiseBuffer.get(1)[i];
								}
							}else{
								if (orderLMS+delay>0){
									if (i<orderLMS+delay){
										noise[orderLMS+delay-i-1]=noiseBuffer.get(0)[bufferLength-i-1];
									}
									noise[orderLMS+delay+i]=noiseBuffer.get(1)[i];
									if (i<-delay){
										noise[orderLMS+delay+bufferLength+i]=noiseBuffer.get(2)[i];
									}
								}else{
									if (i>-orderLMS-delay){
										noise[i+orderLMS+delay]=noiseBuffer.get(1)[i];
									}
									if (i<-delay){
										noise[bufferLength+i+orderLMS+delay]=noiseBuffer.get(2)[i];
									}
								}
							}
						}
					}else{
						for (i=bufferLength;--i>=0;){
							//Get the signal part
							signal[i+orderLMS]=senderBuffer.get(1)[i];
							if (i<orderLMS){
								signal[orderLMS-i-1]=senderBuffer.get(0)[bufferLength-i-1];
							}
							//Get the part to play
							toFilter[i]=senderBuffer.get(1)[i];
							//Get the noise part, taking in consideration the delay
							if (delay>0){
								if (i<orderLMS+delay){
									noise[orderLMS+delay-i-1]=noiseBuffer.get(-diff)[bufferLength-i-1];
								}
								if (i+delay<bufferLength){
									noise[i+orderLMS+delay]=noiseBuffer.get(1-diff)[i];
								}
							}else{
								if (orderLMS+delay>0){
									if (i<orderLMS+delay){
										noise[orderLMS+delay-i-1]=noiseBuffer.get(-diff)[bufferLength-i-1];
									}
									noise[orderLMS+delay+i]=noiseBuffer.get(1-diff)[i];
									if (i<-delay){
										noise[orderLMS+delay+bufferLength+i]=noiseBuffer.get(2-diff)[i];
									}
								}else{
									if (i>-orderLMS-delay){
										noise[i+orderLMS+delay]=noiseBuffer.get(1-diff)[i];
									}
									if (i<-delay){
										noise[bufferLength+i+orderLMS+delay]=noiseBuffer.get(2-diff)[i];
									}
								}
							}
						}
					}
				}

				double max=0;
				if(noiseCancellation){
					//Apply the LMS noise cancellation
					short[] noiseEstimate;
					noiseEstimate=nlmsShort(signal, noise, orderLMS, mu);
					for (i=bufferLength;--i>=0;){
						toFilter[i]-=noiseEstimate[i];
					}
					toFilterBuffer.add(toFilter);
					//Apply voice detection
					voiceDetection();
					//Helps recording the noise estimate to debug by using Matlab
					if (recordNoise){
						int p;
						for(p=bufferLength;p-->0;){
							out.writeDouble(noiseEstimate[p]);
						};
						if(startRecord-System.currentTimeMillis()>1000){
							out.close();
							recordNoise=false;
						}
					}

				}else{
					toFilterBuffer.add(toFilter);
				}


				//Plays the denoised signal and remove the used buffers
				if (senderBuffer.size()>2+Math.abs(diff) && noiseBuffer.size()>2+Math.abs(diff)){
					if (toFilterBuffer.size()>=2){
						sound_out(toFilter,bufferLength);
						toFilterBuffer.remove(0);
						noiseBuffer.remove(0);
						senderBuffer.remove(0);
					}

				}
				//Message to plot
				messageData="mu="+mu+"  //  "+"counter="+counter+"  //  "+"delay="+delay+"\n"+"mu'="+modifiedmu+"diff="+diff+"  //  "
						+"meanDiff="+meanDiff+"\n"+"noiseCancellation="+noiseCancellation+"\n"+
						"timeVD="+timeVD+"  //  "+"speechFlag="+speechFlag+"\n"+
						"max="+max+"  //  "+"maxTot="+maxTot+"\n"+
						"lengthSenderBuffer="+senderBuffer.size()+"\n"+"lengthNoiseBuffer="
						+noiseBuffer.size()+"\n"+"timeLMS="+timeLMS+"  //  "+"timeDelay="+timeDelay;

				break;
			}
		}
	};     

	//Calculate the estimate of x given y
	private short[] nlmsShort(short[] x,short[] y,int N,double muu){
		final long start=System.currentTimeMillis();
		final int M=y.length;
		final short[] xhat=new short[M-N];
		int i;
		int n;
		int m;
		int o;
		double normY;
		double xn;
		double k;
		double xhatt;
		if (firstLMS){
			for (i=N;--i>=0;){
				thetahat[i]=0;
			}
			firstLMS=false;
		}
		for (n=M-N;--n>=0;){
			m=M-N-1-n;
			o=m+N-1;
			xn=x[o+1];
			xhatt=0;
			normY=0;
			for (i=N;--i>=0;){
				xhatt+=y[o-i]*thetahat[i];
				normY+=y[o-i]*y[o-i];
			}
			xhat[o+1-N]=(short)xhatt;
			modifiedmu=(float) (muu*N/(normY+1));
			k=(xn-xhatt)*muu*N/(normY+1);
			if (m%1==0){
				for (i=N;--i>=0;){
					thetahat[i]+=k*y[o-i];
				}
			}
		}
		//Variable that gives the execution speed of the algorithm
		timeLMS=(int) (System.currentTimeMillis()-start);
		return xhat;
	}

	//Calculate the delay between two sequences noise and signal
	//Based on the maximum of the cross-correlation
	public int calculateDelay(short[] signal,short[] noise){
		long start=System.currentTimeMillis();
		double[] correlation = new double[2*10*bufferLength];
		int shift=10*bufferLength;
		double val;
		int maxIndex = 0;
		double maxVal = 0;
		int i;
		int k;
		for(i=correlation.length;i-->0;){
			val = 0;
			for(k=10*bufferLength;k-->0;){
				if(k+shift>10*bufferLength-1){
					break;
				}
				if(k+shift<0){
					continue;
				}
				val+=signal[k]*noise[k+shift];
			}
			correlation[i] = val;
			shift--;
			if(correlation[i]>maxVal){
				maxVal=correlation[i];
				maxIndex=i;
			}
		}
		//Variable that gives the execution speed of the algorithm
		timeDelay=(int)(System.currentTimeMillis()-start);
		return maxIndex-10*bufferLength;
	}

	
	/*
	//Implementation of the voice detection algorithm
	private void voiceDetection(){
		long startTime=System.currentTimeMillis();
		//Initialization for first time logMMSE is used
		if(firstVD){
			W=bufferLength/numberSubdivision;
			noiseCounter=0;
			noiseLength=9;
			noiseMargin=3;
			hangover=8;
			speechFlag=false;
			fft=new FastFourierTransformer(DftNormalization.STANDARD);
			segmentation=new double[W];
			segments=new ArrayList<Complex[]>();
			norms=new ArrayList<double[]>();
			norm=new double[(W/2)+1];
		}
		//Local variables
		segments.clear();
		norms.clear();
		//Segmentation and FFT
		int i,j;
		for(i=numberSubdivision;i-->0;){
			for(j=W;j-->0;){
				segmentation[j]=toFilterBuffer.get(0)[j+i*W];
			}
			segments.add(0,fft.transform(segmentation, TransformType.FORWARD));
		}
		//Calculate arguments and norms of the FFTs
		for(i=numberSubdivision;i-->0;){
			for(j=W/2+1;j-->0;){
				norm[j]=segments.get(i)[j].abs();
			}
			norms.add(0, norm);
		}
		//Initialize the noise estimate
		if(firstVD){
			N=new double[(W/2)+1];
			for(i=(W/2)+1;i-->0;){
				for(j=numberSubdivision;j-->0;){
					N[i]+=norms.get(j)[i]/numberSubdivision;
				}
			}
			firstVD=false;
		}
		//Loop over the segments
		for(i=0;i<numberSubdivision;i++){
			//Voice detection
			vad(norms.get(i),N);
			//If not voice, we update the noise estimate and cut the signal
			if(!speechFlag){
				for(j=N.length;j-->0;){
					N[j]=(noiseLength*N[j]+norms.get(i)[j])/(noiseLength+1);
				}
				for(j=W;j-->0;){
					toFilter[j+i*W]=0;
				}
			}
		}
		timeVD=(int)(System.currentTimeMillis()-startTime);
	}
	*/
	
	
	private void voiceDetection(){
		long startTime=System.currentTimeMillis();
		//Initialization for first time logMMSE is used
		int i,j;
		if(firstVD){
			W=bufferLength/numberSubdivision;
			noiseCounter=0;
			noiseLength=9;
			noiseMargin=5;
			hangover=8;
			speechFlag=false;
			fft=new FastFourierTransformer(DftNormalization.STANDARD);
			segmentation=new double[W];
			norm=new double[(W/2)+1];
			N=new double[(W/2)+1];
			for(i=0;i<numberSubdivision;i++){
				for(j=W;j-->0;){
					segmentation[j]=toFilter[j+i*W];
				}
				Complex[] segmentFFT=fft.transform(segmentation, TransformType.FORWARD);
				for(j=(W/2)+1;j-->0;){
					norm[j]=segmentFFT[j].abs();
					N[j]+=norm[j]/numberSubdivision;
				}		
			}
			firstVD=false;
		}else{
			for(i=0;i<numberSubdivision;i++){
				for(j=W;j-->0;){
					segmentation[j]=toFilter[j+i*W];
				}
				Complex[] segmentFFT=fft.transform(segmentation, TransformType.FORWARD);
				for(j=(W/2)+1;j-->0;){
					norm[j]=segmentFFT[j].abs();
				}
				vad(norm,N);
				if(!speechFlag){
					for(j=(W/2)+1;j-->0;){
						N[j]=(noiseLength*N[j]+norm[j])/(noiseLength+1);
					}
					for(j=W;j-->0;){
						toFilter[j+i*W]=0;
					}
				}
			}
		}
		timeVD=(int)(System.currentTimeMillis()-startTime);
	}


	//Voice detection algorithm
	public void vad(double[] norm,double[] noise){
		double dist=0.0;
		double val=0.0;
		int i;
		for(i=norm.length;i-->0;){
			val=20*(Math.log10(norm[i])-Math.log10(noise[i]));
			if(val>=0){
				dist+=val;
			}
		}
		dist/=norm.length;
		if(dist<noiseMargin){
			noiseCounter++;
		}else{
			noiseCounter=0;
		}
		if(noiseCounter>hangover){
			speechFlag=false;
		}else{
			speechFlag=true;
		}
	}

	// Fill in the functions receiving sensor data to do processing 
	public void gps(long time, double latitude, double longitude, double height, double precision)
	{
		gpsData = "G: "+format4_2.format(latitude)+":"+format4_2.format(longitude)+":"+format4_2.format(height)+":"+format4_2.format(precision);
	}

	public void magnetic_field(long time, double x, double y, double z)
	{
		magneticData = "M: "+format4_2.format(x)+":"+format4_2.format(y)+":"+format4_2.format(z);
	}

	public void accelerometer(long time, double x, double y, double z)
	{
		triggerTime = "A: "+format4_2.format(x)+":"+format4_2.format(y)+":"+format4_2.format(z);
	}

	public void gyroscope(long time, double x, double y, double z)
	{
		gyroData = "G: "+format4_2.format(x)+":"+format4_2.format(y)+":"+format4_2.format(z);
	}
	public void proximity(long time, double p)
	{
		proximityData = "P: "+format4_2.format(p); 
	}

	public void light(long time, double l)
	{
		lightData = "L: "+format4_2.format(l);
	}

	public void sound_in(long time, short[] samples, int length)
	{			
		if (myGroupID==0 || myGroupID==1){
			p_streaming_buffer_out(samples, length, messageGroups[2]);
		}
	}



	public void screen_touched(float x, float y) 
	{
		if (myGroupID==2){
			//Change the value of noiseCancellation
			if(y>150 && y<300){
				noiseCancellation=!noiseCancellation;
				int i=0;
				for(i=thetahat.length;i-->0;){
					thetahat[i]=0;
				}
				firstVD=true;
			}
			//Change the value of mu
			if(y>370 && y<520){
				if(x<360){
					mu+=deltaMu;
				}else{
					mu-=deltaMu;
				}
			}
			//Launch a recording of the estimate of the noise
			if(y>520){
				recordNoise=true;
				startRecord=System.currentTimeMillis();
			}
		}
	} 

	// Implement your phone to phone receive messaging here
	public void message_in(StudentMessage message)
	{
	}

	public void plot_data(Canvas plotCanvas, int width, int height) 
	{		
		//Plots the interface
		if (myGroupID==2){
			Paint writting=new Paint();
			writting.setColor(Color.WHITE);
			writting.setTextSize(40);
			plotCanvas.drawRect(10, 50, 710, 200, blue);
			plotCanvas.drawRect(10, 270, 360, 420, green);
			plotCanvas.drawRect(360, 270, 710, 420, red);
			plotCanvas.drawText("noiseCancellation=!noiseCancellation", 20, 145, writting);
			plotCanvas.drawText("Change the value of mu:", 40, 250, writting);
			plotCanvas.drawText("+"+deltaMu,100 , 365 , writting);
			plotCanvas.drawText("-"+deltaMu,460 , 365 , writting);
		}			
	}


	//Enables us to fix the value of mu
	public void stringFromUser(String user_input){		
		mu=Float.parseFloat(user_input);
	}


	public void stringFromBrowseForFile(String filename) {
		d_filename=filename;
		set_output_text(d_filename);
	}

	// Implement wifi ap analysis here
	public void wifi_ap(long time, List<ScanResult> wifi_list)
	{
		wifi_ap = "";
		for(ScanResult sr: wifi_list)
			wifi_ap += sr.SSID + " " + sr.level + "\n"; 

	}

	// Implement reception of streaming sound here
	public void streaming_buffer_in(short[] buffer, int length, int senderId)
	{
		senderID=senderId;
		if (myGroupID==2){
			switch(state){
			case WAITING:
				if (senderId==0 && started2){
					senderBuffer.add(buffer);
					i1++;
				}
				if (senderId==1 && started1){
					noiseBuffer.add(buffer);
					i2++;
				}
				received=(i1>10 && i2>10);
				bufferLength=length;
				break;
			case CORRELATION:

				break;
			case EMPTY:

				break;
			case PLAY:
				if (senderId==0){
					senderBuffer.add(buffer);
				}
				if (senderId==1){
					noiseBuffer.add(buffer);
				}
				break;
			}
		}
	}

	byte[] latestImage = null;
	byte[] latestRGBImage = null;
	int imageHeight = 0;
	int imageWidth = 0;
	QRCodeReader r = new QRCodeReader();

	public void camera_image(byte[] image, int width, int height) // For gray scale G8 in each byte
	{
		/* Save latest image*/
		latestImage = image;
		imageWidth = width;
		imageHeight = height;

	}

	public void camera_image_rgb(byte[] image, int width, int height) // For color RGB888 interleaved
	{
		latestRGBImage = image;
		imageWidth = width;
		imageHeight = height;

		/* Below is example code which uses the com.google.zxing.qrcode QR decoder to
		   detect a web address. The address is displayed in the text display. */
		/* zxing start color */ 

		Result res = null;
		int[] frame = new int[width/4*height/4];
		for(int y=0;y<height/4;y++)
			for(int x=0;x<width/4;x++)
			{
				int i = (y*4*width+x*4)*3;

				int rgbColor = 0xFF000000 | (image[i]<<16)&0xFF0000 | (image[i+1]<<8)&0xFF00 | image[i+2]&0xFF;
				frame[y*width/4+x] = rgbColor;
			}



		RGBLuminanceSource s = new RGBLuminanceSource(width/4,height/4,frame);
		BinaryBitmap b = new BinaryBitmap(new GlobalHistogramBinarizer(s));
		Detector d;
		DetectorResult dr = null;
		try {
			d = new Detector(b.getBlackMatrix());
			dr = d.detect();

			res = r.decode(b);
		} catch (NotFoundException e) {
		} catch (FormatException e) {
		}
		catch (ChecksumException e) {
		}
		if(dr != null)
			for(ResultPoint rp : dr.getPoints())
			{
				image[(int) ((rp.getX()*4+rp.getY()*4*width)*3)] = 0;
				image[(int) ((rp.getX()*4+rp.getY()*4*width)*3)+1] = 0;
				image[(int) ((rp.getX()*4+rp.getY()*4*width)*3)+1] = (byte) 0xFF;
			}

		if(res != null)
		{
			set_output_text(res.getText());
		} 

		/* zxing stop color */ 
	}


	//This function is called before the framework executes normally  meaning that no sensors or
	// initialing is done.
	// If you return true the execution stops after this function.
	// Use this to test algorithms with static data.
	public boolean test_harness()
	{
		boolean do_test=false; // Set to true when running test_harness_example

		// The below code is used together with test_harness_example.m.	
		if (do_test) {	
			int no_of_real;
			double [] in_values;
			double [] out_values;



			SimpleOutputFile out = new SimpleOutputFile();
			SimpleInputFile in = new SimpleInputFile();

			in.open("indata.txt");    	    	
			out.open("outdata.txt");

			// Read data from input file 
			no_of_real=in.readInt();
			in_values=new double[no_of_real];
			// Read file from sdcard
			for(int i=0; i<in_values.length; i++){
				in_values[i]=in.readDouble(); 
			};


			// Call the function to be tested 
			out_values=square(in_values);

			// Write file on sdcard 
			for(int i=0; i<in_values.length; i++){
				out.writeDouble(out_values[i]);
			};


			out.close();  
			in.close();

			return true;
		} else
			return false;
	}

	/* Used in test_harness_example.m */
	private double [] square(double [] in_values) {
		double [] out_values;
		out_values = new double[in_values.length];
		for(int i=0; i<in_values.length; i++){
			out_values[i]=in_values[i]*in_values[i];
		} 
		return out_values;
	}


	public void playsoundexample(){
		if (init_done && (!file_loaded) && (!(d_filename==null))) {
			set_output_text(d_filename);
			the_sound_file_contents=read_data_from_file(d_filename); // Read file from plain file of samples in form of shorts
			the_sound_file_contents_bb=ByteBuffer.wrap(the_sound_file_contents); // Wrapper to easier access content.
			the_sound_file_contents_bb.order(ByteOrder.LITTLE_ENDIAN);

			file_loaded=true;
		};

		if (file_loaded) {
			if (the_sound_file_contents_bb.remaining()<2*buffer.length)
				the_sound_file_contents_bb.rewind(); // Start buffer from beginning
			for (int i1=0;i1<buffer.length;i1++) {				
				buffer[i1]=the_sound_file_contents_bb.getShort(); // Create a buffer of shorts
			};
			p_streaming_buffer_out(buffer,buffer.length,"N3");
			sound_out(buffer,buffer.length); // Send buffer to player			
		}; 		  
	};

}


