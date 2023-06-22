package ch.ehi.ilishaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.generator.nls.Ili2TranslationXml;
import ch.interlis.ili2c.generator.nls.ModelElements;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox_j.inifile.IniFileReader;
import ch.interlis.iox_j.statistics.Stopwatch;
import ch.interlis.iox_j.validator.ValidationConfig;

public class CreateModel {
    
	public boolean createModel(File destModel,File srcFiles[],
	        Settings settings) {
		boolean ok=true;
		
        if(destModel==null){
            EhiLogger.logError("no destination model file given");
            return !ok;
        }
		if(settings==null) {
		    settings=new Settings();
		}
        // give user important info (such as input files or program version)
        EhiLogger.logState(Main.APP_NAME+"-"+Main.getVersion());
        EhiLogger.logState("ili2c-"+ch.interlis.ili2c.Ili2c.getVersion());
        EhiLogger.logState("iox-ili-"+ch.interlis.iox_j.utility.IoxUtility.getVersion());
        EhiLogger.logState("java.version "+System.getProperty("java.version"));
        EhiLogger.logState("user.name <"+java.lang.System.getProperty("user.name")+">");
        EhiLogger.logState("maxMemory "+java.lang.Runtime.getRuntime().maxMemory()/1024L+" KB");
        String DATE_FORMAT = "yyyy-MM-dd HH:mm";
        SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
        EhiLogger.logState("Start date "+dateFormat.format(new java.util.Date()));
        if(destModel!=null){
            EhiLogger.logState("destModel <"+destModel.getPath()+">");
        }
        String configFilename=settings.getValue(Main.SETTING_CONFIGFILE);
        if(configFilename!=null){
            EhiLogger.logState("configFile <"+configFilename+">");
        }
        
        
        EhiLogger.traceState("read configuration...");
        ValidationConfig config = null;
        try {
            config = IniFileReader.readFile(new File(configFilename));
        } catch (IOException e) {
            EhiLogger.logError("failed to read config file <"+configFilename+">",e);
            return !ok;
        }
                
        EhiLogger.traceState("read source models...");
        ch.interlis.ili2c.config.Configuration ili2cConfig =new ch.interlis.ili2c.config.Configuration();
        ili2cConfig.setAutoCompleteModelList(true);
        for(String entry:config.getIliQnames()) {
            if(!entry.contains(".")) {
                String modelName=entry;
                EhiLogger.logState("srcModel <"+modelName+">");
                ili2cConfig.addFileEntry(new FileEntry(modelName,FileEntryKind.ILIMODELFILE));
            }
        }
        if(ili2cConfig.getSizeFileEntry()==0){
            EhiLogger.logError("no source models given in config file");
            return !ok;
        }
        if(srcFiles!=null) {
            for(File srcFile:srcFiles){
                EhiLogger.logState("srcFile <"+srcFile.getPath()+">");
                ili2cConfig.addFileEntry(new FileEntry(srcFile.getPath(),FileEntryKind.ILIMODELFILE));
            }
        }
        TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
        if(td==null) {
            EhiLogger.logError("failed to read source models");
            return !ok;
        }
        EhiLogger.logState("write destination model...");
        java.io.Writer out=null;
        try {
            out = new java.io.OutputStreamWriter(new FileOutputStream(destModel.getPath()),"UTF-8");
            IliGenerator gen = new IliGenerator();
            gen.generate(out, td, config,settings);
        } catch (Exception e) {
            EhiLogger.logError("failed to write destination model",e);
            return !ok;
        }finally {
            if(out!=null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
		
        EhiLogger.logState("End date " + dateFormat.format(new java.util.Date()));
		return ok;
	}
}
