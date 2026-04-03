package coolaid.villagepillagetweaks.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import coolaid.villagepillagetweaks.fabric.client.ConfigScreenFabric;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenFabric::create;
    }
}