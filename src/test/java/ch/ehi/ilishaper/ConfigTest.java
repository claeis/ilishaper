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
    private static final String CONFIG_FILE=TEST_DATA+"config.ini";
    private static final String OUT_FOLDER = "build/out";

    @org.junit.BeforeClass
    public static void setup() {
        new File(OUT_FOLDER).mkdir();
    }
    @Test
    public void filter() throws Exception {
        ValidationConfig trafoConfig = null;
        trafoConfig = IniFileReader.readFile(new File(CONFIG_FILE));
        String filter=trafoConfig.getConfigValue("Basismodell.TopicT1.ClassA",IliGenerator.CONFIG_VIEWABLE_FILTER);
        assertEquals("Attr5==#rot",filter);
    }

}
