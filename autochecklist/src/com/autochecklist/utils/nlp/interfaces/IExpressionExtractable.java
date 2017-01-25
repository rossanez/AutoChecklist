package com.autochecklist.utils.nlp.interfaces;

import java.util.List;

import com.autochecklist.utils.Pair;

public interface IExpressionExtractable {

	List<Pair<String, String>> extract(String text);
}
