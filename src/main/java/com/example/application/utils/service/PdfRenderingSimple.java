package com.example.application.utils.service;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * <a href=
 * "http://stackoverflow.com/questions/30882927/how-to-generate-dyanamic-no-of-pages-using-pdfbox">
 * How to generate Dyanamic no of pages using PDFBOX </a>
 * <p>
 * A simple PDF rendering class
 * </p>
 * 
 * @author mkl
 */
public class PdfRenderingSimple implements AutoCloseable {
	
    //
    // members
    //
    final PDDocument doc;

    private PDPageContentStream content = null;
    private int textRenderingLineY = 0;
	
	//
	// rendering
	//
	@SuppressWarnings("deprecation")
	public void renderText(String Info, int marginwidth) throws IOException {
		if (content == null || textRenderingLineY < 12)
			newPage();

		textRenderingLineY -= 12;
		System.out.print("lineno=" + textRenderingLineY);
		PDFont fontPlain = PDType1Font.HELVETICA;
		content.beginText();
		content.setFont(fontPlain, 10);
		content.moveTextPositionByAmount(marginwidth, textRenderingLineY);
		content.drawString(Info);
		content.endText();
	}

	//
	// constructor
	//
	public PdfRenderingSimple(PDDocument doc) {
		this.doc = doc;
	}

	//
	// AutoCloseable implementation
	//
	/**
	 * Closes the current page
	 */
	@Override
	public void close() throws IOException {
		if (content != null) {
			content.close();
			content = null;
		}
	}

	//
	// helper methods
	//
	void newPage() throws IOException {
		close();

		PDPage page = new PDPage();
		doc.addPage(page);
		content = new PDPageContentStream(doc, page);
		content.setNonStrokingColor(Color.BLACK);
  
		textRenderingLineY = 768;
	}
	
	public static void main(String[] args) throws IOException {
		PDDocument doc = new PDDocument();

		PdfRenderingSimple renderer = new PdfRenderingSimple(doc);
		for (int i = 0; i < 2000; i++) {
		    renderer.renderText("hello" + i, 60);
		}
		renderer.close();
		File file = new File("renderSimple.pdf");
		doc.save(file);
		doc.close();
		
		Desktop.getDesktop().open(file);
	}

}
