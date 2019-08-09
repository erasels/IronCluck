package ironCluck.patches;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.BlueCards;
import com.megacrit.cardcrawl.daily.mods.Chimera;
import com.megacrit.cardcrawl.daily.mods.Diverse;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import ironCluck.IronCluck;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class IronCluckPatches {
    //Load image and avert animation overwrite
    @SpirePatch(clz = Ironclad.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class})
    public static class AddImage {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(Ironclad.class.getName()) && m.getMethodName().equals("initializeClass")) {
                        m.replace("{" +
                                "$proceed("+IronCluckPatches.class.getName()+".getPath(\"combat\"), "+IronCluckPatches.class.getName()+".getPath(\"shoulder2\"), " +IronCluckPatches.class.getName()+".getPath(\"shoulder\"), "+IronCluckPatches.class.getName()+".getPath(\"corpse\"), $5,$6,$7,$8,$9,$10);" +
                                "}");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = Ironclad.class, method = SpirePatch.CONSTRUCTOR)
    public static class SkipAnimationLoading {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn Insert(Ironclad __instance, String pN) {
            if (ModHelper.enabledMods.size() > 0 && (ModHelper.isModEnabled(Diverse.ID) || ModHelper.isModEnabled(Chimera.ID) || ModHelper.isModEnabled(BlueCards.ID))) {
                __instance.masterMaxOrbs = 1;
            }
            return SpireReturn.Return(null);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Ironclad.class, "loadAnimation");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    //Prevent animation crash on getting hit
    @SpirePatch(clz = Ironclad.class, method = "damage")
    public static class SkipIfStatement {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess m) throws CannotCompileException {
                    if (m.getClassName().equals(Ironclad.class.getName()) && m.getFieldName().equals("currentBlock")) {
                        m.replace("{" +
                                "$_ = " + Integer.MAX_VALUE + ";" +
                                "}");
                    }
                }
            };
        }
    }

    /*@SpirePatch(clz = Ironclad.class, method = "damage")
    public static class NewDamageAnimation {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(Ironclad __instance, DamageInfo d) {
            if (d.owner != null && d.type != DamageInfo.DamageType.THORNS && d.output - __instance.currentBlock > 0) {
                __instance.useSlowAttackAnimation();
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "damage");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }*/

    //Change name
    @SpirePatch(clz = Ironclad.class, method = "getTitle")
    public static class TitleChanger {
        public static SpireReturn<?> Prefix(Ironclad __instance, AbstractPlayer.PlayerClass pc) {
            if (Settings.language == Settings.GameLanguage.ENG) {
                return SpireReturn.Return("the Ironcluck");
            } else if(Settings.language == Settings.GameLanguage.ZHS) {
                return SpireReturn.Return("铁甲战鸡");
            }
            return SpireReturn.Continue();
        }
    }

    private static boolean firstRun = true;

    @SpirePatch(clz = CharacterOption.class, method = "renderInfo")
    public static class CharSelectTitleChanger {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderSmartText")) {
                        if (firstRun) {
                            firstRun = false;
                            m.replace("{" +
                                    "if(name.equals("+Ironclad.class.getName()+".NAMES[0]) || name.equals(\"铁甲战士\")) {" +
                                        "if("+Settings.class.getName()+".language == "+Settings.class.getName()+".GameLanguage.ZHS) {" +
                                        "$proceed($1, $2, \"铁甲战鸡\", $4, $5, $6, $7, $8);" +
                                        "} else if("+Settings.class.getName()+".language == "+Settings.class.getName()+".GameLanguage.ENG) {" +
                                        "$proceed($1, $2, \"The Ironcluck\", $4, $5, $6, $7, $8);" +
                                        "}" +
                                    "} else {" +
                                    "$proceed($$);" +
                                    "}" +
                                    "}");
                        }
                    }
                }
            };
        }
    }

    //Change Character select bg and button
    private static boolean firstRun2 = true;

    @SpirePatch(clz = CharacterSelectScreen.class, method = "initialize")
    public static class ChangeBG {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(NewExpr m) throws CannotCompileException {
                    if (m.getClassName().equals(CharacterOption.class.getName()) && firstRun2) {
                        firstRun2 = false;
                        m.replace("{" +
                                //Replace $3 with button image load
                                "$_ = $proceed($1, $2, " + ImageMaster.class.getName() + ".loadImage(" +IronCluckPatches.class.getName()+".getPath(\"ironcladButton\"))," + ImageMaster.class.getName() + ".loadImage(" +IronCluckPatches.class.getName()+".getPath(\"ironcladPortrait\")));" +
                                "}");
                    }
                }
            };
        }
    }

    //Custom Death text
    @SpirePatch(clz = DeathScreen.class, method = "getDeathText")
    public static class DeathTextChange {
        public static SpireReturn<String> Prefix(DeathScreen __instance) {
            if (AbstractDungeon.player.chosenClass == AbstractPlayer.PlayerClass.IRONCLAD) {
                if(Settings.language == Settings.GameLanguage.ENG) {
                    return SpireReturn.Return("Chickened out...");
                } else if(Settings.language == Settings.GameLanguage.ZHS){
                    return SpireReturn.Return("鸡你太美……");
                }
            }
            return SpireReturn.Continue();
        }
    }

    //Heart Beat text
    @SpirePatch(clz = Ironclad.class, method = "getSpireHeartText")
    public static class HeartTextChange {
        public static SpireReturn<String> Prefix(Ironclad __instance) {
            if (AbstractDungeon.player.chosenClass == AbstractPlayer.PlayerClass.IRONCLAD) {
                if(Settings.language == Settings.GameLanguage.ENG) {
                    return SpireReturn.Return("NL Winner winner, chicken dinner...");
                } else if(Settings.language == Settings.GameLanguage.ZHS){
                    return SpireReturn.Return("NL  大吉大利，晚上吃鸡……");
                }
            }
            return SpireReturn.Continue();
        }
    }

    //Chicken sound
    @SpirePatch(clz = Ironclad.class, method = "doCharSelectScreenSelectEffect")
    public static class SoundChanger {
        public static SpireReturn Prefix(Ironclad __instance) {
            CardCrawlGame.sound.playA("IRONCLUCK", MathUtils.random(-0.2F, 0.2F));
            CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.MED, ScreenShake.ShakeDur.SHORT, true);
            return SpireReturn.Return(null);
        }
    }

    public static String getPath(String append) {
        String tmp = IronCluck.getModID() + "Resources/img/";
        if(Loader.isModLoaded("cowboy-ironclad")) {
            tmp += "cowboy/";
        }
        return tmp + append + ".png";
    }
}
