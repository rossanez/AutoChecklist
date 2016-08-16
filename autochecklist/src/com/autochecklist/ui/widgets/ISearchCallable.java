package com.autochecklist.ui.widgets;

public interface ISearchCallable {

	void onSearch(String text, boolean wrapAround);
	
	void onClose();
}
