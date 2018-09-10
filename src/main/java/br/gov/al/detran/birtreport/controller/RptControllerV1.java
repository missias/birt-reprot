package br.gov.al.detran.birtreport.controller;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
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
import br.gov.al.detran.birtreport.model.Result;
import br.gov.al.detran.birtreport.service.ReportRunner;

@RestController("RptControllerV1")
@CrossOrigin
public class RptControllerV1 {

	private Logger logger = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	@Qualifier("birt")
	private ReportRunner reportRunner;

    @Autowired
    private Environment env;


	@RequestMapping(value = "/runreport", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Result> getPDFReport(
			@RequestParam(name = "report", value = "", required = true) String report,
			@RequestBody String datasource,
			@RequestParam(name = "file-name", value = "", required = true) String file_name,
			@RequestParam(name = "bancos", value = "", required = false) String bancos) {
    	
		ResponseEntity<Result> responseEntity;
		WeakReference<Result> r;
		try {
 
			if (logger.isDebugEnabled()) {
			   logger.info( ("REPORT REQUEST NAME:   " + report));
			}
			
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("bancos", (( (bancos == "") || (bancos == null)) ? env.getProperty("bancos") : bancos ));

			String downloadReportFile = new WeakReference<BIRTReport>(new BIRTReport(report, file_name, paramMap, datasource, reportRunner)).get().runReportAndWritePDF().getFileName();

        	r = new WeakReference<Result>(new Result());
			r.get().setReportName(report + ".rptdesign");
			r.get().setReportFileName(downloadReportFile);
			r.get().getRetorno().put("codigo", "200");
			r.get().getRetorno().put("mensagem", "OK");
			r.get().setPath(env.getProperty("birt_temp_pdf_output_dir" + downloadReportFile + ".pdf"));

			responseEntity = new ResponseEntity<Result>(r.get(),  HttpStatus.OK); 
			
			return responseEntity;
			
        } catch (Exception e) {
        	
			responseEntity = new ResponseEntity<Result>(HttpStatus.NOT_IMPLEMENTED);
			return responseEntity;
		}
		

	}

}
