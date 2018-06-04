package br.gov.al.detran.birtreport.model;

import java.util.WeakHashMap;

import br.gov.al.detran.birtreport.service.ReportRunner;

public class BIRTReport extends Report {

	
    public BIRTReport(String name, String reportParameters, ReportRunner reportRunner) {
        super(name, reportParameters, reportRunner);
    }

    public BIRTReport(String name, WeakHashMap<String, String> reportParameters, ReportRunner reportRunner) {
    	 super(name, reportParameters, reportRunner);
    }

    public BIRTReport(String name, String format, WeakHashMap<String, String> reportParameters, ReportRunner reportRunner) {
   	 super(name, format, reportParameters, reportRunner);
   }

    

    public BIRTReport(String name, WeakHashMap<String, String> reportParameters, StringBuffer data, ReportRunner reportRunner) {
        super(name, reportParameters, data, reportRunner);
    }


	public BIRTReport(String name, String FileName,WeakHashMap<String, String> reportParameters, StringBuffer  data, ReportRunner reportRunner) {
		 super(name, FileName, reportParameters, data, reportRunner);
	}

    @Override
    public Report runReport() {
        this.reportContent = reportRunner.runReport(this);
        return this;
    }

	@Override
	public Report runReportAndWritePDF() {
		this.fileName = reportRunner.runReportAndWritePDF(this);
        return this;
	}




}