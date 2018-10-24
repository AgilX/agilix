package fr.agilix.services.web.rest;

import com.codahale.metrics.annotation.Timed;
import fr.agilix.services.domain.Patient;
import fr.agilix.services.repository.PatientRepository;
import fr.agilix.services.repository.search.PatientSearchRepository;
import fr.agilix.services.web.rest.errors.BadRequestAlertException;
import fr.agilix.services.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Patient.
 */
@RestController
@RequestMapping("/api")
public class PatientResource {

    private final Logger log = LoggerFactory.getLogger(PatientResource.class);

    private static final String ENTITY_NAME = "servicesPatient";

    private PatientRepository patientRepository;

    private PatientSearchRepository patientSearchRepository;

    public PatientResource(PatientRepository patientRepository, PatientSearchRepository patientSearchRepository) {
        this.patientRepository = patientRepository;
        this.patientSearchRepository = patientSearchRepository;
    }

    /**
     * POST  /patients : Create a new patient.
     *
     * @param patient the patient to create
     * @return the ResponseEntity with status 201 (Created) and with body the new patient, or with status 400 (Bad Request) if the patient has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/patients")
    @Timed
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) throws URISyntaxException {
        log.debug("REST request to save Patient : {}", patient);
        if (patient.getId() != null) {
            throw new BadRequestAlertException("A new patient cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Patient result = patientRepository.save(patient);
        patientSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/patients/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /patients : Updates an existing patient.
     *
     * @param patient the patient to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated patient,
     * or with status 400 (Bad Request) if the patient is not valid,
     * or with status 500 (Internal Server Error) if the patient couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/patients")
    @Timed
    public ResponseEntity<Patient> updatePatient(@RequestBody Patient patient) throws URISyntaxException {
        log.debug("REST request to update Patient : {}", patient);
        if (patient.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Patient result = patientRepository.save(patient);
        patientSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, patient.getId().toString()))
            .body(result);
    }

    /**
     * GET  /patients : get all the patients.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of patients in body
     */
    @GetMapping("/patients")
    @Timed
    public List<Patient> getAllPatients() {
        log.debug("REST request to get all Patients");
        return patientRepository.findAll();
    }

    /**
     * GET  /patients/:id : get the "id" patient.
     *
     * @param id the id of the patient to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the patient, or with status 404 (Not Found)
     */
    @GetMapping("/patients/{id}")
    @Timed
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        log.debug("REST request to get Patient : {}", id);
        Optional<Patient> patient = patientRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(patient);
    }

    /**
     * DELETE  /patients/:id : delete the "id" patient.
     *
     * @param id the id of the patient to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/patients/{id}")
    @Timed
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.debug("REST request to delete Patient : {}", id);

        patientRepository.deleteById(id);
        patientSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/patients?query=:query : search for the patient corresponding
     * to the query.
     *
     * @param query the query of the patient search
     * @return the result of the search
     */
    @GetMapping("/_search/patients")
    @Timed
    public List<Patient> searchPatients(@RequestParam String query) {
        log.debug("REST request to search Patients for query {}", query);
        return StreamSupport
            .stream(patientSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
