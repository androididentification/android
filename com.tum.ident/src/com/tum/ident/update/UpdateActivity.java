package com.tum.ident.update;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.tum.ident.R;

public class UpdateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);// https://www.dropbox.com/sh/207l1abkg47di69/AADMvyALl8rD8RDCzSEcgCeEa?dl=0
		Intent browserIntent = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("https://www.dropbox.com/s/nxeq73ujflr1e10/Ident.apk?dl=1"));
		startActivity(browserIntent);

		finish();
	}


}
