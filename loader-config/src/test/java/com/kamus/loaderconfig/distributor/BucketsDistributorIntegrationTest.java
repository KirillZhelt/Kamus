package com.kamus.loaderconfig.distributor;

import com.google.common.collect.ImmutableSet;
import com.kamus.loaderconfig.db.model.DistributedBucket;
import com.kamus.loaderconfig.db.repository.DistributedBucketRepository;
import com.kamus.loaderconfig.distributor.model.AssignedBucketsInterval;
import com.kamus.loaderconfig.distributor.model.BucketsDistribution;
import com.kamus.core.model.LoaderId;
import com.kamus.loaderconfig.distributor.config.DistributorTestConfig;
import com.kamus.loaderconfig.integration.MysqlIntegrationTestsContainer;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DistributorTestConfig.class)
public class BucketsDistributorIntegrationTest {

    @ClassRule
    public static MysqlIntegrationTestsContainer mysqlContainer = MysqlIntegrationTestsContainer.getInstance();

    @Autowired
    private BucketsDistributor distributor;

    @Autowired
    private DistributedBucketRepository repository;

    @Test
    public void testFirstEverDistributionOnInit_NoLoaders() {
        Set<LoaderId> loaders = Collections.emptySet();

        Map<LoaderId, BucketsDistribution> distribution = distributor.distribute(loaders);

        assertThat(distribution).hasSize(0);
    }

    @Test
    public void testFirstEverDistributionOnInit_EqualLoad() {
        LoaderId l1 = new LoaderId("loader1");
        LoaderId l2 = new LoaderId("loader2");
        LoaderId l3 = new LoaderId("loader3");
        LoaderId l4 = new LoaderId("loader4");

        Set<LoaderId> loaders = ImmutableSet.of(l1, l2, l3, l4);

        Map<LoaderId, BucketsDistribution> distribution = distributor.distribute(loaders);

        assertThat(distribution).hasSize(4);

        AssignedBucketsInterval buckets1 = distribution.get(new LoaderId("loader1")).getAssignedBuckets();
        assertEquals(0, buckets1.getStartBucket());
        assertEquals(64, buckets1.getEndBucket());

        AssignedBucketsInterval buckets2 = distribution.get(new LoaderId("loader2")).getAssignedBuckets();
        assertEquals(64, buckets2.getStartBucket());
        assertEquals(128, buckets2.getEndBucket());

        AssignedBucketsInterval buckets3 = distribution.get(new LoaderId("loader3")).getAssignedBuckets();
        assertEquals(128, buckets3.getStartBucket());
        assertEquals(192, buckets3.getEndBucket());

        AssignedBucketsInterval buckets4 = distribution.get(new LoaderId("loader4")).getAssignedBuckets();
        assertEquals(192, buckets4.getStartBucket());
        assertEquals(256, buckets4.getEndBucket());

        List<DistributedBucket> buckets = repository.findAll();
        assertThat(buckets)
                .hasSize(256);

        for (int i = 0; i < 64; i++) {
            assertEquals(l1.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 64; i < 128; i++) {
            assertEquals(l2.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 128; i < 192; i++) {
            assertEquals(l3.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 192; i < 256; i++) {
            assertEquals(l4.getId(), buckets.get(i).getLoaderId());
        }

    }

    @Test
    public void testFirstEverDistributionOnInit_NonEqualLoad() {
        LoaderId l1 = new LoaderId("loader1");
        LoaderId l2 = new LoaderId("loader2");
        LoaderId l3 = new LoaderId("loader3");
        LoaderId l4 = new LoaderId("loader4");
        LoaderId l5 = new LoaderId("loader5");

        Set<LoaderId> loaders = ImmutableSet.of(l1, l2, l3, l4, l5);

        Map<LoaderId, BucketsDistribution> distribution = distributor.distribute(loaders);

        assertThat(distribution).hasSize(5);

        AssignedBucketsInterval buckets1 = distribution.get(new LoaderId("loader1")).getAssignedBuckets();
        assertEquals(0, buckets1.getStartBucket());
        assertEquals(52, buckets1.getEndBucket());

        AssignedBucketsInterval buckets2 = distribution.get(new LoaderId("loader2")).getAssignedBuckets();
        assertEquals(52, buckets2.getStartBucket());
        assertEquals(103, buckets2.getEndBucket());

        AssignedBucketsInterval buckets3 = distribution.get(new LoaderId("loader3")).getAssignedBuckets();
        assertEquals(103, buckets3.getStartBucket());
        assertEquals(154, buckets3.getEndBucket());

        AssignedBucketsInterval buckets4 = distribution.get(new LoaderId("loader4")).getAssignedBuckets();
        assertEquals(154, buckets4.getStartBucket());
        assertEquals(205, buckets4.getEndBucket());

        AssignedBucketsInterval buckets5 = distribution.get(new LoaderId("loader5")).getAssignedBuckets();
        assertEquals(205, buckets5.getStartBucket());
        assertEquals(256, buckets5.getEndBucket());

        List<DistributedBucket> buckets = repository.findAll();
        assertThat(buckets)
                .hasSize(256);

        for (int i = 0; i < 52; i++) {
            assertEquals(l1.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 52; i < 103; i++) {
            assertEquals(l2.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 103; i < 154; i++) {
            assertEquals(l3.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 154; i < 205; i++) {
            assertEquals(l4.getId(), buckets.get(i).getLoaderId());
        }

        for (int i = 205; i < 256; i++) {
            assertEquals(l5.getId(), buckets.get(i).getLoaderId());
        }

    }


}
