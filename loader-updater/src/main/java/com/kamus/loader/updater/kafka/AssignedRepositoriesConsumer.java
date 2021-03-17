package com.kamus.loader.updater.kafka;

import com.kamus.core.model.Loader;
import com.kamus.loader.updater.service.LoaderConfigurationService;
import com.kamus.loader.updater.service.LoadersUpdaterService;
import com.kamus.loader.updater.service.exception.NoActiveLoaderException;
import com.kamus.loaderconfig.grpcjava.AssignedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AssignedRepositoriesConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AssignedRepositoriesConsumer.class);

    private final LoaderConfigurationService loaderConfigurationService;
    private final LoadersUpdaterService loadersUpdaterService;

    public AssignedRepositoriesConsumer(LoaderConfigurationService loaderConfigurationService, LoadersUpdaterService loadersUpdaterService) {
        this.loaderConfigurationService = loaderConfigurationService;
        this.loadersUpdaterService = loadersUpdaterService;
    }

    @KafkaListener(topics = "assigned.repositories", containerFactory = "assignedRepositoriesListenerContainerFactory")
    public void processAssignedRepository(@Payload AssignedRepository assignedRepository) {
        logger.info("Processing {}", assignedRepository);

        try {
            Loader loader =
                    loaderConfigurationService.getLoaderForBucket(assignedRepository.getBucketId());
            updateLoader(loader, assignedRepository);
        } catch (NoActiveLoaderException e) {
            logger.info("No active loader for {}", assignedRepository);
        }
    }

    private void updateLoader(Loader loader, AssignedRepository assignedRepository) {
        try {
            loadersUpdaterService.updateLoaderWithRepository(loader, assignedRepository);
        } catch (Exception e) {
            logger.error("Wasn't able to update loader {} with the repository {} because of {}", loader, assignedRepository, e);
        }
    }

}
