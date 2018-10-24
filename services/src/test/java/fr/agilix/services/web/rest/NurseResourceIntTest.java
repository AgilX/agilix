package fr.agilix.services.web.rest;

import fr.agilix.services.ServicesApp;

import fr.agilix.services.config.SecurityBeanOverrideConfiguration;

import fr.agilix.services.domain.Nurse;
import fr.agilix.services.repository.NurseRepository;
import fr.agilix.services.repository.search.NurseSearchRepository;
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
 * Test class for the NurseResource REST controller.
 *
 * @see NurseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, ServicesApp.class})
public class NurseResourceIntTest {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    @Autowired
    private NurseRepository nurseRepository;

    /**
     * This repository is mocked in the fr.agilix.services.repository.search test package.
     *
     * @see fr.agilix.services.repository.search.NurseSearchRepositoryMockConfiguration
     */
    @Autowired
    private NurseSearchRepository mockNurseSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restNurseMockMvc;

    private Nurse nurse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final NurseResource nurseResource = new NurseResource(nurseRepository, mockNurseSearchRepository);
        this.restNurseMockMvc = MockMvcBuilders.standaloneSetup(nurseResource)
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
    public static Nurse createEntity(EntityManager em) {
        Nurse nurse = new Nurse()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME);
        return nurse;
    }

    @Before
    public void initTest() {
        nurse = createEntity(em);
    }

    @Test
    @Transactional
    public void createNurse() throws Exception {
        int databaseSizeBeforeCreate = nurseRepository.findAll().size();

        // Create the Nurse
        restNurseMockMvc.perform(post("/api/nurses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nurse)))
            .andExpect(status().isCreated());

        // Validate the Nurse in the database
        List<Nurse> nurseList = nurseRepository.findAll();
        assertThat(nurseList).hasSize(databaseSizeBeforeCreate + 1);
        Nurse testNurse = nurseList.get(nurseList.size() - 1);
        assertThat(testNurse.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testNurse.getLastName()).isEqualTo(DEFAULT_LAST_NAME);

        // Validate the Nurse in Elasticsearch
        verify(mockNurseSearchRepository, times(1)).save(testNurse);
    }

    @Test
    @Transactional
    public void createNurseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = nurseRepository.findAll().size();

        // Create the Nurse with an existing ID
        nurse.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restNurseMockMvc.perform(post("/api/nurses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nurse)))
            .andExpect(status().isBadRequest());

        // Validate the Nurse in the database
        List<Nurse> nurseList = nurseRepository.findAll();
        assertThat(nurseList).hasSize(databaseSizeBeforeCreate);

        // Validate the Nurse in Elasticsearch
        verify(mockNurseSearchRepository, times(0)).save(nurse);
    }

    @Test
    @Transactional
    public void getAllNurses() throws Exception {
        // Initialize the database
        nurseRepository.saveAndFlush(nurse);

        // Get all the nurseList
        restNurseMockMvc.perform(get("/api/nurses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(nurse.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME.toString())))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME.toString())));
    }
    
    @Test
    @Transactional
    public void getNurse() throws Exception {
        // Initialize the database
        nurseRepository.saveAndFlush(nurse);

        // Get the nurse
        restNurseMockMvc.perform(get("/api/nurses/{id}", nurse.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(nurse.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME.toString()))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingNurse() throws Exception {
        // Get the nurse
        restNurseMockMvc.perform(get("/api/nurses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNurse() throws Exception {
        // Initialize the database
        nurseRepository.saveAndFlush(nurse);

        int databaseSizeBeforeUpdate = nurseRepository.findAll().size();

        // Update the nurse
        Nurse updatedNurse = nurseRepository.findById(nurse.getId()).get();
        // Disconnect from session so that the updates on updatedNurse are not directly saved in db
        em.detach(updatedNurse);
        updatedNurse
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME);

        restNurseMockMvc.perform(put("/api/nurses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedNurse)))
            .andExpect(status().isOk());

        // Validate the Nurse in the database
        List<Nurse> nurseList = nurseRepository.findAll();
        assertThat(nurseList).hasSize(databaseSizeBeforeUpdate);
        Nurse testNurse = nurseList.get(nurseList.size() - 1);
        assertThat(testNurse.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testNurse.getLastName()).isEqualTo(UPDATED_LAST_NAME);

        // Validate the Nurse in Elasticsearch
        verify(mockNurseSearchRepository, times(1)).save(testNurse);
    }

    @Test
    @Transactional
    public void updateNonExistingNurse() throws Exception {
        int databaseSizeBeforeUpdate = nurseRepository.findAll().size();

        // Create the Nurse

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNurseMockMvc.perform(put("/api/nurses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(nurse)))
            .andExpect(status().isBadRequest());

        // Validate the Nurse in the database
        List<Nurse> nurseList = nurseRepository.findAll();
        assertThat(nurseList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Nurse in Elasticsearch
        verify(mockNurseSearchRepository, times(0)).save(nurse);
    }

    @Test
    @Transactional
    public void deleteNurse() throws Exception {
        // Initialize the database
        nurseRepository.saveAndFlush(nurse);

        int databaseSizeBeforeDelete = nurseRepository.findAll().size();

        // Get the nurse
        restNurseMockMvc.perform(delete("/api/nurses/{id}", nurse.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Nurse> nurseList = nurseRepository.findAll();
        assertThat(nurseList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Nurse in Elasticsearch
        verify(mockNurseSearchRepository, times(1)).deleteById(nurse.getId());
    }

    @Test
    @Transactional
    public void searchNurse() throws Exception {
        // Initialize the database
        nurseRepository.saveAndFlush(nurse);
        when(mockNurseSearchRepository.search(queryStringQuery("id:" + nurse.getId())))
            .thenReturn(Collections.singletonList(nurse));
        // Search the nurse
        restNurseMockMvc.perform(get("/api/_search/nurses?query=id:" + nurse.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(nurse.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME.toString())))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Nurse.class);
        Nurse nurse1 = new Nurse();
        nurse1.setId(1L);
        Nurse nurse2 = new Nurse();
        nurse2.setId(nurse1.getId());
        assertThat(nurse1).isEqualTo(nurse2);
        nurse2.setId(2L);
        assertThat(nurse1).isNotEqualTo(nurse2);
        nurse1.setId(null);
        assertThat(nurse1).isNotEqualTo(nurse2);
    }
}
