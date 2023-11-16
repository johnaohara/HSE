package rest;

import io.quarkiverse.renarde.backoffice.BackofficeController;
import jakarta.annotation.security.RolesAllowed;
import model.DatabaseStorage;
import model.Todo;

@RolesAllowed("admin")
public class DatabaseStorageBackoffice extends BackofficeController<DatabaseStorage> {

}
