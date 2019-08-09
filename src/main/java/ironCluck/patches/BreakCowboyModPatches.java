package ironCluck.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;

public class BreakCowboyModPatches {
    @SpirePatch(cls = "src.main.patches.ReplaceIroncladAnimation", method = "Postfix", optional = true)
    public static class FuckingStupid {
        @SpireInsertPatch(rloc=0)
        public static SpireReturn Insert(Ironclad __instance, String playerName) {
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(cls = "src.main.patches.ReplaceCharacterSelectionButton", method = "Postfix", optional = true)
    public static class EndMySuffering {
        @SpireInsertPatch(rloc=0)
        public static SpireReturn Insert(CharacterSelectScreen __instance) {
            return SpireReturn.Return(null);
        }
    }
}
