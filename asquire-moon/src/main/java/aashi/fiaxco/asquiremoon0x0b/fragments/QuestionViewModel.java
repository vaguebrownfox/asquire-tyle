package aashi.fiaxco.asquiremoon0x0b.fragments;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.net.InetAddress;
import java.util.HashMap;

import aashi.fiaxco.asquiremoon0x0b.providestuff.Question;
import aashi.fiaxco.asquiremoon0x0b.providestuff.Questions;

public class QuestionViewModel extends ViewModel {


	public HashMap<String, Question> mQuestions = new Questions().getQuestionHashMap();

	private final MutableLiveData<Bundle> currentAnsweredQuestion = new MutableLiveData<>();

	public LiveData<Bundle> getCurrentAnsweredQuestion() {
		return currentAnsweredQuestion;
	}

	public void setCurrentAnsweredQuestion(Bundle questionAnsweredBundle) {
		currentAnsweredQuestion.setValue(questionAnsweredBundle);
	}

	private void setAnswer(Bundle bundle) {
		int qNo = bundle.getInt(QuestionFragment.QNO_PARAM);
		String answer = bundle.getString(QuestionFragment.ANSWER);
		int[] nQns = bundle.getIntArray(QuestionFragment.NQ_PARAM);

		mQuestions.get("" + qNo).setAnswer(answer);
	}

}