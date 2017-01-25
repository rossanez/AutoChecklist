package com.autochecklist.utils.nlp.interfaces;

import java.util.Set;

public interface IMissingNumericValuesIndicativeDetectable {

	Set<String> detect(String text);
}
