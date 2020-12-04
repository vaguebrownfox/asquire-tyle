package aashi.fiaxco.asquiremoon0x0b.providestuff;

import java.util.HashMap;

public class Questions {
	public  HashMap<String, Question> questionHashMap = new HashMap<>();

	public Question question1 = new Question(
			1,
			"What is your smoking status?",
			new String[]{"Ex-smoker(1 year)", "Current smoker", "Non-smoker"},
			new int[]{2});


	public Question question2 = new Question(2,
			"Do you have repeated episodes of cough?",
			new String[]{"Yes", "No"},
			new int[]{3, 5});


	public Question question3 = new Question(3,
			"How many times in a year cough-episode occurs?",
			new String[]{"1", "2", "3", ">3"},
			new int[]{4});


	public Question question4 = new Question(4,
			"How long does each episode last?",
			new String[]{"less that 3 times", "more than 3 times"},
			new int[]{5});


	public Question question5 = new Question(5,
			"Are you a known case of Asthma?",
			new String[]{"Yes", "No"},
			new int[]{6, 19});


	public Question question6 = new Question(6,
			"Wheeze and chest tightness present?",
			new String[]{"Yes", "No"},
			new int[]{7, 11});


	public Question question7 = new Question(7,
			"For how long is the wheeze present? (days)",
			new String[]{},
			new int[]{8});


	public Question question8 = new Question(8,
			"Do you experience episodic or continuous wheeze?",
			new String[]{"Episodic", "Continuous", "Can't say"},
			new int[]{9});


	public Question question9 = new Question(9,
			"Does your wheeze vary with seasons?",
			new String[]{"Yes", "No"},
			new int[]{10});


	public Question question10 = new Question(10,
			"Does your wheeze vary across the day?",
			new String[]{"Yes", "No"},
			new int[]{11});


	public Question question11 = new Question(11,
			"Do you have cough?",
			new String[]{"Yes", "No"},
			new int[]{12, 14});


	public Question question12 = new Question(12,
			"Do you have dry or wet cough?",
			new String[]{"Wet", "Dry"},
			new int[]{13, 14});


	public Question question13 = new Question(13,
			"Sputum color",
			new String[]{"White", "Yellow", "Green"},
			new int[]{14});


	public Question question14 = new Question(14,
			"Are you under any medication for asthma?",
			new String[]{"Yes", "No"},
			new int[]{15});


	public Question question15 = new Question(15,
			"Do you use inhalers or nebulizers?",
			new String[]{"Inhalers", "Nebulizers", "Both", "Don't use anything"},
			new int[]{16});


	public Question question16 = new Question(16,
			"Do you have family history of asthma?",
			new String[]{"Yes", "No"},
			new int[]{17});


	public Question question17 = new Question(17,
			"Do you have allergies?",
			new String[]{"Yes", "No"},
			new int[]{18, 19});


	public Question question18 = new Question(18,
			"Triggers for allergies?",
			new String[]{"No Triggers", "Pollution", "Strong smells", "Pollen", "Other"},
			new int[]{19});


	public Question question19 = new Question(19,
			"Have you suffered from lung TB in the past?",
			new String[]{"Yes", "No"},
			new int[]{20});


	public Question question20 = new Question(20,
			"Do you have any other respiratory illness?",
			new String[]{"Yes", "No"},
			new int[]{21, 22});


	public Question question21 = new Question(21,
			"Write in brief About it",
			new String[]{},
			new int[]{22});


	public Question question22 = new Question(22,
			"Are you a known case of high blood pressure?",
			new String[]{"Yes", "No"},
			new int[]{23, 24});


	public Question question23 = new Question(23,
			"Your high blood pressure is",
			new String[]{"Well controlled", "Nearly controlled"},
			new int[]{24});


	public Question question24 = new Question(24,
			"Area you a known case of diabetes?",
			new String[]{"Yes", "No"},
			new int[]{25, 26});


	public Question question25 = new Question(25,
			"Your diabetes is",
			new String[]{"Well controlled", "Nearly controlled"},
			new int[]{26});


	public Question question26 = new Question(26,
			"Are you a known case of heart disease?",
			new String[]{"Yes", "No"},
			new int[]{27});


	public Question question27 = new Question(27,
			"Any other health problems?",
			new String[]{"Yes", "No"},
			new int[]{28, -1});


	public Question question28 = new Question(28,
			"What health problems?",
			new String[]{},
			new int[]{-1});



	public HashMap<String, Question> getQuestionHashMap() {
		questionHashMap.put("1", question1);
		questionHashMap.put("2", question2);
		questionHashMap.put("3", question3);
		questionHashMap.put("4", question4);
		questionHashMap.put("5", question5);
		questionHashMap.put("6", question6);
		questionHashMap.put("7", question7);
		questionHashMap.put("8", question8);
		questionHashMap.put("9", question9);
		questionHashMap.put("10", question10);
		questionHashMap.put("11", question11);
		questionHashMap.put("12", question12);
		questionHashMap.put("13", question13);
		questionHashMap.put("14", question14);
		questionHashMap.put("15", question15);
		questionHashMap.put("16", question16);
		questionHashMap.put("17", question17);
		questionHashMap.put("18", question18);
		questionHashMap.put("19", question19);
		questionHashMap.put("20", question20);
		questionHashMap.put("21", question21);
		questionHashMap.put("22", question22);
		questionHashMap.put("23", question23);
		questionHashMap.put("24", question24);
		questionHashMap.put("25", question25);
		questionHashMap.put("26", question26);
		questionHashMap.put("27", question27);
		questionHashMap.put("28", question28);

		return questionHashMap;
	}
}