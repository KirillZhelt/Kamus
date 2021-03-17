package com.kamus.loaderconfig.distributor;

import com.kamus.core.model.Loader;
import com.kamus.core.model.LoaderId;

import java.util.Set;

public interface LoadersChangeAware {

    void onLoadersInit(Set<Loader> loaders);

    void onLoaderAdded(Loader loader);
    void onLoaderRemoved(LoaderId path);

}
