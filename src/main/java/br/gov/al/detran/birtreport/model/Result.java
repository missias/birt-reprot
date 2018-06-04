package br.gov.al.detran.birtreport.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Result {

	@XmlElement(required = true)
	private String path;

	@XmlElement(required = false)
	private String reportName;

	@XmlElement(required = false)
	private String reportFileName;

	@XmlElement(required = false)
	private byte[] reportAsStream;

	@XmlElement(name = "retorno", required = true)
	private Map<String, String> retorno;

	public Result() {
		super();
		this.path = "";
		this.retorno = new HashMap<String, String>();
		this.reportAsStream = new byte[]{0,0,0};

	}

	public Map<String, String> getRetorno() {
		return retorno;
	}

	public void setRetorno(Map<String, String> retorno) {
		this.retorno = retorno;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportFileName() {
		return reportFileName;
	}

	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}

	public byte[] getReportAsStream() {
		return reportAsStream;
	}

	public void setReportAsStream(byte[] reportAsStream) {
		this.reportAsStream = reportAsStream;
	}


}
