package ch.ehi.ilishaper;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.interlis2.validator.Validator;
import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.ilishaper.Main;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.EndBasketEvent;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox.StartBasketEvent;
import ch.interlis.iox.StartTransferEvent;

public class AcceptanceTest {
    private static final String TEST_DATA="src/test/data/Acceptance/";
    private static final String CONFIG_FILE=TEST_DATA+"config.ini";
    private static final String OUT_FOLDER = "build/out";

    @org.junit.BeforeClass
    public static void setup() {
        new File(OUT_FOLDER).mkdir();
    }
    @Test
    public void createModel() throws IoxException {
        EhiLogger.getInstance().setTraceFilter(false);
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File resultIli = new File(OUT_FOLDER,"AcceptanceTest_createModel.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertTrue(ret);
        ch.interlis.ili2c.config.Configuration ili2cConfig=new ch.interlis.ili2c.config.Configuration();
        ili2cConfig.addFileEntry(new ch.interlis.ili2c.config.FileEntry(resultIli.getPath(), ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
        ili2cConfig.setAutoCompleteModelList(true);
        TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
        assertNotNull(td);
        assertNotNull(td.getElement("Derivatmodell.TopicT1.ClassA"));
        assertNotNull(td.getElement("Derivatmodell.TopicT1.ClassA.Attr2"));
        assertNull(td.getElement("Derivatmodell.TopicT1.ClassA.Attr1")); //ignored Attribute
        assertNull(td.getElement("Derivatmodell.TopicT1.ClassB")); // ignored Class
        assertNull(td.getElement("Derivatmodell.TopicT2")); // ignored Topic
    }

    @Test
    public void deriveData() throws IoxException {
        //EhiLogger.getInstance().setTraceFilter(false);
        final File destFile = new File(OUT_FOLDER,"AcceptanceTest_deriveData.xtf");
        {
            Settings settings = new Settings();
            settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
            settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
            boolean ret = new DeriveData().deriveData(destFile,new File[] {new File(TEST_DATA,"Basisdaten.xtf")},settings);
            assertTrue(ret);
        }

        // Validate generated XTF
        {
            Settings settings = new Settings();
            settings.setValue(Validator.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
            settings.setValue(Validator.SETTING_MODELNAMES, "Derivatmodell");
            boolean runValidation = Validator.runValidation(new String[] { destFile.getPath() }, settings);
            assertTrue(runValidation);
        }
        {
            Settings settings = new Settings();
            settings.setValue(Ili2cSettings.ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
            ch.interlis.ili2c.config.Configuration ili2cConfig=new ch.interlis.ili2c.config.Configuration();
            ili2cConfig.addFileEntry(new ch.interlis.ili2c.config.FileEntry(TEST_DATA+"Derivatmodell.ili", ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
            ili2cConfig.setAutoCompleteModelList(true);
            TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
            assertNotNull(td);
            
            HashMap<String,IomObject> objs=new HashMap<String,IomObject>();
            HashMap<String,StartBasketEvent> baskets=new HashMap<String,StartBasketEvent>();
            XtfReader reader=new XtfReader(destFile);
            reader.setModel(td);
            IoxEvent event=null;
             do{
                event=reader.read();
                if(event instanceof StartTransferEvent){
                }else if(event instanceof StartBasketEvent){
                    if(((StartBasketEvent) event).getBid()!=null) {
                        baskets.put(((StartBasketEvent) event).getBid(), (StartBasketEvent)event);
                    }
                }else if(event instanceof ObjectEvent){
                    IomObject iomObj=((ObjectEvent)event).getIomObject();
                    if(iomObj.getobjectoid()!=null){
                        objs.put(iomObj.getobjectoid(), iomObj);
                    }
                }else if(event instanceof EndBasketEvent){
                }else if(event instanceof EndTransferEvent){
                }
             }while(!(event instanceof EndTransferEvent));
            
             assertEquals(2,baskets.size());
        }
    }
    

}
