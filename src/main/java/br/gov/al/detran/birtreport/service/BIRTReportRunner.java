package br.gov.al.detran.birtreport.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLImageHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IExcelRenderOption;
import org.eclipse.birt.report.engine.api.IHTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.core.internal.registry.RegistryProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.gov.al.detran.birtreport.model.Report;

@Service
@Qualifier("birt")
@PropertySource("classpath:config.properties")
public class BIRTReportRunner implements ReportRunner {
    private static final String DEFAULT_LOGGING_DIRECTORY = "defaultBirtLoggingDirectory/";
    private Logger logger = LoggerFactory.getLogger(BIRTReportRunner.class);

    private static String reportOutputDirectory;
    private static String reportPDFOutputDirectory;

    private IReportEngine birtReportEngine = null;

    @Autowired
    private Environment env;

    /**
     * Starts up and configures the BIRT Report Engine
     */
    @PostConstruct
    public void startUp() {
        if(env.getProperty("birt_report_input_dir") == null)
            throw new RuntimeException("Cannot start application since birt report input directory was not specified.");
        try {
            String birtLoggingDirectory = env.getProperty("birt_logging_directory") == null ? DEFAULT_LOGGING_DIRECTORY : env.getProperty("birt_logging_directory");

            Level birtLoggingLevel = env.getProperty("birt_logging_level") == null ? Level.SEVERE : Level.parse(env.getProperty("birt_logging_level"));
            EngineConfig engineConfig = new EngineConfig();
            logger.info("BIRT LOG DIRECTORY SET TO : {}".intern(), birtLoggingDirectory.intern());
            logger.info("BIRT LOGGING LEVEL SET TO {}".intern(), birtLoggingLevel);
            engineConfig.setLogConfig(birtLoggingDirectory.intern(), birtLoggingLevel); /**/

            
            logger.info("***** BIRT STARTUP *****".intern());
            
            // Required due to a bug in BIRT that occurs in calling Startup after the Platform has already been started up
            RegistryProviderFactory.releaseDefault();
            Platform.startup(engineConfig);
            IReportEngineFactory reportEngineFactory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            birtReportEngine = reportEngineFactory.createReportEngine(engineConfig);
        } catch (BirtException e) {
            // TODO add logging aspect and find out how to log a platform startup problem from this catch block, if possible, using the aspect.
            // Possibly rethrow the exception here and catch it in the aspect.
            logger.error("Birt Startup Error: {}".intern(), e.getMessage().intern());
        }

        reportOutputDirectory = env.getProperty("birt_temp_file_output_dir".intern());
        reportPDFOutputDirectory = env.getProperty("birt_temp_pdf_output_dir".intern());

    }

    /**
     * Shuts down the BIRT Report Engine
     */
    @PreDestroy
    public void shutdown() {
        birtReportEngine.destroy();
        RegistryProviderFactory.releaseDefault();
        Platform.shutdown();
    }

    public File getReportFromFilesystem(String reportName) throws RuntimeException {
        String reportDirectory = env.getProperty("birt_report_input_dir".intern());


        Path birtReport = Paths.get(reportDirectory.intern() + File.separator + reportName.intern() + ".rptdesign".intern());
        if(!Files.isReadable(birtReport))
            throw new RuntimeException("Report ".intern() + birtReport + " either did not exist or was not writable.".intern());

        return birtReport.toFile();
    }

     /*
      * check if exist rptDocument file
      */
    public boolean isRptDocumentExist(String rptdocument) throws RuntimeException {

       /* String rptdocument = reportOutputDirectory.intern() + File.separator
                + "generated".intern() + File.separator
                + reportName.intern() + ".rptdocument".intern();*/

        Path birtReport = Paths.get(rptdocument);

        return Files.isReadable(birtReport);
    }

