package model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class DatabaseStorage extends PanacheEntity {

    public String name;

    @Enumerated(EnumType.ORDINAL)
    public DatabaseType type;

    public String fileLocation;

    public enum DatabaseType {
        NONE, BACKUP
    }

}
