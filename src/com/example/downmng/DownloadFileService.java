package com.example.downmng;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import com.saponia.mobapp.R;
import com.saponia.mobapp.network.ParseJSON;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileService extends Service {

	final static String ACTION = "DownloadServiceAction";
	final static String STOP_SERVICE = "";
	final static int RQS_STOP_SERVICE = 1;
	
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	String fileName = "excel_zaliha.xlsx";
	String fileType = "xlsx";
	String pathFile = "/sdcard/"+fileName;
	public Activity applicationActivity;
	public Context appContext;
	String FileUrl;
	
	public int noteId = 10;	
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;	
	
	
	public DownloadFileService (Activity applicationActivity,String FileUrl,String fileType,String fileName,String pathFile){
		super();
		this.FileUrl = FileUrl;
		this.applicationActivity = applicationActivity;
		this.fileName = fileName;
		this.fileType = fileType;
		this.pathFile = pathFile;
	}
	
	@Override
	public void onCreate() {
		
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
	    HandlerThread thread = new HandlerThread("ServiceStartArguments");
	    thread.start();

	    // Get the HandlerThread's Looper and use it for our Handler
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);		
		
		
		Log.i("TAG", "Service onCreate");
		super.onCreate();		
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;		
		
		//return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	  		Log.i("TAG", "Service onStartCommand");
			Context context = getApplicationContext();
			
			int count;
			String path=""; 
			try {
				URL url = new URL(FileUrl);
				URLConnection conexion = url.openConnection();
				conexion.connect();
				
				mNotifyManager = (NotificationManager) applicationActivity.getSystemService(Context.NOTIFICATION_SERVICE);
				mBuilder = new NotificationCompat.Builder(applicationActivity);
				mBuilder.setContentTitle("Download")
						.setContentText("Download in progress")
						.setSmallIcon(R.drawable.ic_launcher);			
				
				mBuilder.setProgress(100, 0, false);
				mNotifyManager.notify(noteId, mBuilder.build());			
				
				
				int lenghtOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
			
				InputStream input = new BufferedInputStream(url.openStream());
				//path = Conns.APP_MAIN_SD_DOC_DIR + "/"+fileName;
				//path = "/sdcard/"+fileName;
				path = pathFile;
				OutputStream output = new FileOutputStream(path);//"/sdcard/"+fileName); //Environment.getExternalStorageDirectory().getPath()
			
				byte data[] = new byte[1024];
			
				long total = 0;
			
				while ((count = input.read(data)) != -1) {
					total += count;
					//publishProgress(""+(int)((total*100)/lenghtOfFile));
					
					mBuilder.setProgress(100, (int)((total*100)/lenghtOfFile), false);
					mNotifyManager.notify(noteId, mBuilder.build());					
					
					output.write(data, 0, count);
				}
		
				output.flush();
				output.close();
				input.close();
				
				mBuilder.setProgress(0, 0, false);
				mNotifyManager.notify(noteId, mBuilder.build());			
				
			} catch (Exception e) {
				path = "" ;
		    	Calendar c = Calendar.getInstance(); 
		    	String datetime = DateFormat.format(Conns.TIMESTAMP_FORMAT_PATTERN, c).toString();			
				ParseJSON.dodajSynclog(applicationActivity.getApplicationContext(),"","downloadFile","sapadmin", datetime,datetime, 0, 0, 1, "Greška prilikom dohvaæanja datoteke:"+e.getMessage(), null);
			}			
	          // Stop the service using the startId, so that we don't stop
	          // the service in the middle of handling another job
	          stopSelf(msg.arg1);
	      }
	  }	
	

}
