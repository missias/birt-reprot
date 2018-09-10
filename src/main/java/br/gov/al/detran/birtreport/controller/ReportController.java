package br.gov.al.detran.birtreport.controller;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.gov.al.detran.birtreport.model.BIRTReport;
import br.gov.al.detran.birtreport.service.ReportRunner;

@RestController("ReportController")
@RequestMapping("/reports")
@CrossOrigin
public class ReportController {

	private Logger logger = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	@Qualifier("birt")
	private ReportRunner reportRunner;

	@Autowired
	private Environment env;

	@RequestMapping(value = "/birt", method = RequestMethod.POST)
	public ResponseEntity<byte[]> getBIRTReport(
			@RequestParam(name = "report", value = "", required = true) String report, @RequestBody String datasource,
			@RequestParam(name = "file-name", value = "", required = true) String file_name,
			@RequestParam(name = "bancos", value = "", required = false) String bancos,
			@RequestParam(name = "format", value = "", defaultValue = "pdf", required = false) String format) {

		WeakReference<byte[]> reportBytes = null;
		ResponseEntity<byte[]> responseEntity = null;

		try {

			if (logger.isDebugEnabled()) {
				logger.info("REPORT REQUEST NAME:   " + report);
			}

			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("bancos", (((bancos == "") || (bancos == null)) ? env.getProperty("bancos") : bancos));
			paramMap.put("format", (((format == "") || (format == null)) ? "pdf" : format));

			if (logger.isDebugEnabled()) {
				logger.info("REPORT REQUEST PARAMS:   " + paramMap);
			}
			reportBytes = new WeakReference<byte[]>( new WeakReference<BIRTReport>(new BIRTReport(report, paramMap.get("format"), file_name, paramMap, datasource, reportRunner)).get().runReport().getReportContent().toByteArray());

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.parseMediaType("application/pdf"));
			String fileName = (file_name + ".pdf");

			httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + fileName);
			httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(reportBytes.get().length));

			httpHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
			responseEntity = new ResponseEntity<byte[]>(reportBytes.get(), httpHeaders, HttpStatus.OK);

			
			reportBytes = null;
			return responseEntity;

		} catch (Exception e) {
			e.printStackTrace();
			responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_IMPLEMENTED);
			return responseEntity;
		} finally {

		}

	}

	@RequestMapping(value = "/birt", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getBIRTReport(@RequestParam Map<String, String> params) {

		WeakReference<byte[]> reportBytes = null;
		
		ResponseEntity<byte[]> responseEntity = null;

		try {
			
		    if ( !params.containsKey("bancos") ) {
		    	params.put("bancos", env.getProperty("bancos") );
		    }
			if (!params.containsKey("format")) {
				params.put("format", "pdf");
			}

			if (!params.containsKey("file-name")) {
				params.put("file-name", params.get("report"));
			}
			
			params.put("host", env.getProperty("detran.api.host"));
			
			if (logger.isDebugEnabled()) {

				logger.info("PARAMS: " + params);

				logger.info("Host API:   " + env.getProperty("detran.api.host"));
				logger.info("REPORT REQUEST NAME:   " + params.get("report"));
			}

			reportBytes = new WeakReference<byte[]>( new WeakReference<BIRTReport>(new BIRTReport(params.get("report"), params.get("format"), params, reportRunner)).get().runReport().getReportContent().toByteArray());
			
			HttpHeaders httpHeaders = new HttpHeaders();

			String fileName = "";
			if ("pdf".equals(params.get("format"))) {
				httpHeaders.setContentType(MediaType.parseMediaType("application/pdf"));
				fileName = (params.get("file-name") + ".pdf");
			}
			if ("xls".equals(params.get("format"))) {
				httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
				fileName = (params.get("file-name") + ".xls");
			}

			httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, ("inline;filename=" + fileName));
			httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(reportBytes.get().length));
			httpHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
			responseEntity = new ResponseEntity<byte[]>(reportBytes.get(), httpHeaders, HttpStatus.OK);

			params.clear();
			httpHeaders.clear();
			reportBytes = null;
			
			return responseEntity;

		} catch (Exception e) {

			e.printStackTrace();
			responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_IMPLEMENTED);
			return responseEntity;
		}
	}

}
