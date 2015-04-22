/* Copyright KTH Royal Institute of Technology, Martin Ohlsson, Per Zetterberg
 * This software is provided  ’as is’. It is free to use for non-commercial purposes.
 * For commercial purposes please contact Peter Händel (peter.handel@ee.kth.se)
 * for a license. For non-commercial use, we appreciate citations of our work,
 * please contact, Per Zetterberg (per.zetterberg@ee.kth.se), 
 * for how information on how to cite. */ 

package se.kth.android.StudentCode;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
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
		useSensors =  ACCELEROMETER;// CAMERA;//CAMERA_RGB;//WIFI_SCAN | SOUND_OUT; //GYROSCOPE;//SOUND_IN|SOUND_OUT;//WIFI_SCAN | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT;//TIME_SYNC|SOUND_IN;//TIME_SYNC | ACCELEROMETER | MAGNETIC_FIELD | PROXIMITY | LIGHT | SOUND_IN;						

		// Set sample rate for sound in/out, 8000 for emulator, 8000, 11025, 22050 or 44100 for target device
		sampleRate = 44100;

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
		processInterval = 100;

		// If you access and modify data structures from several sensor functions and/or process you may need to make the calls
		// be performed in series instead of simultaneous to prevent exception when one function changes data at the same time as another 
		// reads it. If this is the case set useConcurrentLocks to true
		useConcurrentLocks = false;


		// If you want a text on screen before start is pressed put it here
		introText = "Make sure that the phone is held stationary and chose a floor before starting";

		// Stuff for the playing of sound example
		init_done=true;
		buffer=new short[1024]; // 1024 samples sent to codec at a time

		userInputString=true;

		hasStarted=false;
	}

	// This is called when the user presses start in the menu, reinitialize any data if needed
	public void start()
	{
		z= new ArrayList<Double>();

		lengthZ=2100;
		for (int i=0;i<lengthZ;i++){
			double zero=0;
			z.add(zero);
		}
		hasStarted=true;
		timeChangedFloor=System.currentTimeMillis();
		meanLimit=0.27;
		userInput=2;
	}

	// This is called when the user presses stop in the menu, do any post processing here
	public void stop()	
	{
	}

	// Place your local field variables here
	String triggerTime;
	String gpsData;
	String gyroData;
	String accelerometerData;
	String magneticData;
	String proximityData;
	String lightData;
	String screenData;
	String messageData;
	String wifi_ap = "Start value";



	int lengthZ;
	ArrayList<Double> z;

	double mean1;
	double mean2;
	double mean3;
	double mean4;
	double mean5;
	double mean6;
	double mean7;
	boolean hasStarted;
	boolean moving;
	boolean oneUp;
	boolean oneDown;
	boolean twoUp;
	boolean twoDown;
	boolean threeUp;
	boolean threeDown;
	boolean fourUp;
	boolean fourDown;
	boolean fiveUp;
	boolean fiveDown;
	boolean sixUp;
	boolean sixDown;
	int userInput;
	boolean changedFloor;
	long timeChangedFloor;
	double meanLimit;
	/*
	double[] result1=new double[2];
	double[] result2=new double[2];
	 */
	int counter=0;

	int icounter = 0;
	// Fill in the process function that will be called according to interval above
	public void process()
	{ 
		if (hasStarted){
			long currentTime=System.currentTimeMillis();
			boolean canChange=(currentTime-timeChangedFloor>3000);
			moving=true;
			oneDown=(mean1>meanLimit-0.05 && mean2<-meanLimit+0.05);
			oneUp=(mean1<-meanLimit+0.05 && mean2>meanLimit-0.05);
			twoDown=(mean1>meanLimit && mean3<-meanLimit);
			twoUp=(mean1<-meanLimit && mean3>meanLimit);
			threeDown=(mean1>meanLimit && mean4<-meanLimit);
			threeUp=(mean1<-meanLimit && mean4>meanLimit);
			fourDown=(mean1>meanLimit && mean5<-meanLimit);
			fourUp=(mean1<-meanLimit && mean5>meanLimit);
			fiveDown=(mean1>meanLimit && mean6<-meanLimit);
			fiveUp=(mean1<-meanLimit && mean6>meanLimit);
			sixDown=(mean1>meanLimit && mean7<-meanLimit);
			sixUp=(mean1<-meanLimit && mean7>meanLimit);
			if (canChange && mean1<0.33 && mean1>-0.33){
				if (oneDown || oneUp || twoDown || twoUp || threeDown || threeUp || fourDown || fourUp || fiveDown || fiveUp || sixDown || sixUp){
					changedFloor=true;
					timeChangedFloor=System.currentTimeMillis();
				}
			}
			if (changedFloor){
				if (oneDown){
					userInput--;
				}else if (oneUp){
					userInput++;
				}else if (twoDown){
					userInput-=2;
				}else if (twoUp){
					userInput+=2;
				}else if (threeDown){
					userInput-=3;
				}else if (threeUp){
					userInput+=3;
				}else if (fourDown){
					userInput-=4;
				}else if (fourUp){
					userInput+=4;
				}else if (fiveDown){
					userInput-=5;
				}else if (fiveUp){
					userInput+=5;
				}else if (sixDown){
					userInput-=6;
				}else if (sixUp){
					userInput+=6;
				}
				mean1=0;
				mean2=0;
				mean3=0;
				mean4=0;
				mean5=0;
				mean6=0;
				mean7=0;
				for (int i=0;i<z.size();i++){
					z.set(i,0.0);
				}
				changedFloor=false;
			}
			accelerometerData="You're on the floor "+userInput+"\n"+"z="+z.get(0)+"\n"+"mean1="+mean1;
		}
		set_output_text(accelerometerData);
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


	int i=0;
	public void accelerometer(long time, double x, double y, double z)
	{
		if (this.z.size()>lengthZ){
			this.z.remove(lengthZ);
		}
		this.z.add(0, z-9.1);
		double z0=this.z.get(0);
		double z300=this.z.get(300);
		double z600=this.z.get(600);
		double z900=this.z.get(900);
		double z1200=this.z.get(1200);
		double z1500=this.z.get(1500);
		double z1800=this.z.get(1800);
		double z2100=this.z.get(2100);
		mean1+=z0/300;
		mean1-=z300/300;
		mean2+=z300/300;
		mean2-=z600/300;
		mean3+=z600/300;
		mean3-=z900/300;
		mean4+=z900/300;
		mean4-=z1200/300;
		mean5+=z1200/300;
		mean5-=z1500/300;
		mean6+=z1500/300;
		mean6-=z1800/300;
		mean7+=z1800/300;
		mean7-=z2100/300;
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
		userInput=Integer.parseInt(user_input);
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

	private double calculateAverage(List<Double> list,int sizeList){
		double mean=0;
		for (int i=0;i<sizeList;i++){
			mean+=list.get(i);
		}
		mean=mean/list.size();
		return mean;
	}

	private double[] calculateVariance(List<Double> list,int sizeList){
		double[] result=new double[2];
		result[0]=this.calculateAverage(list,sizeList);
		result[1]=0;
		double value;
		for (int i=0;i<sizeList;i++){
			value=list.get(i);
			result[1]+=(value-result[0])*(value-result[0]);
		}
		result[1]=result[1]/list.size();
		return result;
	}

	/*
	private double calculateVariance(List<Double> list,int sizeList){
		double calculatedMean=this.calculateAverage(list,sizeList);
		double var=0;
		double value;
		for (int i=0;i<sizeList;i++){
			value=list.get(i);
			var+=(value-calculatedMean)*(value-calculatedMean);
		}
		var=var/list.size();
		return var;
	}*/


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



