package com.autochecklist.base.documentsections;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.autochecklist.utils.Utils;

public class RequirementsTraceabilityMatrix {

	private final String[][] mMatrix;
	private final int requirementIdColumn;
	
	private String mContents;

	public RequirementsTraceabilityMatrix(String preprocFileName) {
		obtainRTM(preprocFileName);
		mMatrix = Utils.getMatrixFromCommaSeparatedValueString(mContents);
		if (mMatrix != null) {
			Utils.println("Traceability matrix detected in CSV format.");
			requirementIdColumn = findRequirementIdColumn();
			if (requirementIdColumn >= 0) {
				Utils.println("Found the requirement ID column in the matrix.");
			} else {
				Utils.println("Unable to find the requirement ID column in the matrix!");
			}
		} else {
			Utils.println("Traceability matrix is NOT in CSV format!");
			requirementIdColumn = -1;
		}
	}

	public boolean hasContents() {
		return !Utils.isTextEmpty(mContents);
	}

	public String getContents() {
		return mContents;
	}

	public boolean isInPreciseMode() {
		return (mMatrix != null) && (requirementIdColumn >= 0);
	}

	private boolean hasMatrix() {
		return mMatrix != null;
	}

	public String[][] getMatrix() {
		return mMatrix;
	}

	public int countInstances(String instance) {
		if (Utils.isTextEmpty(instance)) return -1;
		if (Utils.isTextEmpty(mContents)) return -1;

		return StringUtils.countMatches(mContents, instance);
	}

	public boolean containInstances(String instance) {
		return countInstances(instance) > 0;
	}

    public String getRequirementRow(String reqId) {
    	if (!isInPreciseMode()) return null;

    	for (String[] row : mMatrix) {
    		if (StringUtils.contains(row[requirementIdColumn], reqId)) {
    			return Arrays.toString(row);
    		}
    	}

    	return null;
    }

	private void obtainRTM(String fileName) {
		try {
			obtainRTM_internal(fileName);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			Utils.printError("Unable to parse the preprocessed file for obtaining requirement traceability matrix! - " + e.getMessage());
			throw new RuntimeException("Unable to parse preprocessed file for obtaining requirement traceability matrix! - " + e.getMessage());
		}
	}

	private void obtainRTM_internal(String fileName) throws SAXException, IOException, ParserConfigurationException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("SRS")) {
			Utils.printError("Inconsistence on preprocessed file!");
			throw new RuntimeException("Inconsistence on preprocessed file!");
		}

		NodeList nRTMSections = doc.getElementsByTagName("RTM");
		
		if (nRTMSections == null) { // It may be empty, but not null!
			Utils.printError("Inconsistence on preprocessed file for RTM matrix!");
			throw new RuntimeException("Inconsistence on preprocessed file for RTM matrix!");
		}

		Node nRTMSection = nRTMSections.item(0);
		if (nRTMSection == null) return;
		if (nRTMSection.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nRTMSection;
			mContents = eElement.getTextContent();
		}
	}

	private int findRequirementIdColumn() {
		if (!hasMatrix()) return -1;

		String[] firstRow = mMatrix[0];
		for (int i = 0; i < firstRow.length; i++) {
			if (StringUtils.containsIgnoreCase(firstRow[i], "requirement")) {
				return i;
			}
		}

		return -1;
	}
}
