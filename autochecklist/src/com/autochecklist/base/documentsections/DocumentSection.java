package com.autochecklist.base.documentsections;

public class DocumentSection {

	private String mId;
	private String mDescription;

	public DocumentSection(String id, String description) {
		mId = id;
		mDescription = description;
	}

	public String getId() {
		return mId;
	}

	public String getDescription() {
		return mDescription;
	}
}