    /**
     * This method creates and executes the report task, the main responsibility
     * of the entire Report Service.
     * This method is key to enabling pagination for the BIRT report. The IRunTask run task
     * is created and then used to generate an ".rptdocument" binary file.
     * This binary file is then read by the separately created IRenderTask render
     * task. The render task renders the binary document as a binary PDF output
     * stream which is then returned from the method.
     * <p>
     *
     * @param birtReport the report object created at the controller to hold the data of the report request.
     * @return Returns a ByteArrayOutputStream of the PDF bytes generated by the
     **/



    public ByteArrayOutputStream runReport(Report birtReport) {

    	logger.info("Run rptdesign: {}.", birtReport.getName());

    	ByteArrayOutputStream byteArrayOutputStream;  // = new WeakReference<>(new StringBuilder()); 
    	
        File rptDesignFile;

        // get the path to the report design file
        try {
            rptDesignFile = getReportFromFilesystem(birtReport.getName().intern());
        } catch (Exception e) {
            logger.error("Error while loading rptdesign: {}.".intern(), e.getMessage().intern());
            throw new RuntimeException("Could not find report".intern());
        }

       
        try {

        	IReportRunnable reportDesign = birtReportEngine.openReportDesign(rptDesignFile.getPath());
        	IRunTask runTask =  birtReportEngine.createRunTask(reportDesign);

              if (birtReport.getMapParam().size() > 0) {
                  for (Map.Entry<String, String> entry :birtReport.getMapParam().entrySet()) {
                      runTask.setParameterValue(entry.getKey(), entry.getValue());
                  }
              }

        	
              runTask.validateParameters();
              

          	   //***************** DATASET
                if (birtReport.getData() !=null) {
                   HashMap<String, Object> contextMap = new HashMap<String, Object>();
				   contextMap.put("org.eclipse.birt.report.data.oda.xml.inputStream",
				   new ByteArrayInputStream(birtReport.getData().toString().getBytes()));
				   contextMap.put("org.eclipse.birt.report.data.oda.xml.closeInputStream",	Boolean.TRUE);
				   runTask.setAppContext(contextMap);
                }
				//***********************

              String rptdocument = reportOutputDirectory + File.separator
                      + "generated" + File.separator
                      + birtReport.getName() + ".rptdocument";

              
              if (!isRptDocumentExist(rptdocument)) {
                  runTask.run(rptdocument);
              }
              
              

              IReportDocument reportDocument =   birtReportEngine.openReportDocument(rptdocument);
              IRenderTask renderTask = birtReportEngine.createRenderTask(reportDocument);
              
              
              
              
              byteArrayOutputStream = new ByteArrayOutputStream();
              
              PDFRenderOption pdfRenderOption = null;
              EXCELRenderOption xlsRenderOption = null;
              HTMLRenderOption htmlRenderOption = null;
              
              if ("pdf".equals(birtReport.getFormat())) {
            	  pdfRenderOption = new PDFRenderOption();
            	  pdfRenderOption.setOption(IPDFRenderOption.REPAGINATE_FOR_PDF, new Boolean(true));
            	  pdfRenderOption.setOutputFormat(  birtReport.getFormat()  );
            	  pdfRenderOption.setOutputStream(byteArrayOutputStream);
            	  pdfRenderOption.closeOutputStreamOnExit(true);
            	  renderTask.setRenderOption(pdfRenderOption);

            	  
              }
              if ("xls".equals(birtReport.getFormat())) {
            	  xlsRenderOption = new EXCELRenderOption();
            	  xlsRenderOption.setOption( IExcelRenderOption.OFFICE_VERSION, new HTMLImageHandler());
            	  xlsRenderOption.setOutputFormat(  birtReport.getFormat()  );
            	  xlsRenderOption.setOutputStream(byteArrayOutputStream);
            	  xlsRenderOption.closeOutputStreamOnExit(true);

            	  renderTask.setRenderOption(xlsRenderOption);
              }

              if ("html".equals(birtReport.getFormat())) {
            	  htmlRenderOption = new HTMLRenderOption();
            	  htmlRenderOption.setOption(IHTMLRenderOption.OUTPUT_FORMAT_PDF, new Boolean(true));
            	  htmlRenderOption.setOutputFormat(  birtReport.getFormat()  );
            	  htmlRenderOption.setOutputStream(byteArrayOutputStream);
            	  htmlRenderOption.closeOutputStreamOnExit(true);
            	  renderTask.setRenderOption(htmlRenderOption);
              }
              
              renderTask.render();
              renderTask.close();

        } catch (EngineException e) {
            logger.error("Error while running report task: {}.".intern(), e.getMessage().intern());
            throw new RuntimeException();
        } finally {
            //birtReport.getData().clear();
           // birtReport.getMapParam().clear();
		}

        return byteArrayOutputStream;

    }

