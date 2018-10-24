package fr.agilix.services.repository.search;

import fr.agilix.services.domain.Room;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Room entity.
 */
public interface RoomSearchRepository extends ElasticsearchRepository<Room, Long> {
}
