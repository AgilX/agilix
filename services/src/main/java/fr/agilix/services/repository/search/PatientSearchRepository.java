package fr.agilix.services.repository.search;

import fr.agilix.services.domain.Patient;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Patient entity.
 */
public interface PatientSearchRepository extends ElasticsearchRepository<Patient, Long> {
}
