package fr.agilix.services.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of RoomSearchRepository to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class RoomSearchRepositoryMockConfiguration {

    @MockBean
    private RoomSearchRepository mockRoomSearchRepository;

}
