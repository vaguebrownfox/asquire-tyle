package aashi.fiaxco.asquiremoon0x0b;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

import aashi.fiaxco.asquiremoon0x0b.fragments.QuestionFragment;
import aashi.fiaxco.asquiremoon0x0b.fragments.QuestionViewModel;
import aashi.fiaxco.asquiremoon0x0b.providestuff.Question;
import aashi.fiaxco.asquiremoon0x0b.providestuff.Questions;

public class SurveyActivity extends AppCompatActivity {

	// Constants
	private static final String TAG = "SurveyActivity";

	// UI
	ScrollView mConsentView, mQuestionsSV;
	LinearLayout mUserInfoView, mQuestionsLL;
	Button mDeclineButton, mAcceptButton, mOkButton, mDoneButton;
	EditText nameET, ageET, heightET, weightET;
	Spinner genderSPN;

	// Data
	private String mUserID;
	private String mName, mAge, mGender, mHeight, mWeight;
	private QuestionViewModel mViewModel;
	private HashMap<String, Question> mQuestionHashMap;
	private HashMap<String, QuestionFragment> mQuestionFragMap;
	private HashMap<String, String> mAnswers;
	private int mNQs;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);

		// Get Data from Previous Activity
		Intent pIntent = getIntent();
		mUserID = pIntent.getStringExtra(UserIDActivity.USER_ID);

		mViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

		// Questions
		mQuestionHashMap = new Questions().getQuestionHashMap();
		mQuestionFragMap = new HashMap<>();
		mAnswers = new HashMap<>();

		// Find View
		{
			mDeclineButton = findViewById(R.id.survey_consent_decline);
			mAcceptButton = findViewById(R.id.survey_consent_accept);
			mConsentView = findViewById(R.id.survey_consent_scroll);

			nameET = findViewById(R.id.edit_profile_name);
			ageET = findViewById(R.id.edit_profile_age);
			heightET = findViewById(R.id.edit_profile_height);
			weightET = findViewById(R.id.edit_profile_weight);
			genderSPN = findViewById(R.id.spinner_gender);
			mOkButton = findViewById(R.id.userInfo_survey_button);
			mUserInfoView = findViewById(R.id.survey_userinfo_linearLayout);

			mQuestionsLL = findViewById(R.id.survey_questions_ll);
			mQuestionsSV = findViewById(R.id.survey_questions_scrollV);

			mDoneButton = findViewById(R.id.survey_done_button);
		}

		// Initialize UI
		initUIValues();

		// Set observers
		setObservers();

		// Set On Click Listeners
		setOnClickListeners();

	}

	private void initUIValues() {

		SharedPreferences sharedPref = SurveyActivity.this.getPreferences(Context.MODE_PRIVATE);
		boolean consentAccept = sharedPref.getBoolean(mUserID + "accept", false);
		mConsentView.setVisibility(consentAccept ? View.INVISIBLE : View.VISIBLE);
		mUserInfoView.setVisibility(!consentAccept ? View.INVISIBLE : View.VISIBLE);

		setupSpinner();


		Set<String> keys = mViewModel.mQuestions.keySet();
		mNQs = keys.size();
		for (String k : keys) {
			Question q = mViewModel.mQuestions.get(k);
			mQuestionFragMap.put(k, QuestionFragment.newInstance(q));
		}

	}

	private void setObservers() {
		mViewModel.getCurrentAnsweredQuestion().observe(this, new Observer<Bundle>() {
			@Override
			public void onChanged(Bundle bundle) {
				int qNo = bundle.getInt(QuestionFragment.QNO_PARAM);
				String answer = bundle.getString(QuestionFragment.ANSWER);
				int[] nQns = bundle.getIntArray(QuestionFragment.NQ_PARAM);


				Question question = mViewModel.mQuestions.get("" + qNo);

				// for debug
				/*if (nQns[0] != -1) {

						if (nQns.length == 1 || mViewModel.mQuestions.get("" + qNo).getOptions()[0].equals(answer)) {
							addQuestionFragment(nQns[0]);
						} else if (nQns[1] != -1) {
							addQuestionFragment(nQns[1]);
						} else {
							//addProceedButton();
						}

				}*/
				/*if (nQns.length > 1) {
					removeFragFrom(qNo + 1);
					Set<String> keys = mViewModel.mQuestions.keySet();
					for(String k : keys) {
						Question q = mViewModel.mQuestions.get(k);
						mQuestionFragMap.put(k, QuestionFragment.newInstance(q));
					}
				}
				addQuestionFragment(qNo + 1);


				if (nQns[0] != -1) {
					if (nQns.length == 1 || question.getOptions()[0].equals(answer)) {
						addQuestionFragment(nQns[0]);
					} else if (nQns[1] != -1) {
						removeFragFrom(qNo + 1);
						Set<String> keys = mViewModel.mQuestions.keySet();
						for(String k : keys) {
							Question q = mViewModel.mQuestions.get(k);
							mQuestionFragMap.put(k, QuestionFragment.newInstance(q));
						}
						addQuestionFragment(nQns[1]);
					} else {
						makeToast("Proceed");
					}
				}*/

				// Working logic
				/*if (nQns[0] != -1) {
					if (nQns.length == 1) {
						if (mAnswers.get("" + qNo) == null)
							addQuestionFragment(nQns[0]);
					} else if (nQns[1] != -1) {
						removeFragFrom(qNo + 1);
						for (int i = qNo + 1; i < 28 + 1; i++) {
							Question q = mViewModel.mQuestions.get("" + i);
							mQuestionFragMap.put("" + i, QuestionFragment.newInstance(q));
						}
						if (question.getOptions()[0].equals(answer)){
							addQuestionFragment(nQns[0]);
						} else {
							addQuestionFragment(nQns[1]);
						}
					} else {
						removeFragFrom(qNo + 1);
						for (int i = qNo + 1; i < 28 + 1; i++) {
							Question q = mViewModel.mQuestions.get("" + i);
							mQuestionFragMap.put("" + i, QuestionFragment.newInstance(q));
						}
						if (question.getOptions()[0].equals(answer))
							addQuestionFragment(nQns[0]);
						//makeToast("Proceed");
					}
				}*/

				// Working ++
				/*if (nQns[0] != -1) {
					if (nQns.length == 1) {
						if (mAnswers.get("" + qNo) == null)
							addQuestionFragment(nQns[0]);
					} else  {
						removeFragFrom(qNo + 1);
						for (int i = qNo + 1; i < 28 + 1; i++) {
							Question q = mViewModel.mQuestions.get("" + i);
							mQuestionFragMap.put("" + i, QuestionFragment.newInstance(q));
						}
						if (nQns[1] != -1) {
							if (question.getOptions()[0].equals(answer)) {
								addQuestionFragment(nQns[0]);
							} else {
								addQuestionFragment(nQns[1]);
							}
						} else {
							if (question.getOptions()[0].equals(answer)) {
								addQuestionFragment(nQns[0]);
							}
						}
					}
				}*/

				// Working x_x
				if (nQns[0] != -1) {
					if (nQns.length == 1) {
						if (mAnswers.get("" + qNo) == null) {
							addQuestionFragment(nQns[0]);
						}
					} else {
						removeFragFrom(qNo + 1);
						if (question.getOptions()[0].equals(answer)) {
							addQuestionFragment(nQns[0]);
						} else if (nQns[1] != -1) {
							addQuestionFragment(nQns[1]);
						}
					}
				}
				mAnswers.put("" + qNo, answer);


				mDoneButton.setVisibility(qNo == mNQs || qNo == mNQs - 1 ? View.VISIBLE : View.GONE);
			}
		});
	}

	private void removeFragFrom(int nQ) {
		for (int i = nQ; i < mNQs + 1; i++) {
			try {
				removeQuestionFragment(i);
				mAnswers.put("" + i, null);
				Question q = mViewModel.mQuestions.get("" + i);
				mQuestionFragMap.put("" + i, QuestionFragment.newInstance(q));
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "removeFragFrom: " + e);
			}
		}
	}


	private void setOnClickListeners() {

		mDeclineButton.setOnClickListener(view -> {
			Intent intent = new Intent(SurveyActivity.this, UserIDActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		});

		mAcceptButton.setOnClickListener(view -> {
			mConsentView.setVisibility(View.GONE);
			mUserInfoView.setVisibility(View.VISIBLE);

			SharedPreferences sharedPref = SurveyActivity.this.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			String acceptID = mUserID + "accept";
			editor.putBoolean(acceptID, true);
			editor.apply();

		});

		mOkButton.setOnClickListener(view -> {
			mName = nameET.getText().toString();
			mAge = ageET.getText().toString();
			mHeight = heightET.getText().toString();
			mWeight = weightET.getText().toString();

			if (!checkForEmptyText() || true) { // true for debugging
				mUserInfoView.setVisibility(View.GONE);

				// TODO: Store user info data

				addQuestionFragment(1); // add first question

			} else
				Toast.makeText(this, "Fill all the fields", Toast.LENGTH_SHORT).show();
		});


	}

	public void addQuestionFragment(int qNo) {

		try {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(mQuestionsLL.getId(), mQuestionFragMap.get("" + qNo)).commit();
		} catch (Exception e) {
			e.printStackTrace();
			makeToast("already dead");
		}

		scrollToBottom();
	}

	public void removeQuestionFragment(int qNo) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.remove(mQuestionFragMap.get("" + qNo)).commit();

		scrollToBottom();
	}

	private void scrollToBottom() {
		mQuestionsSV
				.postDelayed(() -> mQuestionsSV.fullScroll(View.FOCUS_DOWN), 200);
	}

	// setup gender spinner
	private void setupSpinner() {

		Spinner genderSpinner = findViewById(R.id.spinner_gender);
		ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.array_gender_options, android.R.layout.simple_spinner_item);

		genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		genderSpinner.setAdapter(genderSpinnerAdapter);

		genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mGender = (String) parent.getItemAtPosition(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mGender = "Other";
			}
		});

	}

	// returns true if empty
	private boolean checkForEmptyText() {
		return TextUtils.isEmpty(mName) ||
				TextUtils.isEmpty(mAge) ||
				TextUtils.isEmpty(mHeight) ||
				TextUtils.isEmpty(mWeight);
	}


	// Misc
	private void makeToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
