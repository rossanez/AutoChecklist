package com.autochecklist.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NumberAndUnitOccurrences {

    private Map<String, Set<String>> mOccurrences;

	public NumberAndUnitOccurrences() {
		mOccurrences = new HashMap<String, Set<String>>();
	}

	public Set<String> put(String key, String value) {
		Set<String> currValues = mOccurrences.get(key);
		if (currValues == null) {
			currValues = new HashSet<String>();
		}
        currValues.add(value);
		
		return mOccurrences.put(key, currValues);
	}

	public Set<String> get(String key) {
		return mOccurrences.get(key);
	}

	public boolean isEmpty() {
		return mOccurrences.isEmpty();
	}

	public Set<String> getAllOccurrences() {
		return mOccurrences.keySet();
	}
}