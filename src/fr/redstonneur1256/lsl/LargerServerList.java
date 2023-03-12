package fr.redstonneur1256.lsl;

import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

public class LargerServerList extends Mod {

    @Override
    public void init() {
        if(Vars.headless) {
            Log.err("LargerServerList is a client side mod made to modify the server list UI");
            return;
        }

        Vars.ui.settings.addCategory("@lsl.settings", Icon.menu, table -> table.sliderPref("lsl.serversPerRow", 3, 1, 5, String::valueOf));
    }

}
