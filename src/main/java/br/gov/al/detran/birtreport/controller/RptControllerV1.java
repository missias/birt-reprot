package br.gov.al.detran.birtreport.controller;

import java.util.WeakHashMap;

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
		Result r;
		try {
 
        	//httpHeaders = new WeakReference<>(new HttpHeaders());

			logger.info( ("REPORT REQUEST NAME:   " + report).intern());
			StringBuffer data = new StringBuffer(datasource.intern());

			WeakHashMap<String, String> paramMap = new WeakHashMap<String, String>();
			paramMap.put("bancos".intern(), (( (bancos == "") || (bancos == null)) ? env.getProperty("bancos").intern() : bancos.intern() ));

			StringBuilder downloadReportFile = new StringBuilder();
			downloadReportFile.append(new BIRTReport(report.intern(), file_name.intern(), paramMap, data, reportRunner).runReportAndWritePDF().getFileName());

			/*httpHeaders.get().setContentType(MediaType.parseMediaType("application/pdf".intern()));
			httpHeaders.get().add("Cache-Control".intern(), "no-cache, no-store, must-revalidate".intern());
			httpHeaders.get().add("Pragma".intern(), "no-cache".intern());
			httpHeaders.get().add("Expires".intern(), "0".intern());
            */
			   
        	r = new Result();
			r.setReportName(report + ".rptdesign");
			r.setReportFileName(downloadReportFile.toString().intern());
			r.getRetorno().put("codigo".intern(), "200".intern());
			r.getRetorno().put("mensagem".intern(), "OK".intern());
			r.setPath(env.getProperty("birt_temp_pdf_output_dir".intern() + downloadReportFile.toString().intern() + ".pdf".intern()));

			responseEntity = new ResponseEntity<Result>(r,  HttpStatus.OK); 

			
			return responseEntity;
			
			
			
        } catch (Exception e) {
        	
        	r = new Result();
        	
        	r.getRetorno().put("codigo".intern(), "500".intern());
			r.getRetorno().put("mensagem".intern(), e.getLocalizedMessage().intern());

			responseEntity = new ResponseEntity<Result>(HttpStatus.NOT_IMPLEMENTED);
			return responseEntity;
		}
		

	}

}
