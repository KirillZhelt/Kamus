package com.kamus.loaderconfig.distributor;

import com.kamus.loaderconfig.distributor.model.Loader;
import com.kamus.loaderconfig.distributor.model.LoaderId;

import java.util.Set;

public interface LoadersChangeAware {

    void onLoadersInit(Set<Loader> loaders);

    void onLoaderAdded(Loader loader);
    void onLoaderRemoved(LoaderId path);

}
