package com.example.application.utils.service;

import org.springframework.stereotype.Service;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;

import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;

import java.io.ByteArrayOutputStream;

@Service
public class ReportConsolidatorService {
	

	public byte[] build(JasperReportBuilder[] builders) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			concatenatedReport()
					.concatenate(builders)
					.toPdf(baos);
		} catch (DRException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public JasperConcatenatedReportBuilder build2(JasperReportBuilder[] builders) {
	
			return concatenatedReport()
					.concatenate(builders);
	}

}
