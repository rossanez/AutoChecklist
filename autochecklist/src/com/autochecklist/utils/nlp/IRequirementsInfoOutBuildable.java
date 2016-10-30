package com.autochecklist.utils.nlp;

import java.io.File;

public interface IRequirementsInfoOutBuildable {
	
	void createNewRequirement(String req);
	void addRequirementText(String reqText);
    void appendRequirementText(String reqText);

    void addDocumentSection(String id, String name);

    File generateOutputFile(String fileName);
}
