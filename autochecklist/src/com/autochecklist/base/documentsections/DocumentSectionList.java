package com.autochecklist.base.documentsections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.autochecklist.utils.Utils;

public class DocumentSectionList {

	private List<DocumentSection> mDocumentSections;

	public DocumentSectionList() {
		mDocumentSections = new ArrayList<DocumentSection>();
	}

	public DocumentSectionList(String preprocFileName) {
		mDocumentSections = new ArrayList<DocumentSection>();
		obtainDocumentSections(preprocFileName);
	}

	public List<DocumentSection> getDocumentSections() {
		return mDocumentSections;
	}

	public void addSections(List<DocumentSection> sections) {
		mDocumentSections.addAll(sections);
	}

	private void obtainDocumentSections(String fileName) {
		try {
			obtainDocumentSections_internal(fileName);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			Utils.printError("Unable to parse the preprocessed file for obtaining document sections! - " + e.getMessage());
			throw new RuntimeException("Unable to parse preprocessed file for obtaining document sections! - " + e.getMessage());
		}
	}

	private void obtainDocumentSections_internal(String fileName) throws SAXException, IOException, ParserConfigurationException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("SRS")) {
			Utils.printError("Inconsistence on preprocessed file!");
			throw new RuntimeException("Inconsistence on preprocessed file!");
		}

		NodeList nSections = doc.getElementsByTagName("section");
		for (int i = 0; i < nSections.getLength(); i++) {
			Node nSection = nSections.item(i);
			if (nSection.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nSection;
				String id = eElement.getElementsByTagName("id")
						.item(0).getTextContent();
				String text = eElement.getElementsByTagName("name")
						.item(0).getTextContent();
				mDocumentSections.add(new DocumentSection(id, text));
			}
		}
	}
}
