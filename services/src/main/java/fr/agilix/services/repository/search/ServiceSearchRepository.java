package fr.agilix.services.repository.search;

import fr.agilix.services.domain.Service;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Service entity.
 */
public interface ServiceSearchRepository extends ElasticsearchRepository<Service, Long> {
}
