package svc;

import io.hyperfoil.tools.qdup.QDup;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import model.DevInstance;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DevInstanceManager {

    private static final Logger log = Logger.getLogger(DevInstanceManager.class);
    @Inject
    EntityManager entityManager;

    @Blocking
    @ConsumeEvent("newDevInstance")
    public void consumeNewDevInstance(Message<Long> msg) {
        log.infof("Received newDevInstance event: %d", msg.body());
        try {
            startDevInstance(msg.body());
        } catch (InstanceManagementException e) {
            log.errorf("Error starting DevInstance with id %d: %s", msg.body(), e.getMessage());
        }
    }
    @Blocking
    @ConsumeEvent("stopDevInstance")
    @Transactional
    public void consumeStopDevInstance(Message<Long> msg) {
        log.infof("Received stopDevInstance event: %d", msg.body());
    }

    private static final Map<Long, Object> runningDevInstances = new HashMap<>();


    public void startDevInstance(Long id) throws InstanceManagementException {
        if ( runningDevInstances.containsKey(id) ) {
            throw new InstanceManagementException("DevInstance with id " + id + " is already running");
        }


//        EntityTransaction tx = entityManager.getTransaction();
//        tx.begin();
        try {
            DevInstance devInstance = getDevInstance(id);

            if (devInstance.status != DevInstance.InstanceStatus.CREATED) {
                log.warnf("Error starting DevInstance with id %d: status (%s)", id, devInstance.status);
                throw new InstanceManagementException("Cannot start DevInstance with id " + id);
            }
            devInstance.status = DevInstance.InstanceStatus.PENDING;
            devInstance.persist();
//            tx.commit();
        } catch (Exception e){
            log.warnf("Error stopping DevInstance with id %d: %s", id, e.getMessage());
//            tx.rollback();
        }

//        tx = entityManager.getTransaction();
//        tx.begin();
        DevInstance devInstance = getDevInstance(id);

        String tmpdir = null;
        try {
            tmpdir = Files.createTempDirectory("tmpDirQdup").toFile().getAbsolutePath();
//            String tmpDirsLocation = System.getProperty("java.io.tmpdir");

            InputStream qDupStream = null;

            qDupStream = DevInstanceManager.class.getResourceAsStream("startDevInstance.qdup.yaml");//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(qDupStream == null) {

                URL resourceUrl = DevInstanceManager.class.getResource("startDevInstance.qdup.yaml");

                if ( resourceUrl == null) {
                    log.errorf("Cannot get resource \"qdup/startDevInstance.qdup.yaml\" from Jar file.");
                    throw new Exception("Cannot get resource \"qdup/startDevInstance.qdup.yaml\" from Jar file.");
                }

            }

            Files.copy( qDupStream, Path.of(tmpdir, "startDevInstance.qdup.yaml"));

            String[] args = {"-B", tmpdir,
                    Path.of(tmpdir, "startDevInstance.qdup.yaml").toString(),
                    "-S", "TARGET_DIR=".concat(tmpdir),
                    "-S", "GIT_REPO=".concat(devInstance.githubRepo),
                    "-S", "GIT_BRANCH=".concat(devInstance.githubCommit),
            };

            QDup qdup = new QDup(args);

            qdup.run();

            devInstance.status = DevInstance.InstanceStatus.RUNNING;

//            tx.commit();

            runningDevInstances.put(id, new Object());
        } catch (IOException e) {
            log.errorf("Error creating temp directory: %s", e.getMessage());
//            tx.rollback();
        } catch (Exception e) {
            log.errorf("Error creating temp directory: %s", e.getMessage());
//            tx.rollback();
        }


    }

    public void stopDevInstance(Long id) throws InstanceManagementException {
        if ( !runningDevInstances.containsKey(id) ) {
            throw new InstanceManagementException("DevInstance with id " + id + " is not running");
        }

        DevInstance devInstance;

        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            devInstance = getDevInstance(id);

            if (devInstance.status != DevInstance.InstanceStatus.RUNNING) {
                throw new InstanceManagementException("Cannot stop DevInstance with id " + id + " is not running");
            }
            devInstance.status = DevInstance.InstanceStatus.STOPPING;
            devInstance.persist();
            tx.commit();
        } catch (Exception e){
            log.warnf("Error stopping DevInstance with id %d: %s", id, e.getMessage());
            tx.rollback();
        }



        runningDevInstances.remove(id);
    }



    private DevInstance getDevInstance(Long id) throws InstanceManagementException {
        DevInstance devInstance = DevInstance.findById(id);

        if ( devInstance == null ) {
            throw new InstanceManagementException("DevInstance with id " + id + " does not exist");
        }
        return devInstance;
    }


}
