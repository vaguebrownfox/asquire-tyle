package aashi.fiaxco.asquiremoon0x0b;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserIDActivity extends AppCompatActivity {

	// Constants
	private static final String TAG = "UserIDActivity";
	public static final String USER_ID = "user_id_from_user_id_activity";
	private static final int SURVEY_ACT_RES_CODE = 890;
	private static final int AUDIO_EFFECT_REQUEST = 9999;

	private final short MAX_USERS = 5;
	private final short ID_LENGTH = 8;

	// UI
	private RadioGroup mUserInButtonsRG;
	private EditText mUserNameET;
	private FloatingActionButton mAddUserFAB;
	private SwitchCompat mDarkModeSwitch;

	// Data
	private Map<String, ?> mUserMap;
	private String mUserID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find View
		{
			mUserInButtonsRG = findViewById(R.id.main_buttons_RG);
			mUserNameET = findViewById(R.id.main_username_ET);
			mAddUserFAB = findViewById(R.id.main_add_user_FAB);
			mDarkModeSwitch = findViewById(R.id.user_dark_mode_switch);
		}

		// On Click
		setUpOnClickListeners();
	}
	@Override
	protected void onResume() {
		super.onResume();

		mUserInButtonsRG.removeAllViews();
		mUserMap = readUsers();
		for (String user : mUserMap.keySet()) {
			addButtonToRG(user);
		}
	}



	private void setUpOnClickListeners() {
		// FAB
		mAddUserFAB.setOnClickListener(view -> fabAddUser());

		// User Radio buttons
		mUserInButtonsRG.setOnCheckedChangeListener((radioGroup, i) -> {
			requestRecordPermission();
			if (isRecordPermissionGranted()) {
				startSurvey(radioGroup);
			}

		});

		// Dark Mode toggle
		mDarkModeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
			if (b) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
			else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		});

	}

	private void startSurvey(RadioGroup radioGroup) {
		RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
		String name = radioButton.getText().toString();
		mUserID = Objects.requireNonNull(mUserMap.get(name)).toString();
		makeToast(mUserID);
		Intent surveyIntent =
				new Intent(UserIDActivity.this, SurveyActivity.class);
		surveyIntent.putExtra(USER_ID, mUserID);
		startActivityForResult(surveyIntent, SURVEY_ACT_RES_CODE);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SURVEY_ACT_RES_CODE) {
			if(resultCode == Activity.RESULT_OK) {
				assert data != null;
				Locale locale = new Locale("ENG");
				SimpleDateFormat s = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", locale);
				String timestamp = s.format(new Date());

				Intent recIntent = new Intent(UserIDActivity.this, RecordActivity.class);
				recIntent.putExtra(USER_ID, mUserID);
				recIntent.putExtra(SurveyActivity.TIMESTAMP, timestamp);
				startActivity(recIntent);
			}
		}
	}



	private void fabAddUser() {
		String name = mUserNameET.getText().toString();
		if (!name.isEmpty() && mUserMap.size() < MAX_USERS) {
			mUserNameET.setText("");
			addUser(name);
		} else {
			String err = mUserMap.size() >= MAX_USERS ? "Cannot add anymore... Sorry!" :
					"Need more character..s";
			mUserNameET.setText("");
			mUserNameET.setHint(err);
			mUserNameET.postDelayed(
					() -> mUserNameET.setHint("New user? Create username"), 2000);
		}
	}


	// Support Methods
	private void addUser(String name) {
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		String userID =
				name + "-" + UUID.randomUUID().toString().substring(0, ID_LENGTH);
		editor.putString(name, userID);
		editor.apply();

		try {
			Objects.requireNonNull(mUserMap.get(name));
		} catch (Exception e) {
			e.printStackTrace();
			mUserMap = sharedPref.getAll();
			addButtonToRG(name);
		}
	}
	private void addButtonToRG(String usr) {
		RadioButton radioButton = new RadioButton(this);
		radioButton.setId(View.generateViewId());
		radioButton.setText(usr);
		radioButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

		mUserInButtonsRG.addView(radioButton);
	}
	private Map<String, ?> readUsers() {
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getAll();
	}

	// Permissions
	private boolean isRecordPermissionGranted() {
		return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
				PackageManager.PERMISSION_GRANTED);
	}

	private void requestRecordPermission() {
		ActivityCompat.requestPermissions(
				this,
				new String[]{Manifest.permission.RECORD_AUDIO},
				AUDIO_EFFECT_REQUEST);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (AUDIO_EFFECT_REQUEST != requestCode) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);

			return;
		}

		if (grantResults.length != 1 ||
				grantResults[0] != PackageManager.PERMISSION_GRANTED) {

			makeToast(getString(R.string.need_record_audio_permission));
		}
	}

	// Misc
	private void makeToast(String msg) {
		Toast.makeText(UserIDActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
}