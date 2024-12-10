package ch.ehi.ilishaper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.ErrorTracker;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.PredefinedModel;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.xtf.XtfModel;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iom_j.xtf.XtfWriterBase;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxLogging;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox.IoxWriter;
import ch.interlis.iox.StartTransferEvent;
import ch.interlis.iox_j.IoxIliReader;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.inifile.IniFileReader;
import ch.interlis.iox_j.logging.FileLogger;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.statistics.IoxStatistics;
import ch.interlis.iox_j.utility.IoxUtility;
import ch.interlis.iox_j.utility.ReaderFactory;
import ch.interlis.iox_j.validator.ValidationConfig;

public class DeriveData {
    
    public static final String MSG_CONVERSION_DONE = "...conversion done";
    public static final String MSG_CONVERSION_FAILED = "...conversion failed";
    
	public boolean deriveData(File destFile,File srcFiles[],
	        Settings settings) {
		boolean ret=false; // fail by default
		if(srcFiles==null  || srcFiles.length==0){
            EhiLogger.logError("no INTERLIS file given");
            return ret;
        }
        if(settings==null){
            settings=new Settings();
        }
        FileLogger logfile=null;
        ErrorTracker logStderr=null;
        String logFilename=settings.getValue(Main.SETTING_LOGFILE);
        try {
            if(logFilename!=null){
                File f=new java.io.File(logFilename);
                try {
                    if(isWriteable(f)) {
                        logfile=new FileLogger(f);
                        EhiLogger.getInstance().addListener(logfile);
                    }else {
                        EhiLogger.logError("failed to write to logfile <"+f.getPath()+">");
                        return ret;
                    }
                } catch (IOException e) {
                    EhiLogger.logError("failed to write to logfile <"+f.getPath()+">",e);
                    return ret;
                }
            }
            logStderr=new ErrorTracker();
            EhiLogger.getInstance().addListener(logStderr);
            
            EhiLogger.logState(Main.APP_NAME+"-"+Main.getVersion());
            EhiLogger.logState("ili2c-"+ch.interlis.ili2c.Ili2c.getVersion());
            EhiLogger.logState("iox-ili-"+ch.interlis.iox_j.utility.IoxUtility.getVersion());
            EhiLogger.logState("java.version "+System.getProperty("java.version"));
            EhiLogger.logState("User <"+java.lang.System.getProperty("user.name")+">");
            String DATE_FORMAT = "yyyy-MM-dd HH:mm";
            SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
            EhiLogger.logState("Start date "+dateFormat.format(new java.util.Date()));
            EhiLogger.logState("maxMemory "+java.lang.Runtime.getRuntime().maxMemory()/1024L+" KB");
            for(File dataFile:srcFiles){
                EhiLogger.logState("dataFile <"+dataFile.getPath()+">");
            }
            // find out, which ili model is required
            List<String> modelnames=new ArrayList<String>();
            for(File dataFile:srcFiles){
                List<String> modelnameFromFile=ch.interlis.iox_j.IoxUtility.getModels(dataFile);
                if(modelnameFromFile==null){
                    return ret;
                }
                modelnames.addAll(modelnameFromFile);
            }
            IoxLogging errHandler=new ch.interlis.iox_j.logging.Log2EhiLogger();
            LogEventFactory errFactory=new LogEventFactory();
            errFactory.setLogger(errHandler);
            String modelVersion=null;
            try {
                String fileNames[]=new String[srcFiles.length];
                int i=0;
                for(File srcFile:srcFiles) {
                    fileNames[i++]=srcFile.getPath();
                }
                modelVersion = IoxUtility.getModelVersion(fileNames, errFactory,settings);
            }catch(IoxException ex) {
                EhiLogger.logAdaption("failed to get version from data file; "+ex.toString()+"; ignored");
            }

            String configFilename=settings.getValue(Main.SETTING_CONFIGFILE);
            if(configFilename!=null){
                EhiLogger.logState("configFile <"+configFilename+">");
            }
            
            
            EhiLogger.traceState("read configuration...");
            ValidationConfig trafoConfig = null;
            try {
                trafoConfig = IniFileReader.readFile(new File(configFilename));
            } catch (IOException e) {
                EhiLogger.logError("failed to read config file <"+configFilename+">",e);
                return ret;
            }
                    
            EhiLogger.traceState("read source models...");
            ch.interlis.ili2c.config.Configuration ili2cConfig =new ch.interlis.ili2c.config.Configuration();
            ili2cConfig.setAutoCompleteModelList(true);
            for(String entry:trafoConfig.getIliQnames()) {
                if(!entry.contains(".")) {
                    String modelName=entry;
                    EhiLogger.logState("srcModel <"+modelName+">");
                    ili2cConfig.addFileEntry(new FileEntry(modelName,FileEntryKind.ILIMODELFILE));
                    String destModel=trafoConfig.getConfigValue(modelName, IliGenerator.CONFIG_MODEL_NAME);
                    if(destModel!=null) {
                        ili2cConfig.addFileEntry(new FileEntry(destModel,FileEntryKind.ILIMODELFILE));
                    }
                    String filterModels=trafoConfig.getConfigValue(modelName, IliGenerator.CONFIG_MODEL_FILTER_MODELS);
                    if(filterModels!=null) {
                        String filterModelv[]=filterModels.split(Ili2cSettings.MODELS_SEPARATOR);
                        for(String filterModel:filterModelv) {
                            if(filterModel!=null) {
                                ili2cConfig.addFileEntry(new FileEntry(filterModel,FileEntryKind.ILIMODELFILE));
                            }
                        }
                    }
                }
            }
            if(ili2cConfig.getSizeFileEntry()==0){
                EhiLogger.logError("no source models given in config file");
                return ret;
            }
            TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
            if(td==null) {
                EhiLogger.logError("failed to read source models");
                return ret;
            }
            
            
            IoxStatistics readerStat=new IoxStatistics(td,settings);
            IoxStatistics writerStat=new IoxStatistics(td,settings);
            PipelinePool pool=new PipelinePool();
            XtfShaper shaper=new XtfShaper(td,trafoConfig,settings);
            IoxWriter ioxWriter=null;
            try{
                ioxWriter=new XtfWriter(destFile,td);
                ((XtfWriter) ioxWriter).setModels(shaper.buildModelList(td));
                
                writerStat.setFilename(destFile.getPath());
                {
                    ch.interlis.iox_j.StartTransferEvent startEvent=new ch.interlis.iox_j.StartTransferEvent();
                    startEvent.setSender(Main.getVersion());
                    ioxWriter.write(startEvent);
                    writerStat.add(startEvent);
                }
                
                // loop over data objects
                for(File srcFile:srcFiles){
                    IoxReader ioxReader=null;
                    try{
                        // setup data reader (ITF or XTF)
                        ioxReader=new ReaderFactory().createReader(srcFile, errFactory,settings);
                        
                        if(ioxReader instanceof IoxIliReader){
                            ((IoxIliReader) ioxReader).setModel(td);    
                        }
                        String filename=srcFile.getPath();
                        readerStat.setFilename(filename);
                        errFactory.setDataSource(filename);
                        IoxEvent event=null;
                        do{
                            event=ioxReader.read();
                            // feed object by object to shaper
                            shaper.addInput(event);
                            readerStat.add(event);
                            IoxEvent outEvent=shaper.getMappedObject();
                            if(outEvent!=null) {
                                ioxWriter.write(outEvent);
                                writerStat.add(outEvent);
                            }
                        }while(!(event instanceof EndTransferEvent));
                        IoxEvent outEvent=shaper.getMappedObject();
                        while(outEvent!=null) {
                            ioxWriter.write(outEvent);
                            writerStat.add(outEvent);
                            outEvent=shaper.getMappedObject();
                        }
                    }finally{
                        if(ioxReader!=null){
                            try {
                                ioxReader.close();
                            } catch (IoxException e) {
                                EhiLogger.logError(e);
                            }
                            ioxReader=null;
                        }
                    }
                }
                IoxEvent outEvent=shaper.getMappedObject();
                while(outEvent!=null) {
                    ioxWriter.write(outEvent);
                    writerStat.add(outEvent);
                    outEvent=shaper.getMappedObject();
                }
                {
                    EndTransferEvent endEvent=new ch.interlis.iox_j.EndTransferEvent();
                    ioxWriter.write(endEvent);
                    writerStat.add(endEvent);
                }
                readerStat.write2logger();
                writerStat.write2logger();
                // check for errors
                if(logStderr.hasSeenErrors()){
                    EhiLogger.logState(MSG_CONVERSION_FAILED);
                }else{
                    EhiLogger.logState(MSG_CONVERSION_DONE);
                    ret=true;
                }
            }catch(Throwable ex){
                if(readerStat!=null) {
                    readerStat.write2logger();
                }
                EhiLogger.logError(ex);
                EhiLogger.logState(MSG_CONVERSION_FAILED);
            }finally {
                if(shaper!=null){
                    shaper.close();
                    shaper=null;
                }
                if(ioxWriter!=null) {
                    ioxWriter.flush();
                    ioxWriter.close();
                    ioxWriter=null;
                }
                EhiLogger.logState("End date " + dateFormat.format(new java.util.Date()));
            }
        } catch (IoxException e) {
            EhiLogger.logError(e);
        }finally{
            if(logfile!=null){
                logfile.close();
                EhiLogger.getInstance().removeListener(logfile);
                logfile=null;
            }
            if(logStderr!=null){
                EhiLogger.getInstance().removeListener(logStderr);
                logStderr=null;
            }
        }
		
		return ret;
	}
    public static boolean isWriteable(File f) throws IOException {
        f.createNewFile();
        return f.canWrite();
    }
}
