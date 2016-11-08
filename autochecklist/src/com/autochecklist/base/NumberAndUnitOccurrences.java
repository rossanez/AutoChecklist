package com.autochecklist.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
		// First, adding all the occurrences to a sorted set.
		// Providing the values sorted should be easier to find issues.
		SortedSet<String> sorted = new TreeSet<String>();
		sorted.addAll(mOccurrences.keySet());
		return sorted;
	}
}
