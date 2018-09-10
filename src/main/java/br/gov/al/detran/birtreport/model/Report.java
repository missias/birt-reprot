package br.gov.al.detran.birtreport.model;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import br.gov.al.detran.birtreport.service.ReportRunner;

/**
 * A Report object has a byte representation of the report output that can be
 * used to write to any output stream. This class is designed around the concept
 * of using ByteArrayOutputStreams to write PDFs to an output stream.
 *
 *
 */
public abstract class Report {

    protected String name;
    protected String parameters;
    protected ByteArrayOutputStream reportContent;
    protected ReportRunner reportRunner;

    protected Map<String, String> mapParam;
    protected String data;

    protected String fileName;
    protected String format;

    public Report(String name, String parameters, ReportRunner reportRunner) {
        this.name = name;
        this.parameters = parameters;
        this.reportRunner = reportRunner;
    }

    public Report(String name, Map<String, String> mapParam,  String data, ReportRunner reportRunner) {
        this.name = name;
        this.mapParam = mapParam;
        this.reportRunner = reportRunner;
        this.data = data;
    }

    public Report(String name, String fileName, Map<String, String> mapParam,  String data, ReportRunner reportRunner) {
        this.name = name;
        this.mapParam = mapParam;
        this.reportRunner = reportRunner;
        this.data = data;
        this.fileName = fileName;
    }

    public Report(String name, String format, String fileName, Map<String, String> mapParam,  String data, ReportRunner reportRunner) {
        this.name = name;
        this.mapParam = mapParam;
        this.reportRunner = reportRunner;
        this.data = data;
        this.fileName = fileName;
        this.format = format;
    }

    public Report(String name,Map<String, String> mapParam, ReportRunner reportRunner) {
        this.name = name;
        this.mapParam = mapParam;
        this.reportRunner = reportRunner;
    }

    public Report(String name, String format,Map<String, String> mapParam, ReportRunner reportRunner) {
        this.name = name;
        this.mapParam = mapParam;
        this.reportRunner = reportRunner;
        this.format = format;
    }
    

    /**
     * This is the processing method for a Report. Once the report is ran it
     * populates an internal field with a ByteArrayOutputStream of the
     * report content generated during the run process.
     * @return Returns itself with the report content output stream created.
     */
    public abstract Report runReport();

    public abstract Report runReportAndWritePDF();


    public  ByteArrayOutputStream getReportContent() {
        return this.reportContent;
    }

    public String getName() {
        return name;
    }

    public String getParameters() {
        return parameters;
    }

	public Map<String, String> getMapParam() {
		return mapParam;
	}

	public  String getData() {
		return data;
	}

	public  String setData(String data) {
		return this.data = data;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFormat() {
		return format;
	}




}