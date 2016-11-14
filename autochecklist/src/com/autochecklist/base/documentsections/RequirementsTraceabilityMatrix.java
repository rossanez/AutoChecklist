package com.autochecklist.base.documentsections;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.autochecklist.utils.Utils;

public class RequirementsTraceabilityMatrix {

	private String mContents;

	public RequirementsTraceabilityMatrix(String preprocFileName) {
		obtainRTM(preprocFileName);
	}

	public String getContents() {
		return mContents;
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
		
		if ((nRTMSections == null) || (nRTMSections.getLength() != 1)) {
			Utils.printError("Inconsistence on preprocessed file for RTM matrix!");
			throw new RuntimeException("Inconsistence on preprocessed file for RTM matrix!");
		}

		Node nRTMSection = nRTMSections.item(0);
		if (nRTMSection.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nRTMSection;
			mContents = eElement.getTextContent();
		}
	}
}