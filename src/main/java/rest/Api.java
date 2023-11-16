package rest;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import model.DatabaseStorage;
import model.DevInstance;

import java.util.List;

@Blocking
@Authenticated
@Path("/api")
public class Api {

    @GET
    @Produces("application/json")
    @Path("/devInstance")
    public List<DevInstance> getAllDevInstances() {
        return DevInstance.listAll();
    }

    @GET
    @Produces("application/json")
    @Path("/devInstance/{id}")
    public DevInstance getAllDevInstanceByID(@PathParam("id") Long id) {
        return DevInstance.findById(id);
    }

    @GET
    @Produces("application/json")
    @Path("/storage")
    public List<DatabaseStorage> getAllDatabaseStorage() {
        return DatabaseStorage.listAll();
    }

}
