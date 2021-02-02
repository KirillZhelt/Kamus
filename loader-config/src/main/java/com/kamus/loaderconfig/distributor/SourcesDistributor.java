package com.kamus.loaderconfig.distributor;

import com.kamus.loaderconfig.distributor.model.Sources;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SourcesDistributor {

    private static final int BUCKETS_COUNT = 256;

    // TODO: implement SourcesDistributor
    //
    // sources should be 'stored' in buckets, and buckets should be distributed between loaders
    //
    // it should rebalance the distribution between the active loaders
    // rebalancing should happen when the number of active loaders has changed
    // newly joined loaders are waiting for the config
    //
    // *if the weight of the source is changed significantly then rebalance may also occur

    public Map<String, Sources> getCurrentDistribution() {
        return null;
    }

}
