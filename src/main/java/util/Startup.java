package util;

import java.util.Date;

import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import model.*;

public class Startup {
    @Transactional
    public void onStartup(@Observes StartupEvent start) {
        System.err.println("Adding user fromage");
        User stef = new User();
        stef.email = "fromage@example.com";
        stef.firstName = "Stef";
        stef.lastName = "Epardaud";
        stef.userName = "fromage";
        stef.password = BcryptUtil.bcryptHash("1q2w3e4r");
        stef.status = UserStatus.REGISTERED;
        stef.isAdmin = true;
        stef.persist();

        Todo todo = new Todo();
        todo.owner = stef;
        todo.task = "Buy cheese";
        todo.done = true;
        todo.doneDate = new Date();
        todo.persist();

        todo = new Todo();
        todo.owner = stef;
        todo.task = "Eat cheese";
        todo.persist();


        User john = new User();
        john.email = "johara@example.com";
        john.firstName = "John";
        john.lastName = "OHara";
        john.userName = "johara";
        john.password = BcryptUtil.bcryptHash("test");
        john.status = UserStatus.REGISTERED;
        john.isAdmin = true;
        john.persist();

        DatabaseStorage inContainerStorage = new DatabaseStorage();
        inContainerStorage.name = "None";
        inContainerStorage.type =  DatabaseStorage.DatabaseType.NONE;
        inContainerStorage.persist();

        DatabaseStorage prodBackupStorage = new DatabaseStorage();
        prodBackupStorage.name = "Prod Backup - 2023/11/15";
        prodBackupStorage.fileLocation = "/tmp/db-backup/2023-11-15.tar.gz";
        prodBackupStorage.type =  DatabaseStorage.DatabaseType.BACKUP;
        prodBackupStorage.persist();

        DatabaseStorage sampleStorage = new DatabaseStorage();
        sampleStorage.name = "Sample Data";
        sampleStorage.fileLocation = "/tmp/db-backup/sample.tar.gz";
        sampleStorage.type =  DatabaseStorage.DatabaseType.BACKUP;
        sampleStorage.persist();

        DevInstance createdDevInstance = new DevInstance();
        createdDevInstance.databaseStorage = inContainerStorage;
        createdDevInstance.status = DevInstance.InstanceStatus.CREATED;
        createdDevInstance.name = "PR 826";
        createdDevInstance.url = "http://mwperf-server17.mwperf.perf.lab.eng.rdu.redhat.com:32143";
        createdDevInstance.owner = john;
        createdDevInstance.githubRepo = "https://github.com/shivam-sharma7/Horreum/tree/hover-effect";
        createdDevInstance.githubCommit = "da099ac9f23e28663adb5b7eba48e712123416e2";
        createdDevInstance.description = "Add hover effect on nav elements, fixes #826";
        createdDevInstance.persist();


        DevInstance runnningDevInstance = new DevInstance();
        runnningDevInstance.databaseStorage = prodBackupStorage;
        runnningDevInstance.status = DevInstance.InstanceStatus.RUNNING;
        runnningDevInstance.name = "test Instance";
        runnningDevInstance.url = "http://mwperf-server17.mwperf.perf.lab.eng.rdu.redhat.com:34214";
        runnningDevInstance.owner = john;
        runnningDevInstance.githubRepo = "https://github.com/shivam-sharma7/Horreum/tree/hover-effect";
        runnningDevInstance.githubCommit = "da099ac9f23e28663adb5b7eba48e712123416e2";
        runnningDevInstance.description = "fixes 605";
        runnningDevInstance.persist();


    }

}