    /**
     * This method creates and executes the report task, the main responsibility
     * of the entire Report Service.
     * This method is key to enabling pagination for the BIRT report. The IRunTask run task
     * is created and then used to generate an ".rptdocument" binary file.
     * This binary file is then read by the separately created IRenderTask render
     * task. The render task renders the binary document as a binary PDF output
     * stream which is then returned from the method.
     * <p>
     *
     * @param birtReport the report object created at the controller to hold the data of the report request.
     * @return Returns a file name of the PDF generated
     **/
    
    
	@Override
	public String runReportAndWritePDF(Report report) {

		logger.info("Run rptdesign: {}.".intern(), report.getName().intern());

        File rptDesignFile;

        // get the path to the report design file
        try {
            rptDesignFile = getReportFromFilesystem(report.getName().intern());
        } catch (Exception e) {
            logger.error("Error while loading rptdesign: {}.".intern(), e.getMessage().intern());
            throw new RuntimeException("Could not find report".intern());
        }

        try {

        	IReportRunnable reportDesign =  birtReportEngine.openReportDesign(rptDesignFile.getPath());
        	IRunTask runTask = birtReportEngine.createRunTask(reportDesign);

              if (report.getMapParam().size() > 0) {
                  for (Map.Entry<String, String> entry :report.getMapParam().entrySet()) {
                      runTask.setParameterValue(entry.getKey(), entry.getValue());
                  }
              }

              runTask.validateParameters();

              
          	ByteArrayInputStream dataSource =  new ByteArrayInputStream(report.getData().toString().getBytes());
              
          	   //***************** DATASET
                if (report.getData() !=null) {
                	WeakHashMap<String, Object> contextMap = new WeakHashMap<String, Object>();
				   contextMap.put("org.eclipse.birt.report.data.oda.xml.inputStream".intern(), dataSource);
				   contextMap.put("org.eclipse.birt.report.data.oda.xml.closeInputStream".intern(),	Boolean.TRUE);
				   runTask.setAppContext(contextMap);
                }
				//***********************

              String rptdocument = reportOutputDirectory.intern() + File.separator.intern()
                      + "generated".intern() + File.separator.intern()
                      + report.getName().intern() + ".rptdocument".intern();

              runTask.run(rptdocument.intern());

              SoftReference<IReportDocument> reportDocument = new SoftReference<>( birtReportEngine.openReportDocument(rptdocument.intern()) );
              SoftReference<IRenderTask> renderTask = new SoftReference<>( birtReportEngine.createRenderTask(reportDocument.get()) );

              WeakReference<PDFRenderOption> pdfRenderOption = new WeakReference<>( new PDFRenderOption());
              pdfRenderOption.get().setOption(IPDFRenderOption.REPAGINATE_FOR_PDF, new Boolean(true));
              pdfRenderOption.get().setOutputFormat("pdf".intern());
              pdfRenderOption.get().setOutputFileName(reportPDFOutputDirectory.intern() + report.getFileName().intern() + ".pdf".intern());
              pdfRenderOption.get().closeOutputStreamOnExit(true);
              renderTask.get().setRenderOption(pdfRenderOption.get());
              renderTask.get().render();
              renderTask.get().close();

              
              reportDocument.clear();
              pdfRenderOption.clear();
              renderTask.clear();
              

        } catch (EngineException e) {
            logger.error("Error while running report task: {}.".intern(), e.getMessage().intern());
            throw new RuntimeException();
		}

		return report.getFileName().intern();
	}


    






}