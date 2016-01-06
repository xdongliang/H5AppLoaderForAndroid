package com.github.dongliang.h5appshell;

import com.github.dongliang.h5appshell.plugins.Plugin;

/**
 * Created by zmx
 * Date: 2016/1/6
 * Description: DO WHAT
 */
public class PluginContainer implements PluginLocator,PluginRegister {
    @Override
    public Plugin locate(String pluginName) {
        return null;
    }

    @Override
    public PluginRegister registe(Plugin plugin) {
        return null;
    }
}
