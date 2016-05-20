package activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.deraz.motiontest.MainActivity;
import com.example.deraz.motiontest.R;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

public class MainMenu extends AppCompatActivity {

	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;
	private Button btnStart;

	private SQLiteHandler db;
	private SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);


		txtName = (TextView) findViewById(R.id.name);
		txtEmail = (TextView) findViewById(R.id.email);
		btnLogout = (Button) findViewById(R.id.btnLogout);
		btnStart = (Button) findViewById(R.id.btnStart);

		// SqLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// session manager
		session = new SessionManager(getApplicationContext());

		if (!session.isLoggedIn()) {
			logoutUser();
		}

		// Fetching user details from SQLite
		HashMap<String, String> user = db.getUserDetails();

		final String name = user.get("name");
		final String email = user.get("email");
		final String uid = user.get("uid");


		// Displaying the user details on the screen
		txtName.setText(name);
		txtEmail.setText(email);

		// Logout button click event
		btnLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				logoutUser();
			}
		});
		btnStart.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v){
				Intent intent = new Intent(v.getContext(), MainActivity.class);
				intent.putExtra("user",uid);
				intent.putExtra("email",email);
				intent.putExtra("name",name);
				startActivity(intent);
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_settings:
			{
				Intent intent = new Intent();
				intent.setClassName(this, "activity.MyPrefenceActivity");
				startActivity(intent);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	private void logoutUser() {
		session.setLogin(false);

		db.deleteUsers();

		// Launching the login activity
		Intent intent = new Intent(MainMenu.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
}
