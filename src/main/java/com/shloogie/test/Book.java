package com.shloogie.test;

import java.time.LocalDate;
import java.util.ArrayList;

public class Book {
	public int noPages;
	public String author;
	public LocalDate publishedOn;
	public String title;
	public Student s;
	public ArrayList<String> reviewers = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{ add("rev1"); add("rev2"); add("rev3"); }};
	public Boolean positivelyReceived;
	public char checkCharacter;
	//@SuppressWarnings("unused")
	//private float wantedToSoldRatio = 6.66f;
}
