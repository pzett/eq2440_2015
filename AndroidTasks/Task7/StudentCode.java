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
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.net.wifi.ScanResult;


public class StudentCode extends StudentCodeBase {

	/* Varibles need for plaing sound example */
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
		useSensors =  SOUND_OUT;// CAMERA;//CAMERA_RGB;//WIFI_SCAN | SOUND_OUT; //GYROSCOPE;//SOUND_IN|SOUND_OUT;//WIFI_SCAN | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT;//TIME_SYNC|SOUND_IN;//TIME_SYNC | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT | SOUND_IN;


		// Set sample rate for sound in/out, 8000 for emulator, 8000, 11025, 22050 or 44100 for target device
		sampleRate = 22050;

		// If CAMERA_RGB or CAMERA, use camera GUI?
		useCameraGUI=false;
		useAutoFocus=true;

		// Enable or disable logging of sensor data to memory card
		loggingOn = false;

		// If message communication is used between phones in the project, enable it here and set server address, type and group names
		useMessaging = false;   
		messageServer = "192.168.1.101";  
		messageServerType = PHONE_SERVER;//LINUX_MESSAGE_SERVER; // WEB_MESSAGE_SERVER

		String temp[] =  {"N1","N2","N3"};
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
		introText = "This is the empty version of FrameWork";

		// Stuff for the playing of sound example
		init_done=true;
		buffer=new short[1024]; // 1024 samples sent to codec at a time

