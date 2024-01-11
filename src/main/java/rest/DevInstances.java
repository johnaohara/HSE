package rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.WebApplicationException;
import model.DatabaseStorage;
import model.DevInstance;
import model.Todo;
import model.User;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import svc.DevInstanceManager;
import svc.InstanceManagementException;

import java.util.Date;
import java.util.List;

@Blocking
@Authenticated
public class DevInstances extends HxControllerWithUser<User> {

    private static final Logger log = Logger.getLogger(DevInstanceManager.class);

    @Inject
    EventBus bus;

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance htmx(List<DevInstance> devInstances, List<DatabaseStorage> databases,  String message);
        public static native TemplateInstance htmx$card(DevInstance devInstance);
        public static native TemplateInstance htmx$message(String message);
        public static native TemplateInstance htmx$error(String message);

        public static native TemplateInstance htmx$instanceList(List<DevInstance> devInstances, List<DatabaseStorage> databases,  String message);
    }
    
    public TemplateInstance htmx() {
        List<DevInstance> devInstances = DevInstance.findAll().list(); //.findByOwner(getUser());
        List<DatabaseStorage> dbStorage = DatabaseStorage.findAll().list(); //.findByOwner(getUser());
        return Templates.htmx(devInstances, dbStorage, null);
    }


    @POST
    public TemplateInstance stop(@RestPath Long id) {
        onlyHxRequest();
        DevInstance devInstance = DevInstance.findById(id);
        notFoundIfNull(devInstance);
        if(devInstance.owner != getUser())
            notFound();

        bus.send("stopDevInstance", devInstance.id);
        String message = i18n.formatMessage("todos.message.deleted", devInstance.name);
        // HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
//        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
        	 return Templates.htmx$card(devInstance);
    }

    @POST
    public TemplateInstance remove(@RestPath Long id) {
        onlyHxRequest();
        DevInstance devInstance = DevInstance.findById(id);
        notFoundIfNull(devInstance);
        if(devInstance.owner != getUser())
            notFound();

        bus.send("stopDevInstance", devInstance.id);

        devInstance.delete();

        String message = i18n.formatMessage("todos.message.deleted", devInstance.name);
        // HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
//        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
        List<DevInstance> devInstances = DevInstance.findAll().list(); //.findByOwner(getUser());
        List<DatabaseStorage> dbStorage = DatabaseStorage.findAll().list(); //.findByOwner(getUser());

        return Templates.htmx$instanceList(devInstances, dbStorage, null);
    }
//    @POST
//    public TemplateInstance done(@RestPath Long id) {
//        Todo todo = Todo.findById(id);
//        notFoundIfNull(todo);
//        if(todo.owner != getUser())
//            notFound();
//        todo.done = !todo.done;
//        if(todo.done)
//            todo.doneDate = new Date();
//        String message = i18n.formatMessage("todos.message.updated", todo.task);
//        // HTMX bug: https://github.com/bigskysoftware/htmx/issues/1043
////        	return concatTemplates(Templates.htmx$message(message), Templates.htmx$row(todo));
//        return Templates.htmx$row(todo);
//    }

    @POST
    public TemplateInstance add(@FormParam("name") @NotBlank String name,
                                @FormParam("description") @NotBlank String description,
                                @FormParam("databaseStorageID") @NotBlank String datbaseStorageID,
                                @FormParam("githubRepo") @NotBlank String githubRepo,
                                @FormParam("githubCommit") @NotBlank String githubCommit
    ){
        log.info("New devInstance: " );
        onlyHxRequest();
        if (validation.hasErrors()) {
        	return Templates.htmx$error("Cannot be empty: ");
        }

        //TODO in a tx
        DatabaseStorage storage = DatabaseStorage.findById(datbaseStorageID);
        DevInstance devInstance = new DevInstance();
        devInstance.name = name;
        devInstance.databaseStorage = storage;
        devInstance.githubCommit = githubCommit;
        devInstance.githubRepo = githubRepo;
        devInstance.description = description;

//        runnningDevInstance.databaseStorage = prodBackupStorage;
        devInstance.owner = getUser();
        devInstance.status = DevInstance.InstanceStatus.CREATED;

        devInstance.persist();

        bus.send("newDevInstance", devInstance.id);

//        try {
//            devInstanceManager.startDevInstance(devInstance.id);
//        } catch (InstanceManagementException e) {
//            return Templates.htmx$error("Cannot stop dev environment: "+ e.getMessage());
//        }

        return Templates.htmx$card(devInstance);
    }
}