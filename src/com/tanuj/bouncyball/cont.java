package com.tanuj.bouncyball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class cont extends Activity{

	Button our_button;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cont);
		our_button = (Button) findViewById(R.id.btn);
		our_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openActivity = new Intent("com.tanuj.bouncyball.MAINACTIVITY");
				startActivity(openActivity);
				finish();
			}
		});
	}
	

}