		userInputString=true;
	}

	// This is called when the user presses start in the menu, reinitialize any data if needed
	public void start()
	{	
	}

	// This is called when the user presses stop in the menu, do any post processing here
	public void stop()	
	{
	}

	// Place your local field variables here
	String triggerTime;
	String gpsData;
	String gyroData;
	String magneticData;
	String proximityData;
	String lightData;
	String screenData;
	String messageData;
	String wifi_ap = "Start value";
	int counter=0;

	// Fill in the process function that will be called according to interval above
	public void process()
	{ 
		//set_output_text(""+gyroData+"\n"+gpsData + "\n"+triggerTime+"\n"+ magneticData+"\n"+proximityData+"\n"+lightData+"\n"+screenData+"\n"+messageData);		//set_output_text(debug_output+"\n");
		//set_output_text(wifi_ap);		

		// Sound example. Uncomment to play sound from the file data/lga.dat formatted as described in the slides.		
		//playsoundexample();



	};       


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
		set_output_text("length="+length);
	}


	public void screen_touched(float x, float y) 
	{
	} 

	// Implement your phone to phone receive messaging here
	public void message_in(StudentMessage message)
	{
	}







	// Implement any plotting you need here 
	public void plot_data(Canvas plotCanvas, int width, int height) 
	{		

		if((latestImage != null) && ((useSensors & CAMERA) == CAMERA)) // If camera is enabled, display
		{
			plot_camera_image(plotCanvas,latestImage,imageWidth,imageHeight,width,height);
		}				
		if((latestRGBImage != null) && ((useSensors & CAMERA_RGB) == CAMERA_RGB)) // If camera is enabled, display
		{
			plot_camera_image_rgb(plotCanvas,latestRGBImage,imageWidth,imageHeight,width,height);
		}				
	}


	public void stringFromUser(String user_input){		
		set_output_text(user_input);
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

	/*
	public boolean test_harness()
	{
		boolean do_test=true; // Set to true when running test_harness_example

		// The below code is used together with test_harness_example.m.	
		if (do_test) {	
			int no_of_real;
			double [] in_values;
			double [] in_values2;
			double [] out_values;



			SimpleOutputFile out = new SimpleOutputFile();
			SimpleInputFile in = new SimpleInputFile();
			SimpleInputFile in2 = new SimpleInputFile();

			in.open("indata.txt");    	  
			in2.open("indata2.txt");
			out.open("outdata.txt");

			// Read data from input file 
			no_of_real=in.readInt();
			no_of_real=in2.readInt();
			in_values=new double[no_of_real];
			in_values2=new double[no_of_real];
			// Read file from sdcard
			for(int i=0; i<in_values.length; i++){
				in_values[i]=in.readDouble(); 
			};
			for(int i=0; i<in_values2.length; i++){
				in_values2[i]=in2.readDouble(); 
			};


			// Call the function to be tested 
			out_values=lmsXaviDouble(in_values,in_values2,100,0.000000000001);



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
	 */

	public boolean test_harness()
	{
		boolean do_test=true; // Set to true when running test_harness_example

		// The below code is used together with test_harness_example.m.	
		if (do_test) {	
			int no_of_real;
			short [] in_values;
			short [] in_values2;
			short [] out_values;



			SimpleOutputFile out = new SimpleOutputFile();
			SimpleInputFile in = new SimpleInputFile();
			SimpleInputFile in2 = new SimpleInputFile();

			in.open("indata.txt");    	  
			in2.open("indata2.txt");
			out.open("outdata.txt");

			// Read data from input file 
			no_of_real=in.readInt();
			no_of_real=in2.readInt();
			in_values=new short[no_of_real];
			in_values2=new short[no_of_real];
			// Read file from sdcard
			for(int i=0; i<in_values.length; i++){
				in_values[i]=(short) in.readDouble(); 
			};
			for(int i=0; i<in_values2.length; i++){
				in_values2[i]=(short) in2.readDouble(); 
			};


			// Call the function to be tested 
			out_values=lmsXaviShort(in_values,in_values2,100,0.00001);



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


	private double[] lmsXavi(double[] x,double[] y,int N,double muu){
		long start=System.currentTimeMillis();
		int M=y.length;
		double[] xhat=new double[M];
		double[][] thetahat=new double[N][M-N];
		double[] ytemp=new double[N];

		for (int n=0;n<M-N-1;n++){
			for (int i=0;i<N;i++){
				ytemp[i]=y[n+N-i-1];
			}
			if (n==0){
				xhat[n+N]=0;
				for (int i=0;i<N;i++){
					thetahat[i][n]=muu*ytemp[i]*(x[n+N]-xhat[n+N]);
				}
			}else{
				for (int i=0;i<N;i++){
					xhat[n+N]+=ytemp[i]*thetahat[i][n-1];
				}
				for (int i=0;i<N;i++){
					thetahat[i][n]=thetahat[i][n-1]+muu*ytemp[i]*(x[n+N]-xhat[n+N]);
				}
			}
		}
		int time=(int) (System.currentTimeMillis()-start);
		System.out.println("timeLMS="+time);
		return xhat;
	}

	private double[] lmsXaviDouble(double[] x,double[] y,int N,double muu){
		final long start=System.currentTimeMillis();
		final int M=y.length;
		final double[] xhat=new double[M];
		final double[] thetahat=new double[N];
		int i;
		int n;
		int m;
		int o;
		final double xn0=x[N];
		double xn;
		double k;
		final double k0=xn0*muu;
		double xhatt;
		for (i=N;--i>=0;){
			thetahat[i]=k0*y[N-i-1];
		}
		for (n=M-N-1;--n>0;){
			m=M-N-1-n;
			o=m+N-1;
			xn=x[o+1];
			xhatt=0;
			for (i=N;--i>=0;){
				xhatt+=y[o-i]*thetahat[i];
			}
			xhat[o+1]=xhatt;
			k=(xn-xhatt)*muu;
			for (i=N;--i>=0;){
				thetahat[i]+=k*y[o-i];
			}
		}
		int time=(int) (System.currentTimeMillis()-start);
		System.out.println("timeLMS="+time);
		return xhat;
	}

	private short[] lmsXaviShort(short[] x,short[] y,int N,double muu){
		final long start=System.currentTimeMillis();
		final int M=y.length;
		final short[] xhat=new short[M];
		final double[] thetahat=new double[N];
		int i;
		int n;
		int m;
		int o;
		final double xn0=x[N];
		double xn;
		double k;
		final double k0=xn0*muu;
		double xhatt;
		for (i=N;--i>=0;){
			thetahat[i]=k0*y[N-i-1];
		}
		for (n=M-N-1;--n>0;){
			m=M-N-1-n;
			o=m+N-1;
			xn=x[o+1];
			xhatt=0;
			for (i=N;--i>=0;){
				xhatt+=y[o-i]*thetahat[i];
			}
			xhat[o+1]=(short)xhatt;
			k=(xn-xhatt)*muu;
			if (n%3==0){
				for (i=N;--i>=0;){
					thetahat[i]+=k*y[o-i];
				}
			}
		}
		int time=(int) (System.currentTimeMillis()-start);
		System.out.println("timeLMS="+time);
		System.out.println(""+(M-N-1));
		return xhat;
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



