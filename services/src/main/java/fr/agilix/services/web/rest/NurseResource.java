package fr.agilix.services.web.rest;

import com.codahale.metrics.annotation.Timed;
import fr.agilix.services.domain.Nurse;
import fr.agilix.services.repository.NurseRepository;
import fr.agilix.services.repository.search.NurseSearchRepository;
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
 * REST controller for managing Nurse.
 */
@RestController
@RequestMapping("/api")
public class NurseResource {

    private final Logger log = LoggerFactory.getLogger(NurseResource.class);

    private static final String ENTITY_NAME = "servicesNurse";

    private NurseRepository nurseRepository;

    private NurseSearchRepository nurseSearchRepository;

    public NurseResource(NurseRepository nurseRepository, NurseSearchRepository nurseSearchRepository) {
        this.nurseRepository = nurseRepository;
        this.nurseSearchRepository = nurseSearchRepository;
    }

    /**
     * POST  /nurses : Create a new nurse.
     *
     * @param nurse the nurse to create
     * @return the ResponseEntity with status 201 (Created) and with body the new nurse, or with status 400 (Bad Request) if the nurse has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/nurses")
    @Timed
    public ResponseEntity<Nurse> createNurse(@RequestBody Nurse nurse) throws URISyntaxException {
        log.debug("REST request to save Nurse : {}", nurse);
        if (nurse.getId() != null) {
            throw new BadRequestAlertException("A new nurse cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Nurse result = nurseRepository.save(nurse);
        nurseSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/nurses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /nurses : Updates an existing nurse.
     *
     * @param nurse the nurse to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated nurse,
     * or with status 400 (Bad Request) if the nurse is not valid,
     * or with status 500 (Internal Server Error) if the nurse couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/nurses")
    @Timed
    public ResponseEntity<Nurse> updateNurse(@RequestBody Nurse nurse) throws URISyntaxException {
        log.debug("REST request to update Nurse : {}", nurse);
        if (nurse.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Nurse result = nurseRepository.save(nurse);
        nurseSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, nurse.getId().toString()))
            .body(result);
    }

    /**
     * GET  /nurses : get all the nurses.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of nurses in body
     */
    @GetMapping("/nurses")
    @Timed
    public List<Nurse> getAllNurses() {
        log.debug("REST request to get all Nurses");
        return nurseRepository.findAll();
    }

    /**
     * GET  /nurses/:id : get the "id" nurse.
     *
     * @param id the id of the nurse to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the nurse, or with status 404 (Not Found)
     */
    @GetMapping("/nurses/{id}")
    @Timed
    public ResponseEntity<Nurse> getNurse(@PathVariable Long id) {
        log.debug("REST request to get Nurse : {}", id);
        Optional<Nurse> nurse = nurseRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(nurse);
    }

    /**
     * DELETE  /nurses/:id : delete the "id" nurse.
     *
     * @param id the id of the nurse to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/nurses/{id}")
    @Timed
    public ResponseEntity<Void> deleteNurse(@PathVariable Long id) {
        log.debug("REST request to delete Nurse : {}", id);

        nurseRepository.deleteById(id);
        nurseSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/nurses?query=:query : search for the nurse corresponding
     * to the query.
     *
     * @param query the query of the nurse search
     * @return the result of the search
     */
    @GetMapping("/_search/nurses")
    @Timed
    public List<Nurse> searchNurses(@RequestParam String query) {
        log.debug("REST request to search Nurses for query {}", query);
        return StreamSupport
            .stream(nurseSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
