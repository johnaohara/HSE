package model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.net.URL;
import java.util.List;

@Entity
public class DevInstance extends PanacheEntity {

    public String name;
    public String description;
    public String githubRepo;
    public String githubCommit;
    @Enumerated(EnumType.ORDINAL)
    public InstanceStatus status;
    public String url;
    @ManyToOne
    public DatabaseStorage databaseStorage;

    public Boolean isHidden = false;

    @ManyToOne
    public User owner;

    public enum InstanceStatus {
        CREATED, PENDING, RUNNING, STOPPING, STOPPED, ERROR
    }

    public static List<DevInstance> findByOwner(User user) {
        return find("owner = ?1 ORDER BY id", user).list();
    }

}
