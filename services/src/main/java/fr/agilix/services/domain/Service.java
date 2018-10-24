package fr.agilix.services.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import org.springframework.data.elasticsearch.annotations.Document;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Service.
 */
@Entity
@Table(name = "service")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "service")
public class Service implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name")
    private String serviceName;

    @OneToMany(mappedBy = "service")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Room> rooms = new HashSet<>();
    @OneToMany(mappedBy = "service")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Nurse> nurses = new HashSet<>();
    @OneToMany(mappedBy = "service")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Patient> patients = new HashSet<>();
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Service serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public Service rooms(Set<Room> rooms) {
        this.rooms = rooms;
        return this;
    }

    public Service addRoom(Room room) {
        this.rooms.add(room);
        room.setService(this);
        return this;
    }

    public Service removeRoom(Room room) {
        this.rooms.remove(room);
        room.setService(null);
        return this;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    public Set<Nurse> getNurses() {
        return nurses;
    }

    public Service nurses(Set<Nurse> nurses) {
        this.nurses = nurses;
        return this;
    }

    public Service addNurse(Nurse nurse) {
        this.nurses.add(nurse);
        nurse.setService(this);
        return this;
    }

    public Service removeNurse(Nurse nurse) {
        this.nurses.remove(nurse);
        nurse.setService(null);
        return this;
    }

    public void setNurses(Set<Nurse> nurses) {
        this.nurses = nurses;
    }

    public Set<Patient> getPatients() {
        return patients;
    }

    public Service patients(Set<Patient> patients) {
        this.patients = patients;
        return this;
    }

    public Service addPatient(Patient patient) {
        this.patients.add(patient);
        patient.setService(this);
        return this;
    }

    public Service removePatient(Patient patient) {
        this.patients.remove(patient);
        patient.setService(null);
        return this;
    }

    public void setPatients(Set<Patient> patients) {
        this.patients = patients;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Service service = (Service) o;
        if (service.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), service.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Service{" +
            "id=" + getId() +
            ", serviceName='" + getServiceName() + "'" +
            "}";
    }
}
