package com.kamus.loaderconfig.distributor;

import com.kamus.loaderconfig.distributor.model.Loader;

import java.util.Set;

public interface LoadersChangeAware {

    void onLoadersInit(Set<Loader> loaders);

    void onLoaderAdded(Loader loader);
    void onLoaderRemoved(String path);

}
