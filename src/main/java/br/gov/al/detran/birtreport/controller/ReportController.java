package br.gov.al.detran.birtreport.controller;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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

	private Logger logger =  LoggerFactory.getLogger(ReportController.class);

	@Autowired
	@Qualifier("birt")
	private ReportRunner reportRunner;

    @Autowired
    private Environment env;

    @RequestMapping(value = "/birt", method = RequestMethod.POST)
	public ResponseEntity<byte[]> getBIRTReport(
			@RequestParam(name = "report", value = "", required = true) String report, @RequestBody String datasource,
			@RequestParam(name = "file-name", value = "", required = true) String file_name,
			@RequestParam(name = "bancos", value = "", required = false) String bancos) {

		byte[] reportBytes ;  // = new WeakReference<>(new StringBuilder()); 
		
		ResponseEntity<byte[]> responseEntity;
        try {

			logger.info("REPORT REQUEST NAME:   " + report.intern());
			StringBuffer  data = new StringBuffer();

			WeakHashMap<String, String> paramMap = new WeakHashMap<String, String>();
			paramMap.put("bancos", (( (bancos == "") || (bancos == null)) ? env.getProperty("bancos") : bancos ));

			reportBytes = new BIRTReport(report, paramMap, data, reportRunner).runReport().getReportContent().toByteArray();

			WeakReference<HttpHeaders> httpHeaders = new WeakReference<>(new HttpHeaders());
			httpHeaders.get().setContentType(MediaType.parseMediaType("application/pdf".intern()));
			String fileName = (file_name + ".pdf").intern();

			httpHeaders.get().add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + fileName);
			httpHeaders.get().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(reportBytes.length).intern());


			httpHeaders.get().setCacheControl("must-revalidate, post-check=0, pre-check=0");
			responseEntity = new ResponseEntity<byte[]>(reportBytes, httpHeaders.get(), HttpStatus.OK);

			return responseEntity;

        } catch (Exception e) {
        	e.printStackTrace();
			responseEntity =  new ResponseEntity<byte[]>(HttpStatus.NOT_IMPLEMENTED);
			return responseEntity;
        } finally {
     
		}

	}


	@RequestMapping(value = "/birt", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getBIRTReport(@RequestParam MultiValueMap<String,String> params) {
		
		byte[] reportBytes = null ;

		ResponseEntity<byte[]> responseEntity;
		
		 try {
                WeakHashMap<String, String> paramMap = new WeakHashMap<String, String>();

			    for (Entry<String, List<String>> entry : params.entrySet()) {
			    	if ("report".equals( entry.getKey())) {
			    		paramMap.put("report", ((List<String>)entry.getValue()).get(0));
			    	}else if ("bancos".equals( entry.getKey())) {
		    		   paramMap.put("bancos", (( (  ((List<String>)entry.getValue()).get(0).equals("") || ((List<String>)entry.getValue()).get(0) == null ) ? env.getProperty("bancos") : ((List<String>)entry.getValue()).get(0) )));
		    		} else {
	    		      paramMap.put(entry.getKey(), ((List<String>)entry.getValue()).get(0));
		    		}
			    	
			    	if ("format".equals( entry.getKey())) {
			    		 paramMap.put("format", ("".equals(entry.getValue()) ? "pdf" : ((List<String>)entry.getValue()).get(0) ));
			    	}
			    	
			    	if ("file-name".equals( entry.getKey())) {
			    		 paramMap.put("file-name", ("".equals(entry.getValue()) ? "document" : ((List<String>)entry.getValue()).get(0) ));
			    	}
			    	
		    	}

			   if (!paramMap.containsKey("format")) {
				   paramMap.put("format", "pdf" );
			   }
			   
			   if (!paramMap.containsKey("file-name")) {
				   paramMap.put("file-name", params.getFirst("report") );
			   }
			   
			    
			    paramMap.put("host".intern(), env.getProperty("detran.api.host"));

			    logger.info("PARAMS: ".intern() + paramMap);

				logger.info("Host API:   " + env.getProperty("detran.api.host"));
				logger.info("REPORT REQUEST NAME:   " + paramMap.get("report"));

				reportBytes =  new  BIRTReport( paramMap.get("report"),  paramMap.get("format") , paramMap, reportRunner).runReport().getReportContent().toByteArray();

				HttpHeaders httpHeaders = new HttpHeaders();
				
				String fileName = "";
				if ("pdf".equals( paramMap.get("format"))) {
					httpHeaders.setContentType(MediaType.parseMediaType("application/pdf".intern()));
					fileName = (paramMap.get("file-name").intern() + ".pdf").intern();
				}
				if ("xls".equals( paramMap.get("format"))) {
					httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.ms-excel".intern()));
					fileName = (paramMap.get("file-name").intern() + ".xls").intern();
				}
				
				

				httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION.intern(), ("inline;filename=" + fileName).intern());
				httpHeaders.add(HttpHeaders.CONTENT_LENGTH.intern(), String.valueOf(reportBytes.length).intern());

				httpHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0".intern());
				responseEntity = new ResponseEntity<byte[]>(reportBytes, httpHeaders, HttpStatus.OK);

				paramMap.clear();
				httpHeaders.clear();
				
				return responseEntity;

	        } catch (Exception e) {
	        	 e.printStackTrace();
				responseEntity = new ResponseEntity<byte[]>(HttpStatus.NOT_IMPLEMENTED);
				return responseEntity;
			}
	}





}
