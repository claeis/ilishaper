package ch.ehi.ilishaper;

import static org.junit.Assert.*;

import java.io.File;
import org.interlis2.validator.Validator;
import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.ilishaper.Main;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxException;

public class TopicTest {
    private static final String TEST_DATA="src/test/data/Topic/";
    private static final String CONFIG_FILE_EXTENDS=TEST_DATA+"configExtends.ini";
    private static final String CONFIG_FILE_DEPENDS=TEST_DATA+"configDepends.ini";
    private static final String OUT_FOLDER = "build/out";

    @org.junit.BeforeClass
    public static void setup() {
        new File(OUT_FOLDER).mkdir();
    }
    @Test
    public void createModel_Extends() throws IoxException {
        EhiLogger.getInstance().setTraceFilter(false);
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE_EXTENDS);
        final File resultIli = new File(OUT_FOLDER,"out.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertTrue(ret);
        ch.interlis.ili2c.config.Configuration ili2cConfig=new ch.interlis.ili2c.config.Configuration();
        ili2cConfig.addFileEntry(new ch.interlis.ili2c.config.FileEntry(resultIli.getPath(), ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
        TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
        assertNotNull(td);
        assertNotNull(td.getElement("Derivatmodell.TopicT1"));
        assertNull(td.getElement("Derivatmodell.TopicT2"));
        assertNull(td.getElement("Derivatmodell.TopicT3"));
    }
    @Test
    public void createModel_Depends() throws IoxException {
        EhiLogger.getInstance().setTraceFilter(false);
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE_DEPENDS);
        final File resultIli = new File(OUT_FOLDER,"out.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertTrue(ret);
        ch.interlis.ili2c.config.Configuration ili2cConfig=new ch.interlis.ili2c.config.Configuration();
        ili2cConfig.addFileEntry(new ch.interlis.ili2c.config.FileEntry(resultIli.getPath(), ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
        TransferDescription td=ch.interlis.ili2c.Main.runCompiler(ili2cConfig, settings);
        assertNotNull(td);
        assertNotNull(td.getElement("Derivatmodell.TopicT1"));
        assertNull(td.getElement("Derivatmodell.TopicT2"));
        assertNotNull(td.getElement("Derivatmodell.TopicT3"));
    }
}
