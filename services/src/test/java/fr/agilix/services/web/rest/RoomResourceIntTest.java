package fr.agilix.services.web.rest;

import fr.agilix.services.ServicesApp;

import fr.agilix.services.config.SecurityBeanOverrideConfiguration;

import fr.agilix.services.domain.Room;
import fr.agilix.services.repository.RoomRepository;
import fr.agilix.services.repository.search.RoomSearchRepository;
import fr.agilix.services.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;


import static fr.agilix.services.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the RoomResource REST controller.
 *
 * @see RoomResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, ServicesApp.class})
public class RoomResourceIntTest {

    private static final String DEFAULT_ROOM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ROOM_NAME = "BBBBBBBBBB";

    @Autowired
    private RoomRepository roomRepository;

    /**
     * This repository is mocked in the fr.agilix.services.repository.search test package.
     *
     * @see fr.agilix.services.repository.search.RoomSearchRepositoryMockConfiguration
     */
    @Autowired
    private RoomSearchRepository mockRoomSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restRoomMockMvc;

    private Room room;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final RoomResource roomResource = new RoomResource(roomRepository, mockRoomSearchRepository);
        this.restRoomMockMvc = MockMvcBuilders.standaloneSetup(roomResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Room createEntity(EntityManager em) {
        Room room = new Room()
            .roomName(DEFAULT_ROOM_NAME);
        return room;
    }

    @Before
    public void initTest() {
        room = createEntity(em);
    }

    @Test
    @Transactional
    public void createRoom() throws Exception {
        int databaseSizeBeforeCreate = roomRepository.findAll().size();

        // Create the Room
        restRoomMockMvc.perform(post("/api/rooms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isCreated());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate + 1);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomName()).isEqualTo(DEFAULT_ROOM_NAME);

        // Validate the Room in Elasticsearch
        verify(mockRoomSearchRepository, times(1)).save(testRoom);
    }

    @Test
    @Transactional
    public void createRoomWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = roomRepository.findAll().size();

        // Create the Room with an existing ID
        room.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRoomMockMvc.perform(post("/api/rooms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeCreate);

        // Validate the Room in Elasticsearch
        verify(mockRoomSearchRepository, times(0)).save(room);
    }

    @Test
    @Transactional
    public void getAllRooms() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get all the roomList
        restRoomMockMvc.perform(get("/api/rooms?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(room.getId().intValue())))
            .andExpect(jsonPath("$.[*].roomName").value(hasItem(DEFAULT_ROOM_NAME.toString())));
    }
    
    @Test
    @Transactional
    public void getRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        // Get the room
        restRoomMockMvc.perform(get("/api/rooms/{id}", room.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(room.getId().intValue()))
            .andExpect(jsonPath("$.roomName").value(DEFAULT_ROOM_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingRoom() throws Exception {
        // Get the room
        restRoomMockMvc.perform(get("/api/rooms/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Update the room
        Room updatedRoom = roomRepository.findById(room.getId()).get();
        // Disconnect from session so that the updates on updatedRoom are not directly saved in db
        em.detach(updatedRoom);
        updatedRoom
            .roomName(UPDATED_ROOM_NAME);

        restRoomMockMvc.perform(put("/api/rooms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRoom)))
            .andExpect(status().isOk());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);
        Room testRoom = roomList.get(roomList.size() - 1);
        assertThat(testRoom.getRoomName()).isEqualTo(UPDATED_ROOM_NAME);

        // Validate the Room in Elasticsearch
        verify(mockRoomSearchRepository, times(1)).save(testRoom);
    }

    @Test
    @Transactional
    public void updateNonExistingRoom() throws Exception {
        int databaseSizeBeforeUpdate = roomRepository.findAll().size();

        // Create the Room

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoomMockMvc.perform(put("/api/rooms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(room)))
            .andExpect(status().isBadRequest());

        // Validate the Room in the database
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Room in Elasticsearch
        verify(mockRoomSearchRepository, times(0)).save(room);
    }

    @Test
    @Transactional
    public void deleteRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);

        int databaseSizeBeforeDelete = roomRepository.findAll().size();

        // Get the room
        restRoomMockMvc.perform(delete("/api/rooms/{id}", room.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Room> roomList = roomRepository.findAll();
        assertThat(roomList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Room in Elasticsearch
        verify(mockRoomSearchRepository, times(1)).deleteById(room.getId());
    }

    @Test
    @Transactional
    public void searchRoom() throws Exception {
        // Initialize the database
        roomRepository.saveAndFlush(room);
        when(mockRoomSearchRepository.search(queryStringQuery("id:" + room.getId())))
            .thenReturn(Collections.singletonList(room));
        // Search the room
        restRoomMockMvc.perform(get("/api/_search/rooms?query=id:" + room.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(room.getId().intValue())))
            .andExpect(jsonPath("$.[*].roomName").value(hasItem(DEFAULT_ROOM_NAME.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Room.class);
        Room room1 = new Room();
        room1.setId(1L);
        Room room2 = new Room();
        room2.setId(room1.getId());
        assertThat(room1).isEqualTo(room2);
        room2.setId(2L);
        assertThat(room1).isNotEqualTo(room2);
        room1.setId(null);
        assertThat(room1).isNotEqualTo(room2);
    }
}
