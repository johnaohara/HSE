package svc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.DevInstance;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DevInstanceManager {

    private static final Logger log = Logger.getLogger(DevInstanceManager.class);
    @Inject
    EntityManager entityManager;

    private static final Map<Long, Object> runningDevInstances = new HashMap<>();


    public void startDevInstance(Long id) throws InstanceManagementException {
        if ( runningDevInstances.containsKey(id) ) {
            throw new InstanceManagementException("DevInstance with id " + id + " is already running");
        }


        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            DevInstance devInstance = getDevInstance(id);

            if (devInstance.status != DevInstance.InstanceStatus.CREATED) {
                log.warnf("Error starting DevInstance with id %d: status (%s)", id, devInstance.status);
                throw new InstanceManagementException("Cannot start DevInstance with id " + id);
            }
            devInstance.status = DevInstance.InstanceStatus.STOPPING;
            devInstance.persist();
            tx.commit();
        } catch (Exception e){
            log.warnf("Error stopping DevInstance with id %d: %s", id, e.getMessage());
            tx.rollback();
        }

        DevInstance devInstance = getDevInstance(id);

        runningDevInstances.put(id, new Object());
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
