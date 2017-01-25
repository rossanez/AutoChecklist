package com.autochecklist.utils.nlp;

import java.util.Set;

public interface IMissingNumericValuesIndicativeDetectable {

	Set<String> detect(String text);
}
