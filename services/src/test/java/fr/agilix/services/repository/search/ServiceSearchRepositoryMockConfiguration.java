package fr.agilix.services.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of ServiceSearchRepository to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class ServiceSearchRepositoryMockConfiguration {

    @MockBean
    private ServiceSearchRepository mockServiceSearchRepository;

}
