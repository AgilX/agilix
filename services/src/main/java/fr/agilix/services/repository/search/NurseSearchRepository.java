package fr.agilix.services.repository.search;

import fr.agilix.services.domain.Nurse;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Nurse entity.
 */
public interface NurseSearchRepository extends ElasticsearchRepository<Nurse, Long> {
}
