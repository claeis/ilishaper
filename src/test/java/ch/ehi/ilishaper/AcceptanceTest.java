package ch.ehi.ilishaper;

import static org.junit.Assert.*;

import java.io.File;
import org.interlis2.validator.Validator;
import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.ilishaper.Main;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.iox.IoxException;

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
        boolean ret = new CreateModel().createModel(new File(OUT_FOLDER,"out.ili"),null,settings);
        assertTrue(ret);

    }

    @Test
    public void deriveData() throws IoxException {
        Settings settings = new Settings();
        settings.setValue(Main.SETTING_ILIDIRS, TEST_DATA);
        settings.setValue(Main.SETTING_CONFIGFILE, CONFIG_FILE);
        final File destFile = new File(OUT_FOLDER,"out.xtf");
        boolean ret = new DeriveData().deriveData(destFile,new File[] {new File(TEST_DATA,"Basisdaten.xtf")},settings);
        assertTrue(ret);

        // Validate generated XTF
        //boolean runValidation = Validator.runValidation(new String[] { destFile.getPath() }, settings);
        //assertTrue(runValidation);
    }
    

}
