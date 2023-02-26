package com.example.application.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

public class PdfiPDFUtil {
	
	public void generatePdf(List<byte[]> pdfList) throws IOException {
		
		PDFMergerUtility merger = new PDFMergerUtility();
		
		
		for (byte[] pdfByte : pdfList) {
			InputStream targetStream = new ByteArrayInputStream(pdfByte);
			merger.addSource(targetStream);
		
		}
		
		try {
			
			merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
