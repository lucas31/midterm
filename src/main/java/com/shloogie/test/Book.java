package com.shloogie.test;

import java.time.LocalDate;
import java.util.ArrayList;

public class Book {
	public int noPages;
	public String author;
	private LocalDate publishedOn = LocalDate.of(2017, 10, 01);
	public String title;
	public Student s;
	private ArrayList<String> reviewers = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{ add("rev1"); add("rev2"); add("rev3"); }};

	public Boolean positivelyReceived;
	public char checkCharacter;
	@SuppressWarnings("unused")
	private float wantedToSoldRatio = 6.66f;
	
	public float getWantedToSoldRatio() {
		return wantedToSoldRatio;
	}
	public void setWantedToSoldRatio(float wantedToSoldRatio) {
		this.wantedToSoldRatio = wantedToSoldRatio;
	}
	public ArrayList<String> getReviewers() {
		return reviewers;
	}
	public void setReviewers(ArrayList<String> reviewers) {
		this.reviewers = reviewers;
	}
	public LocalDate getPublishedOn() {
		return publishedOn;
	}
	public void setPublishedOn(LocalDate publishedOn) {
		this.publishedOn = publishedOn;
	}
}
