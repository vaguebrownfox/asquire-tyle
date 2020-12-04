package aashi.fiaxco.asquiremoon0x0b.providestuff;

public class Question {

	private final int qNo;
	private final String question;
	private final String[] options;
	private final int[] nextQuestion;

	private String answer = "";
	private boolean answered = false;

	// constructor
	public Question(int qNo, String question, String[] options, int[] nextQuestion) {
		this.qNo = qNo;
		this.question = question;
		this.options = options;
		this.nextQuestion = nextQuestion;
	}

	// getters and setters
	public int getqNo() {
		return qNo;
	}

	public String getQuestion() {
		return question;
	}

	public String[] getOptions() {
		return options;
	}


	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}


	public String getAnswer() {
		return answer;
	}

	public boolean isAnswered() {
		return answered;
	}


	public int[] getNextQuestion() {
		return nextQuestion;
	}

}
