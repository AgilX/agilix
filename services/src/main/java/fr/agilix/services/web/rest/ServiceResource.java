package fr.agilix.services.web.rest;

import com.codahale.metrics.annotation.Timed;
import fr.agilix.services.domain.Service;
import fr.agilix.services.repository.ServiceRepository;
import fr.agilix.services.repository.search.ServiceSearchRepository;
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
 * REST controller for managing Service.
 */
@RestController
@RequestMapping("/api")
public class ServiceResource {

    private final Logger log = LoggerFactory.getLogger(ServiceResource.class);

    private static final String ENTITY_NAME = "servicesService";

    private ServiceRepository serviceRepository;

    private ServiceSearchRepository serviceSearchRepository;

    public ServiceResource(ServiceRepository serviceRepository, ServiceSearchRepository serviceSearchRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceSearchRepository = serviceSearchRepository;
    }

    /**
     * POST  /services : Create a new service.
     *
     * @param service the service to create
     * @return the ResponseEntity with status 201 (Created) and with body the new service, or with status 400 (Bad Request) if the service has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/services")
    @Timed
    public ResponseEntity<Service> createService(@RequestBody Service service) throws URISyntaxException {
        log.debug("REST request to save Service : {}", service);
        if (service.getId() != null) {
            throw new BadRequestAlertException("A new service cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Service result = serviceRepository.save(service);
        serviceSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /services : Updates an existing service.
     *
     * @param service the service to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated service,
     * or with status 400 (Bad Request) if the service is not valid,
     * or with status 500 (Internal Server Error) if the service couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/services")
    @Timed
    public ResponseEntity<Service> updateService(@RequestBody Service service) throws URISyntaxException {
        log.debug("REST request to update Service : {}", service);
        if (service.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Service result = serviceRepository.save(service);
        serviceSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, service.getId().toString()))
            .body(result);
    }

    /**
     * GET  /services : get all the services.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of services in body
     */
    @GetMapping("/services")
    @Timed
    public List<Service> getAllServices() {
        log.debug("REST request to get all Services");
        return serviceRepository.findAll();
    }

    /**
     * GET  /services/:id : get the "id" service.
     *
     * @param id the id of the service to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the service, or with status 404 (Not Found)
     */
    @GetMapping("/services/{id}")
    @Timed
    public ResponseEntity<Service> getService(@PathVariable Long id) {
        log.debug("REST request to get Service : {}", id);
        Optional<Service> service = serviceRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(service);
    }

    /**
     * DELETE  /services/:id : delete the "id" service.
     *
     * @param id the id of the service to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/services/{id}")
    @Timed
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.debug("REST request to delete Service : {}", id);

        serviceRepository.deleteById(id);
        serviceSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/services?query=:query : search for the service corresponding
     * to the query.
     *
     * @param query the query of the service search
     * @return the result of the search
     */
    @GetMapping("/_search/services")
    @Timed
    public List<Service> searchServices(@RequestParam String query) {
        log.debug("REST request to search Services for query {}", query);
        return StreamSupport
            .stream(serviceSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
