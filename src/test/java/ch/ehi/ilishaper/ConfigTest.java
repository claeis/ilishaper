package ch.ehi.ilishaper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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
import ch.interlis.iox_j.inifile.IniFileReader;
import ch.interlis.iox_j.validator.ValidationConfig;

public class ConfigTest {
    private static final String TEST_DATA="src/test/data/Acceptance/";
    private static final String OUT_FOLDER = "build/out";

    @org.junit.BeforeClass
    public static void setup() {
        new File(OUT_FOLDER).mkdir();
    }
    @Test
    public void filter() throws Exception {
        final String CONFIG_FILE=TEST_DATA+"config.ini";
        ValidationConfig trafoConfig = null;
        trafoConfig = IniFileReader.readFile(new File(CONFIG_FILE));
        String filter=trafoConfig.getConfigValue("Basismodell.TopicT1.ClassA",IliGenerator.CONFIG_VIEWABLE_FILTER);
        assertEquals("Attr5==#rot",filter);
    }
    @Test
    public void wrongModelName_fail() throws Exception {
        //EhiLogger.getInstance().setTraceFilter(false);
        final String CONFIG_FILE=TEST_DATA+"configWrongModelName_fail.ini";
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File resultIli = new File(OUT_FOLDER,"ConfigTest_wrongModelName_fail.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertFalse(ret);
    }
    @Test
    public void wrongTopicName_fail() throws Exception {
        //EhiLogger.getInstance().setTraceFilter(false);
        final String CONFIG_FILE=TEST_DATA+"configWrongTopicName_fail.ini";
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File resultIli = new File(OUT_FOLDER,"ConfigTest_wrongTopicName_fail.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertFalse(ret);
    }
    @Test
    public void wrongAttrName_fail() throws Exception {
        //EhiLogger.getInstance().setTraceFilter(false);
        final String CONFIG_FILE=TEST_DATA+"configWrongAttrName_fail.ini";
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File resultIli = new File(OUT_FOLDER,"ConfigTest_wrongAttrName_fail.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertFalse(ret);
    }
    @Test
    public void wrongAttrNameInFilter_fail() throws Exception {
        //EhiLogger.getInstance().setTraceFilter(false);
        final String CONFIG_FILE=TEST_DATA+"configWrongAttrNameInFilter_fail.ini";
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA+Ili2cSettings.ILIDIR_SEPARATOR+Ili2cSettings.DEFAULT_ILIDIRS);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File resultIli = new File(OUT_FOLDER,"ConfigTest_wrongAttrNameInFilter_fail.ili");
        boolean ret = new CreateModel().createModel(resultIli,null,settings);
        assertFalse(ret);
    }

}
