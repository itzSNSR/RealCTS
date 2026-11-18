package com.realcts.app;

final class TriggerStrategyManager {
    enum Mode { Automatic, VIS, CSHelper, CSService, SearchScreen }
    enum Route { Direct, Switch, CSHelper, CSService, SearchScreen, Unavailable }

    static Route choose(Mode mode, boolean google, boolean helper, boolean service,
                        boolean vis, boolean active, boolean switchAllowed, boolean searchAction) {
        if (!google) return Route.Unavailable;
        if ((mode == Mode.Automatic || mode == Mode.CSHelper) && helper) return Route.CSHelper;
        if ((mode == Mode.Automatic || mode == Mode.SearchScreen) && searchAction) return Route.SearchScreen;
        if (mode == Mode.CSService && service) return Route.CSService;
        if (mode != Mode.Automatic && mode != Mode.VIS || !vis) return Route.Unavailable;
        if (active) return Route.Direct;
        return mode == Mode.VIS && switchAllowed ? Route.Switch : Route.Unavailable;
    }

    static Route select(TriggerStrategyManager.Mode mode, Capabilities c, ModuleLink.Status module, boolean allowSwitch) {
        return choose(mode, c.googleInstalled, (c.helper || mode == Mode.CSHelper && module.helperReady()) && android.os.Build.VERSION.SDK_INT <= 36,
                module.serviceReady(), c.visCandidate(), c.googleActive,
                c.canWrite && allowSwitch && c.googleVoice != null, c.googleTarget && SearchActionService.available());
    }
}
