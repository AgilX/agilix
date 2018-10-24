package fr.agilix.services.web.rest;

import com.codahale.metrics.annotation.Timed;
import fr.agilix.services.domain.Room;
import fr.agilix.services.repository.RoomRepository;
import fr.agilix.services.repository.search.RoomSearchRepository;
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
 * REST controller for managing Room.
 */
@RestController
@RequestMapping("/api")
public class RoomResource {

    private final Logger log = LoggerFactory.getLogger(RoomResource.class);

    private static final String ENTITY_NAME = "servicesRoom";

    private RoomRepository roomRepository;

    private RoomSearchRepository roomSearchRepository;

    public RoomResource(RoomRepository roomRepository, RoomSearchRepository roomSearchRepository) {
        this.roomRepository = roomRepository;
        this.roomSearchRepository = roomSearchRepository;
    }

    /**
     * POST  /rooms : Create a new room.
     *
     * @param room the room to create
     * @return the ResponseEntity with status 201 (Created) and with body the new room, or with status 400 (Bad Request) if the room has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/rooms")
    @Timed
    public ResponseEntity<Room> createRoom(@RequestBody Room room) throws URISyntaxException {
        log.debug("REST request to save Room : {}", room);
        if (room.getId() != null) {
            throw new BadRequestAlertException("A new room cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Room result = roomRepository.save(room);
        roomSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/rooms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /rooms : Updates an existing room.
     *
     * @param room the room to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated room,
     * or with status 400 (Bad Request) if the room is not valid,
     * or with status 500 (Internal Server Error) if the room couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/rooms")
    @Timed
    public ResponseEntity<Room> updateRoom(@RequestBody Room room) throws URISyntaxException {
        log.debug("REST request to update Room : {}", room);
        if (room.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Room result = roomRepository.save(room);
        roomSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, room.getId().toString()))
            .body(result);
    }

    /**
     * GET  /rooms : get all the rooms.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of rooms in body
     */
    @GetMapping("/rooms")
    @Timed
    public List<Room> getAllRooms() {
        log.debug("REST request to get all Rooms");
        return roomRepository.findAll();
    }

    /**
     * GET  /rooms/:id : get the "id" room.
     *
     * @param id the id of the room to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the room, or with status 404 (Not Found)
     */
    @GetMapping("/rooms/{id}")
    @Timed
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        log.debug("REST request to get Room : {}", id);
        Optional<Room> room = roomRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(room);
    }

    /**
     * DELETE  /rooms/:id : delete the "id" room.
     *
     * @param id the id of the room to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/rooms/{id}")
    @Timed
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        log.debug("REST request to delete Room : {}", id);

        roomRepository.deleteById(id);
        roomSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/rooms?query=:query : search for the room corresponding
     * to the query.
     *
     * @param query the query of the room search
     * @return the result of the search
     */
    @GetMapping("/_search/rooms")
    @Timed
    public List<Room> searchRooms(@RequestParam String query) {
        log.debug("REST request to search Rooms for query {}", query);
        return StreamSupport
            .stream(roomSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
