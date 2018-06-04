package br.gov.al.detran.birtreport.service;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import br.gov.al.detran.birtreport.model.Report;

public interface ReportRunner {

	ByteArrayOutputStream runReport(Report report);
    String runReportAndWritePDF(Report report);
}