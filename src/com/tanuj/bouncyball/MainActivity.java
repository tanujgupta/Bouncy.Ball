package com.tanuj.bouncyball;

import java.net.URL;
import java.util.Random;

import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;


import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements OnTouchListener, SensorEventListener {

	MySurface mySurfaceView;    // class handling the surfaceview 
	Blocks[] b = new Blocks[10];   // creating 10 blocks at a time
	powers[] pow = new powers[3];  // creating 3 powers at a time
	float x,y,dx,dy, radius;
	
	float iX, iY, fX, fY, deltaX, deltaY, game_dx;
	// variable declarations
	float energyloss = (float) 0.9;
	float dt =(float) 0.05;
	double bgX =0;
	double bgDX =0.5;
	float sensor_y;
	int score;
    int strike;
    static Boolean game_over =false;
    Boolean click;
	MediaPlayer back_sound;   //background sound
	SoundPool bounce;	      //bouncing sound 
	SensorManager sm;	      // sensors for handling gyroscope
	Sensor sensor;
	Random rand = new Random();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);             // Making display full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mySurfaceView = new MySurface(this);
		mySurfaceView.setOnTouchListener(this);
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);  
		sensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);  // registering sensor
		
		//initializing variables
		initial_blocks();            // initializing blocks
		for (int i=0;i<3;i++){
			pow[i] = new powers();   // creating initial random powers
		}
		x=y=0;
		dx=0;
		score =0;
		game_dx = -3;
		click = false;
		game_over = false;
		dy=5; 
		radius = 20;
		back_sound = MediaPlayer.create(MainActivity.this, R.raw.thunder_small);  //initializing sounds
		bounce = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		strike =  bounce.load(this, R.raw.woosh, 1);
		back_sound.start();
		back_sound.setLooping(true);    //looping the background music
		
		setContentView(mySurfaceView);
				
		}
	/*
	public void moveRight(){
		if(dx + 1 <20){
			dx++;
		}
	}
	
	public void moveLeft(){
		if(dx - 1 > -20){
			dx--;
		}
	} */
	
	public void initial_blocks() {                 // sets up initial position of blocks at the beginning of each new game
		// TODO Auto-generated method stub
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int height = size.y;
		for (int i=0;i<b.length;i++){
			b[i] = new Blocks(140*i, height/2);   // creating initial random blocks 
		}
		
	}

	@Override
	protected void onPause() {                // handling pause events
		// TODO Auto-generated method stub
		super.onPause();
		sm.unregisterListener(this);   // unregistering listeners and sounds
		mySurfaceView.pause();
		back_sound.release();
		
		
	}

	@Override
	protected void onResume() {             // starting activity on resume
		// TODO Auto-generated method stub
		super.onResume();
		mySurfaceView.resume();   
		back_sound.start();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {   
		// TODO Auto-generated method stub
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: 
			iX =  event.getX();      // getting the co-ordinates of touch event to re-start the game after game over
			iY = event.getY();
			// radius=0;
			break;
	
		case MotionEvent.ACTION_UP:
			fX =  event.getX();      
			fY = event.getY();
			
			break;
		}
		deltaX = fX - iX;
		if(deltaX>0){
			dx++;
		}else{
			dx--;
		}

		if(game_over ){
			click = true;
				
			}	
		
		return true;
	}

	public class MySurface extends SurfaceView implements Runnable {    // class handling surface view

		SurfaceHolder myHolder;
		Thread myThread = null;
		Bitmap background;
		
		Paint circle, rect, powers_color, font_color;
		
		boolean isRunning = false;

		public MySurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			myHolder = getHolder();
			
			 background = BitmapFactory.decodeResource(getResources(),R.drawable.background);
		}
		
		

		public void pause() {
			isRunning = false;
			while (true) {
				try {
					myThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			myThread = null;
			// myThread.destroy();
		}

		public void resume() {
			isRunning = true;
			myThread = new Thread(this);
			myThread.start();
		}

		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Bitmap background;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			circle = new Paint();          // initial declarations for this class
			rect = new Paint();
			font_color =  new Paint();
			Rect frect = new Rect();
			Rect frect2 = new Rect();
			Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
			String s;
			click = false;
			while (isRunning) {
				if (!myHolder.getSurface().isValid())
					continue;
				
				Canvas canvas = myHolder.lockCanvas();           // locking the canvas
				
				game_dx = -3  - (int)(score/1000);               // speed of game. It increases with increasing levels every 1000 points
				
				if (bgX > - 3* canvas.getWidth() + 1){            // moving background image        
					bgX = bgX - bgDX;
				}else{
					bgX =0;
				}
				frect.set((int) bgX, 0, (int) (bgX + canvas.getWidth() * 2.5), canvas.getHeight());      // setting background bitmap
				frect2.set((int) (bgX + 2.5 * canvas.getWidth()), 0, (int) (bgX + canvas.getWidth() * 5), canvas.getHeight());
				canvas.drawBitmap(background, null, frect, null);
				canvas.drawBitmap(background, null, frect2, null);
				
			//	canvas.drawBitmap(background, 0, 0, null);
				if(x+dx > canvas.getWidth() - radius ){            //checking for collisions with the walls
					x = canvas.getWidth() - radius -1;
					dx = - dx;
					bounce.play(strike, 1, 1, 0, 0, 1);
				}else if (x + dx <0 + radius){
					x = radius;
					dx = -dx;
					bounce.play(strike, 1, 1, 0, 0, 1);
				}
				if(y+dy > canvas.getHeight() + radius ){         // handling game over events. when the balls goes down the display
					//Game Over
					y = canvas.getHeight() + 3 * radius;
					dy = 0;
					game_over = true;
					game_dx = 0;
					font_color.setTypeface(tf);
					font_color.setTextSize(40);
					font_color.setColor(Color.RED);
					canvas.drawRect( canvas.getWidth()/2 -50, canvas.getHeight()/2 -100, 
							canvas.getWidth()/2 +200, canvas.getHeight()/2 -20 , font_color);
					font_color.setColor(Color.WHITE);
					canvas.drawText("PLAY AGAIN", canvas.getWidth()/2 -50, canvas.getHeight()/2 -50 , font_color);
					 // restarting the game in event of play again is clicked
					if(click && (iX >= (canvas.getWidth()/2 -50)) && (iX <= (canvas.getWidth()/2 +200)) 
							&& (iY >= (canvas.getHeight()/2 -100)) && (iY <= (canvas.getWidth()/2 -20))){
						game_over = false;
						click = false;               //setting initial variables and block positions
						score = 0;
						game_dx =-3;
						dx = 0;
						dy =5;
						y = 0;
						initial_blocks(); 
						
					}
					
					
					
					
				}else if (y + dy <0 + radius){
					y = radius;
					bounce.play(strike, 1, 1, 0, 0, 1);
					dy = -dy;
				}
				for (int i=0; i< b.length;i++){                        // //checking for collisions with blocks
					if (y+radius > b[i].y && y+radius < b[i].y+ b[i].height && x>b[i].x && x<b[i].x+b[i].width ){
						dy = - dy;
						bounce.play(strike, 1, 1, 0, 0, 1);
						y = y - radius;
						
					}
				}
			//	powers_color.setColor(Color.rgb(99, 158,154));
				for (int i=0; i<3;i++){                      //checking for powers
					canvas.drawCircle(pow[i].x, pow[i].y, pow[i].radius, pow[i].powers_color);
					if(Math.sqrt((pow[i].x-x)*(pow[i].x-x) + (pow[i].y-y)* (pow[i].y-y)) <= (pow[i].radius + radius)){
						score += 100;
						if(pow[i].power_type){
							if(dy >= 3) {
								dy--;
								}
						}else if (dy<10){
							dy++;
						}
						pow[i].x = -10;
					}
					if(pow[i].x <= 0){        // generating new powers randomly after they are consumed 
						pow[i].x =canvas.getWidth()+ rand.nextInt(6 * canvas.getWidth());
						pow[i].y = (float) (canvas.getHeight()*0.2) +  rand.nextInt((int)(canvas.getHeight() * 0.7));
						pow[i].powers_color = new Paint();
						switch(rand.nextInt(2)){                     // randomly allocating kind of power
						case 0:
							pow[i].powers_color.setColor(Color.rgb(195, 85, 250));
							pow[i].power_type = false;
							break;
						case 1:
							pow[i].powers_color.setColor(Color.rgb(243, 204, 62));
							pow[i].power_type = true;
							break;
						}
						
						
					}else {
						pow[i].x+= 1.5 * game_dx;
					}
					
				}
				
				x+=dx;
				
				y = y - dy;
		
				if(!game_over){
				score++;
				}
				s = Integer.toString(score);
				font_color.setTypeface(tf);
				font_color.setTextSize(30);
				font_color.setColor(Color.BLACK);                //printing score
				
				canvas.drawText(s, canvas.getWidth() - 100 +3, 50 +3 , font_color);
				font_color.setColor(Color.rgb(198, 226, 255));
				canvas.drawText(s, canvas.getWidth() - 100, 50 , font_color);
				circle.setColor(Color.rgb(28, 148,254));
				
				
				switch((score/10)%4){                             // giving blocks an on-off lights feature
				case 0: rect.setColor(Color.rgb(218, 250,170));
					break;
				case 1: rect.setColor(Color.rgb(202, 250,130));
					break;
				case 2: rect.setColor(Color.rgb(165, 245,81));
					break;
				case 3: rect.setColor(Color.rgb(202, 250,130));
					break;
				}
				
				canvas.drawCircle(x, y, radius, circle);
				for (int i=0; i< b.length; i++){                                          // randomly generating blocks such that they are not
					b[i].width = canvas.getWidth()/7;                                     //overlapping each other
					canvas.drawRect(b[i].x, b[i].y, b[i].width + b[i].x, b[i].height+ b[i].y, rect);
					if(b[i].x + b[i].width <0){
						int fakei =i;
						if(i==0){
							fakei = b.length;
						}
						if(rand.nextInt(10)<(int)((score+2000)/1000)){    // more movable blocks at higher levels
							b[i].block_property = 1; //movable block
						}else{
							b[i].block_property = 0;
						}
						
						b[i].x =b[fakei-1].x + b[i].width + rand.nextInt(2*canvas.getWidth()/5);
						b[i].y = (float) (canvas.getHeight()*0.4) +  rand.nextInt((int) (canvas.getHeight() * 0.45));
						
					}else{ 
						b[i].x =b[i].x + game_dx;
						
						if(b[i].block_property == 1){                                 // moving the movable blocks up or down
							if(b[i].move){
								b[i].y += 1;
							}else {
								b[i].y = b[i].y - 1;
							}
							if (b[i].y > canvas.getHeight() * 0.9){
								b[i].y = (float) (canvas.getHeight() * 0.9);
								b[i].move = false;
							} else if (b[i].y < canvas.getHeight() * 0.1){
								b[i].y = (float) (canvas.getHeight() * 0.1);
								b[i].move = true;
							}
						}
					}
					
					
				}
				
				myHolder.unlockCanvasAndPost(canvas);   // after all the drawing on canvas, unlocking the screen and refreshing
			}
		}



		private Bitmap setBackgroundResource(Bitmap background) {
			// TODO Auto-generated method stub
			return null;
		}

	}

 
	public class Blocks {          // class to handle blocks
		public float dx;
		public float x, y, width, height;
		int block_property;
		Boolean move;
		
		public Blocks(){            // default constructor to initialize blocks
			
			dx = -10;
			x = rand.nextInt(600);
			y = 250 + rand.nextInt(300);
			block_property =0;
			width =120;
			height = 40;
			move = true;
			
		}
		public Blocks(int px, int py){     // constructor in case initial co-ordinates are passed
			
			dx = -10;
			x = px;
			y = py;
			width =120;
			block_property =0;
			height = 40;
			move = true;
			
		}
		
	
	}
	public class powers{        //class to handle powers creation 
		public float radius, x, y;
		boolean power_type;
		Paint powers_color;
		
		public powers(){             // randomly allocating powers
			radius = 8;
			powers_color = new Paint();
			switch(rand.nextInt(2)){
			case 0:
				powers_color.setColor(Color.rgb(195, 85, 250));
				power_type = false;
				break;
			case 1:
				powers_color.setColor(Color.rgb(243, 204, 62));
				power_type = true;
				break;
			}
			
			x = rand.nextInt(600);
			y = 100 + rand.nextInt(300);
			//power_type = rand.nextInt(3);
		}
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(isTablet(this)){
			sensor_y = event.values[1];  // if tablet then landscape mode is default. So sensor to move ball is along y direction
		}else {
			sensor_y = event.values[0]; // if phone then portrait mode is default. So sensor to move ball is along x direction
		}
		
		// sensor_y = event.values[1];
		dx = dx +sensor_y;
		if (dx>15){
			dx = 12;
		} else if(dx <-15){
			dx = -15;
		}
	}
	//  knowing if the default orientation is landscape or portrait 
	//depending upon type of device
	public static boolean isTablet(Context context) {    
	    return (context.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
}
