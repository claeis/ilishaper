package ch.ehi.ilishaper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.metamodel.TransferDescription;

/** Main program and commandline interface of ilishaper.
 */
public class Main {
	
	/** name of application as shown to user.
	 */
	public static final String APP_NAME="ilishaper";
	/** name of jar file.
	 */
	public static final String APP_JAR="ilishaper.jar";
	/** version of application.
	 */
	private static String version=null;
    private static int FC_NOOP=0;
    private static int FC_CREATE_MODEL=1;
    private static int FC_DERIVE_DATA=2;
	
	/** main program entry.
	 * @param args command line arguments.
	 */
	static public void main(String args[]){
		Settings settings=new Settings();
		settings.setValue(Main.SETTING_ILIDIRS, Ili2cSettings.DEFAULT_ILIDIRS);
		String appHome=getAppHome();
		if(appHome!=null){
		    settings.setValue(Main.SETTING_PLUGINFOLDER, new java.io.File(appHome,"plugins").getAbsolutePath());
		    settings.setValue(Main.SETTING_APPHOME, appHome);
		}else{
		    settings.setValue(Main.SETTING_PLUGINFOLDER, new java.io.File("plugins").getAbsolutePath());
		}
		Class mainFrame=null;
		try {
            //mainFrame=Class.forName("org.interlis2.validator.gui.MainFrame");
            mainFrame=Class.forName(preventOptimziation("ch.ehi.ilishaper.gui.Main")); // avoid, that graalvm native-image detects a reference to MainFrame
		}catch(ClassNotFoundException ex){
		    // ignore; report later
		}
		Method mainFrameMain=null;
		if(mainFrame!=null) {
		    try {
	            mainFrameMain = mainFrame.getMethod ("showDialog");
		    }catch(NoSuchMethodException ex) {
	            // ignore; report later
		    }
		}
		String outFile=null;
        File[] xtfFile=null;
		if(args.length==0){
			readSettings(settings);
            // MainFrame.main(xtfFile,settings);
			runGui(mainFrameMain, settings);     
			return;
		}
		int argi=0;
		int function=FC_NOOP;
		boolean doGui=false;
		for(;argi<args.length;argi++){
			String arg=args[argi];
			if(arg.equals("--trace")){
				EhiLogger.getInstance().setTraceFilter(false);
			}else if(arg.equals("--gui")){
				readSettings(settings);
				doGui=true;
			}else if(arg.equals("--modeldir")){
				argi++;
				settings.setValue(Main.SETTING_ILIDIRS, args[argi]);
            }else if (arg.equals("--createModel")){
                function=FC_CREATE_MODEL;
            }else if (arg.equals("--deriveData")){
                function=FC_DERIVE_DATA;
            }else if (arg.equals("--out")) {
                argi++;
                outFile=args[argi];
            }else if(arg.equals("--config")) {
                argi++;
                settings.setValue(Main.SETTING_CONFIGFILE, args[argi]);
			}else if(arg.equals("--log")) {
			    argi++;
			    settings.setValue(Main.SETTING_LOGFILE, args[argi]);
			}else if(arg.equals("--plugins")) {
			    argi++;
			    settings.setValue(Main.SETTING_PLUGINFOLDER, args[argi]);
			}else if(arg.equals("--proxy")) {
				    argi++;
				    settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, args[argi]);
			}else if(arg.equals("--proxyPort")) {
				    argi++;
				    settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, args[argi]);
			}else if(arg.equals("--version")){
				printVersion();
				return;
			}else if(arg.equals("--help")){
					printVersion ();
					System.err.println();
					printDescription ();
					System.err.println();
					printUsage ();
					System.err.println();
					System.err.println("OPTIONS");
					System.err.println();
					System.err.println("--gui                 start GUI.");
					System.err.println("--createModel         creates a simplified ili-model from a source ili-model");
                    System.err.println("--deriveData         creates a dervied xtf-file from a source xtf-file");
                    System.err.println("--out file            output file");
                    System.err.println("--config file         configuration file");
				    System.err.println("--log file            text file, that receives validation results.");
					System.err.println("--modeldir "+Ili2cSettings.DEFAULT_ILIDIRS+" list of directories/repositories");
				    System.err.println("--plugins folder      directory with jar files that contain user defined functions.");
				    System.err.println("--proxy host          proxy server to access model repositories.");
				    System.err.println("--proxyPort port      proxy port to access model repositories.");
					System.err.println("--trace               enable trace messages.");
					System.err.println("--help                Display this help text.");
					System.err.println("--version             Display the version of "+APP_NAME+".");
					System.err.println();
					return;
				
			}else if(arg.startsWith("-")){
				EhiLogger.logAdaption(arg+": unknown option; ignored");
			}else{
				break;
			}
		}
		{
	        int dataFileCount=args.length-argi;
	        xtfFile=new File[dataFileCount];
	        int fileCount=0;
	        while(argi<args.length){
	            xtfFile[fileCount]=new File(args[argi]);
	            fileCount+=1;
	            argi++;
	        }
		}
		if(doGui){
			//MainFrame.main(xtfFile,settings);
            runGui(mainFrameMain, settings);                     
            return;
		}else{
            boolean ok=false;
            if(function==FC_NOOP) {
                ok=true;
            }else if(function==FC_CREATE_MODEL) {
                CreateModel cloner=new CreateModel();
                ok=cloner.createModel(new File(outFile),xtfFile, settings);
            }else if(function==FC_DERIVE_DATA) {
                DeriveData cloner=new DeriveData();
                ok=cloner.deriveData(new File(outFile),xtfFile, settings);
            }else {
                throw new IllegalStateException("function=="+function);
            }
            System.exit(ok ? 0 : 1);
		}
		
	}
    private static String preventOptimziation(String val) {
        StringBuffer buf=new StringBuffer(val.length());
        buf.append(val);
        return buf.toString();
    }
    private static void runGui(Method mainFrameMain, Settings settings) {
        if(mainFrameMain!=null) {
            try {
                mainFrameMain.invoke(null);
                return;                 
            } catch (IllegalArgumentException ex) {
                EhiLogger.logError("failed to open GUI",ex);
            } catch (IllegalAccessException ex) {
                EhiLogger.logError("failed to open GUI",ex);
            } catch (InvocationTargetException ex) {
                EhiLogger.logError("failed to open GUI",ex);
            }
        }else {
            EhiLogger.logError(APP_NAME+": no GUI available");
        }
        System.exit(2);
    }
	/** Name of file with program settings. Only used by GUI, not used by commandline version.
	 */
	private final static String SETTINGS_FILE = System.getProperty("user.home") + "/.ilishaper";
	/** Reads program settings.
	 * @param settings Program configuration as read from file.
	 */
	public static void readSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			if(file.exists()){
				settings.load(file);
			}
		}catch(java.io.IOException ex){
			EhiLogger.logError("failed to load settings from file "+SETTINGS_FILE,ex);
		}
	}
	/** Writes program settings.
	 * @param settings Program configuration to write.
	 */
	public static void writeSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			settings.store(file,APP_NAME+" settings");
		}catch(java.io.IOException ex){
			EhiLogger.logError("failed to settings settings to file "+SETTINGS_FILE,ex);
		}
	}
	
	/** Prints program version.
	 */
	protected static void printVersion ()
	{
	  System.err.println(APP_NAME+", Version "+getVersion());
	  System.err.println("  Developed by Eisenhut Informatik AG, CH-3400 Burgdorf");
	}

	/** Prints program description.
	 */
	protected static void printDescription ()
	{
	  System.err.println("DESCRIPTION");
	  System.err.println("  creates simplified, derived Interlis data.");
	}

	/** Prints program usage.
	 */
	protected static void printUsage()
	{
	  System.err.println ("USAGE");
	  System.err.println("  java -jar "+APP_JAR+" [Options]");
	}
	/** Gets version of program.
	 * @return version e.g. "1.0.0"
	 */
	public static String getVersion() {
		  if(version==null){
		java.util.ResourceBundle resVersion = java.util.ResourceBundle.getBundle(ch.ehi.basics.i18n.ResourceBundle.class2qpackageName(Main.class)+".Version");
			StringBuffer ret=new StringBuffer(20);
		ret.append(resVersion.getString("version"));
			ret.append('-');
		ret.append(resVersion.getString("versionCommit"));
			version=ret.toString();
		  }
		  return version;
	}
	
	/** Gets main folder of program.
	 * 
	 * @return folder Main folder of program.
	 */
	static public String getAppHome()
	{
	  String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	  for(String classpath:classpaths) {
		  if(classpath.toLowerCase().endsWith(".jar")) {
			  File file = new File(classpath);
			  String jarName=file.getName();
			  if(jarName.toLowerCase().startsWith(APP_NAME)) {
				  file=new File(file.getAbsolutePath());
				  if(file.exists()) {
					  return file.getParent();
				  }
			  }
		  }
	  }
	  return null;
	}

	
    /** Path with folders of Interlis model files. Multiple entries are separated by semicolon (';'). 
     * Might contain "http:" URLs which should contain model repositories. 
     */
    public static final String SETTING_ILIDIRS=Ili2cSettings.ILIDIRS;
    /** the main folder of program.
     */
    public static final String SETTING_APPHOME="ch.ehi.ilishaper.appHome";
    /** Name of the folder that contains jar files with plugins.
     */
    public static final String SETTING_PLUGINFOLDER = "ch.ehi.ilishaper.pluginfolder";
    /** Name of the log file that receives the log messages.
     */
    public static final String SETTING_LOGFILE = "ch.ehi.ilishaper.log";
    /** Name of the ini file that defines the mapping.
     */
    public static final String SETTING_CONFIGFILE = "ch.ehi.ilishaper.config";
}
