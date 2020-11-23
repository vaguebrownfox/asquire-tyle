package aashi.fiaxco.asquiretyle0x0a;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserIDActivity extends AppCompatActivity {

	// Constants
	public static final String USER_ID = "user_id_from_user_id_activity";
	private final short MAX_USERS = 3;
	private final short ID_LENGTH = 8;

	// UI
	private RadioGroup mUserInButtonsRG;
	private EditText mUserNameET;
	private FloatingActionButton mAddUserFAB;

	// Data
	private Map<String, ?> mUserMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find View
		{
			mUserInButtonsRG = findViewById(R.id.main_buttons_RG);
			mUserNameET = findViewById(R.id.main_username_ET);
			mAddUserFAB = findViewById(R.id.main_add_user_FAB);
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

	// Ext Methods
	private void setUpOnClickListeners() {
		// FAB
		mAddUserFAB.setOnClickListener(view -> {
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
		});

		// User Radio buttons
		mUserInButtonsRG.setOnCheckedChangeListener((radioGroup, i) -> {
			RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
			String name = radioButton.getText().toString();
			String userId = Objects.requireNonNull(mUserMap.get(name)).toString();

			Toast.makeText(getApplicationContext(),
					Objects.requireNonNull(mUserMap.get(name)).toString(),
					Toast.LENGTH_SHORT).show();

			Intent surveyIntent =
					new Intent(UserIDActivity.this, SurveyActivity.class);
			surveyIntent.putExtra(USER_ID, userId);
			startActivity(surveyIntent);
		});

	}

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
}
