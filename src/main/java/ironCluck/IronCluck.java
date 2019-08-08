package ironCluck;

import basemod.BaseMod;
import basemod.interfaces.AddAudioSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpireInitializer
public class IronCluck implements
        PostInitializeSubscriber,
        AddAudioSubscriber {

    private static String modID;
    public static final Logger runLogger = LogManager.getLogger(IronCluck.class.getName());

    public static void initialize() {
        BaseMod.subscribe(new IronCluck());
        setModID("ironCluck");
    }

    @Override
    public void receivePostInitialize() {
        runLogger.info("The Ironcluck has graced the Spire with its presence.");
    }

    public static String getModID() {
        return modID;
    }

    public static void setModID(String id) {
        modID = id;
    }

    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }

    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio("IRONCLUCK", getModID() + "Resources/audio/cluck.ogg");
    }
}
