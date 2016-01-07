package com.github.dongliang.h5appshell.plugins;

import android.webkit.WebView;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by zmx
 * Date: 2016/1/6
 * Description: DO WHAT
 */
public abstract class Plugin<TParameter> {

    public abstract String getFullName();

    public final Class<TParameter> getParameterType() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            return (Class<TParameter>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    public abstract void execute(WebView webView, TParameter parameter);

}
