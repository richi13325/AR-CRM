package com.ar.crm2.config;

import com.ar.crm2.adapter.out.persistence.ColumnaExistsFichasByColumnaIdAdapter;
import com.ar.crm2.adapter.out.persistence.ColumnaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.ColumnaRepository;
import com.ar.crm2.adapter.out.persistence.repository.ContactoRepository;
import com.ar.crm2.adapter.out.persistence.ContactoRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.EmpresaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.EmpresaRepository;
import com.ar.crm2.adapter.out.persistence.ExistsColumnaAsignadaAdapter;
import com.ar.crm2.adapter.out.persistence.ExistsFichasByColumnaIdAdapter;
import com.ar.crm2.adapter.out.persistence.repository.FichaRepository;
import com.ar.crm2.adapter.out.persistence.FichaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.RolRepository;
import com.ar.crm2.adapter.out.persistence.RolRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.SuperUsuarioRepository;
import com.ar.crm2.adapter.out.persistence.SuperUsuarioRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.TableroRepository;
import com.ar.crm2.adapter.out.persistence.TableroRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.TratoRepository;
import com.ar.crm2.adapter.out.persistence.TratoRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.TareaRepository;
import com.ar.crm2.adapter.out.persistence.TareaRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.repository.UsuarioRepository;
import com.ar.crm2.adapter.out.persistence.UsuarioRepositoryAdapter;
import com.ar.crm2.adapter.out.persistence.mapper.TableroMapper;
import com.ar.crm2.adapter.out.keycloak.KeycloakUserProvisioningAdapter;
import com.ar.crm2.config.KeycloakAdminProperties;
import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.columna.port.in.CreateColumnaUseCase;
import com.ar.crm2.application.columna.port.in.DeleteColumnaUseCase;
import com.ar.crm2.application.columna.port.in.EditColumnaUseCase;
import com.ar.crm2.application.columna.port.in.GetAllColumnasUseCase;
import com.ar.crm2.application.columna.port.in.GetColumnaByIdUseCase;
import com.ar.crm2.application.columna.port.out.DeleteColumnaByIdPort;
import com.ar.crm2.application.columna.port.out.ExistsColumnaAsignadaPort;
import com.ar.crm2.application.columna.port.out.FindAllColumnasPort;
import com.ar.crm2.application.columna.port.out.FindColumnaByIdPort;
import com.ar.crm2.application.columna.port.out.SaveColumnaPort;
import com.ar.crm2.application.columna.service.CreateColumnaService;
import com.ar.crm2.application.columna.service.DeleteColumnaService;
import com.ar.crm2.application.columna.service.EditColumnaService;
import com.ar.crm2.application.columna.service.GetAllColumnasService;
import com.ar.crm2.application.columna.service.GetColumnaByIdService;
import com.ar.crm2.application.superusuario.port.in.CreateSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.DeleteSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.EditSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.in.GetAllSuperUsuariosUseCase;
import com.ar.crm2.application.superusuario.port.in.GetSuperUsuarioByIdUseCase;
import com.ar.crm2.application.superusuario.port.out.DeleteSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindAllSuperUsuariosPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByKeycloakIdPort;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.application.superusuario.service.CreateSuperUsuarioService;
import com.ar.crm2.application.superusuario.service.DeleteSuperUsuarioService;
import com.ar.crm2.application.superusuario.service.EditSuperUsuarioService;
import com.ar.crm2.application.superusuario.service.GetAllSuperUsuariosService;
import com.ar.crm2.application.superusuario.service.GetSuperUsuarioByIdService;
import com.ar.crm2.application.rol.port.in.CreateRolUseCase;
import com.ar.crm2.application.rol.port.in.DeleteRolUseCase;
import com.ar.crm2.application.rol.port.in.EditRolUseCase;
import com.ar.crm2.application.rol.port.in.GetAllRolesUseCase;
import com.ar.crm2.application.rol.port.in.GetRolByIdUseCase;
import com.ar.crm2.application.rol.port.out.DeleteRolByIdPort;
import com.ar.crm2.application.rol.port.out.ExistsUsuariosByRolIdPort;
import com.ar.crm2.application.rol.port.out.FindAllRolesPort;
import com.ar.crm2.application.rol.port.out.FindRolByIdPort;
import com.ar.crm2.application.rol.port.out.SaveRolPort;
import com.ar.crm2.application.rol.service.CreateRolService;
import com.ar.crm2.application.rol.service.DeleteRolService;
import com.ar.crm2.application.rol.service.EditRolService;
import com.ar.crm2.application.rol.service.GetAllRolesService;
import com.ar.crm2.application.rol.service.GetRolByIdService;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.DeleteUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.EditUsuarioUseCase;
import com.ar.crm2.application.usuario.port.in.GetAllUsuariosUseCase;
import com.ar.crm2.application.usuario.port.in.GetUsuarioByIdUseCase;
import com.ar.crm2.application.usuario.port.out.DeleteUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.FindAllUsuariosPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByKeycloakIdPort;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.application.usuario.service.CreateUsuarioService;
import com.ar.crm2.application.usuario.service.DeleteUsuarioService;
import com.ar.crm2.application.usuario.service.EditUsuarioService;
import com.ar.crm2.application.usuario.service.GetAllUsuariosService;
import com.ar.crm2.application.usuario.service.GetUsuarioByIdService;
import com.ar.crm2.application.contacto.port.in.CreateContactoUseCase;
import com.ar.crm2.application.contacto.port.in.DeleteContactoUseCase;
import com.ar.crm2.application.contacto.port.in.EditContactoUseCase;
import com.ar.crm2.application.contacto.port.in.GetAllContactosUseCase;
import com.ar.crm2.application.contacto.port.in.GetContactoByIdUseCase;
import com.ar.crm2.application.contacto.port.out.DeleteContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.ExistsTratosByContactoIdPort;
import com.ar.crm2.application.contacto.port.out.FindAllContactosPort;
import com.ar.crm2.application.contacto.port.out.FindContactoByIdPort;
import com.ar.crm2.application.contacto.port.out.SaveContactoPort;
import com.ar.crm2.application.contacto.service.CreateContactoService;
import com.ar.crm2.application.contacto.service.DeleteContactoService;
import com.ar.crm2.application.contacto.service.EditContactoService;
import com.ar.crm2.application.contacto.service.GetAllContactosService;
import com.ar.crm2.application.contacto.service.GetContactoByIdService;
import com.ar.crm2.application.empresa.port.in.CreateEmpresaUseCase;
import com.ar.crm2.application.empresa.port.in.DeleteEmpresaUseCase;
import com.ar.crm2.application.empresa.port.in.EditEmpresaUseCase;
import com.ar.crm2.application.empresa.port.in.GetAllEmpresasUseCase;
import com.ar.crm2.application.empresa.port.out.DeleteEmpresaByIdPort;
import com.ar.crm2.application.empresa.port.out.ExistsTratosByEmpresaIdPort;
import com.ar.crm2.application.empresa.port.out.FindAllEmpresasPort;
import com.ar.crm2.application.empresa.port.out.FindEmpresaByIdPort;
import com.ar.crm2.application.empresa.port.out.SaveEmpresaPort;
import com.ar.crm2.application.empresa.service.CreateEmpresaService;
import com.ar.crm2.application.empresa.service.DeleteEmpresaService;
import com.ar.crm2.application.empresa.service.EditEmpresaService;
import com.ar.crm2.application.empresa.service.GetAllEmpresasService;
import com.ar.crm2.application.trato.port.in.CreateTratoUseCase;
import com.ar.crm2.application.trato.port.in.DeleteTratoUseCase;
import com.ar.crm2.application.trato.port.in.EditTratoUseCase;
import com.ar.crm2.application.trato.port.in.GetAllTratosUseCase;
import com.ar.crm2.application.trato.port.in.GetTratoByIdUseCase;
import com.ar.crm2.application.trato.port.out.DeleteTratoByIdPort;
import com.ar.crm2.application.trato.port.out.FindAllTratosPort;
import com.ar.crm2.application.trato.port.out.FindTratoByIdPort;
import com.ar.crm2.application.trato.port.out.SaveTratoPort;
import com.ar.crm2.application.trato.service.CreateTratoService;
import com.ar.crm2.application.trato.service.DeleteTratoService;
import com.ar.crm2.application.trato.service.EditTratoService;
import com.ar.crm2.application.trato.service.GetAllTratosService;
import com.ar.crm2.application.trato.service.GetTratoByIdService;
import com.ar.crm2.application.tarea.port.in.CreateTareaUseCase;
import com.ar.crm2.application.tarea.port.in.DeleteTareaUseCase;
import com.ar.crm2.application.tarea.port.in.EditTareaUseCase;
import com.ar.crm2.application.tarea.port.in.GetAllTareasUseCase;
import com.ar.crm2.application.tarea.port.in.GetTareaByIdUseCase;
import com.ar.crm2.application.tarea.port.out.DeleteTareaByIdPort;
import com.ar.crm2.application.tarea.port.out.FindAllTareasPort;
import com.ar.crm2.application.tarea.port.out.FindTareaByIdPort;
import com.ar.crm2.application.tarea.port.out.SaveTareaPort;
import com.ar.crm2.application.tarea.service.CreateTareaService;
import com.ar.crm2.application.tarea.service.DeleteTareaService;
import com.ar.crm2.application.tarea.service.EditTareaService;
import com.ar.crm2.application.tarea.service.GetAllTareasService;
import com.ar.crm2.application.tarea.service.GetTareaByIdService;
import com.ar.crm2.application.ficha.port.in.CreateFichaUseCase;
import com.ar.crm2.application.ficha.port.in.DeleteFichaUseCase;
import com.ar.crm2.application.ficha.port.in.EditFichaUseCase;
import com.ar.crm2.application.ficha.port.in.GetAllFichasUseCase;
import com.ar.crm2.application.ficha.port.in.GetFichaByIdUseCase;
import com.ar.crm2.application.ficha.port.out.DeleteFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.FindAllFichasPort;
import com.ar.crm2.application.ficha.port.out.FindFichaByIdPort;
import com.ar.crm2.application.ficha.port.out.SaveFichaPort;
import com.ar.crm2.application.ficha.service.CreateFichaService;
import com.ar.crm2.application.ficha.service.DeleteFichaService;
import com.ar.crm2.application.ficha.service.EditFichaService;
import com.ar.crm2.application.ficha.service.GetAllFichasService;
import com.ar.crm2.application.ficha.service.GetFichaByIdService;
import com.ar.crm2.application.tablero.port.in.AgregarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.AsignarColumnaTableroUseCase;
import com.ar.crm2.application.tablero.port.in.CreateTableroUseCase;
import com.ar.crm2.application.tablero.port.in.DeleteTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EditTableroUseCase;
import com.ar.crm2.application.tablero.port.in.EliminarColumnaDelTableroUseCase;
import com.ar.crm2.application.tablero.port.in.GetAllTablerosUseCase;
import com.ar.crm2.application.tablero.port.in.GetTableroByIdUseCase;
import com.ar.crm2.application.tablero.port.in.ReordenarColumnasUseCase;
import com.ar.crm2.application.tablero.port.out.DeleteTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.ExistsFichasByColumnaIdPort;
import com.ar.crm2.application.tablero.port.out.FindAllTablerosPort;
import com.ar.crm2.application.tablero.port.out.FindTableroByIdPort;
import com.ar.crm2.application.tablero.port.out.SaveTableroPort;
import com.ar.crm2.application.tablero.port.out.ExistsColumnaEnTableroPort;
import com.ar.crm2.application.tablero.service.AgregarColumnaTableroService;
import com.ar.crm2.application.tablero.service.AsignarColumnaTableroService;
import com.ar.crm2.application.tablero.service.CreateTableroService;
import com.ar.crm2.application.tablero.service.DeleteTableroService;
import com.ar.crm2.application.tablero.service.EditTableroService;
import com.ar.crm2.application.tablero.service.EliminarColumnaDelTableroService;
import com.ar.crm2.application.tablero.service.GetAllTablerosService;
import com.ar.crm2.application.tablero.service.GetTableroByIdService;
import com.ar.crm2.application.tablero.service.ReordenarColumnasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Application wiring configuration.
 * Wires application UseCases with infrastructure adapters without touching application/domain classes.
 *
 * Dependency flow: boot -> infrastructure -> application -> domain
 * This configuration lives in boot because boot is the composition root and can import
 * infrastructure adapters plus application services without contaminating inner layers.
 */
@Configuration
public class WiringConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // ── Infrastructure Adapter Beans (created explicitly to avoid component-scan duplication) ──

    @Bean
    public EmpresaRepositoryAdapter empresaRepositoryAdapter(EmpresaRepository repository) {
        return new EmpresaRepositoryAdapter(repository);
    }

    @Bean
    public ContactoRepositoryAdapter contactoRepositoryAdapter(ContactoRepository repository) {
        return new ContactoRepositoryAdapter(repository);
    }

    @Bean
    public TableroRepositoryAdapter tableroRepositoryAdapter(
            TableroRepository tableroRepository,
            ColumnaRepository columnaRepository,
            TableroMapper tableroMapper
    ) {
        return new TableroRepositoryAdapter(tableroRepository, columnaRepository, tableroMapper);
    }

    @Bean
    public TratoRepositoryAdapter tratoRepositoryAdapter(TratoRepository repository) {
        return new TratoRepositoryAdapter(repository);
    }

    @Bean
    public TareaRepositoryAdapter tareaRepositoryAdapter(TareaRepository repository) {
        return new TareaRepositoryAdapter(repository);
    }

    @Bean
    public FichaRepositoryAdapter fichaRepositoryAdapter(FichaRepository repository) {
        return new FichaRepositoryAdapter(repository);
    }

    @Bean
    public RolRepositoryAdapter rolRepositoryAdapter(RolRepository repository) {
        return new RolRepositoryAdapter(repository);
    }

    @Bean
    public ColumnaRepositoryAdapter columnaRepositoryAdapter(ColumnaRepository repository) {
        return new ColumnaRepositoryAdapter(repository);
    }

    @Bean
    public UsuarioRepositoryAdapter usuarioRepositoryAdapter(UsuarioRepository repository) {
        return new UsuarioRepositoryAdapter(repository);
    }

    @Bean
    public SuperUsuarioRepositoryAdapter superUsuarioRepositoryAdapter(SuperUsuarioRepository repository) {
        return new SuperUsuarioRepositoryAdapter(repository);
    }

    @Bean
    public ExistsFichasByColumnaIdAdapter existsFichasByColumnaIdAdapter(FichaRepository fichaRepository) {
        return new ExistsFichasByColumnaIdAdapter(fichaRepository);
    }

    @Bean
    public ColumnaExistsFichasByColumnaIdAdapter columnaExistsFichasByColumnaIdAdapter(FichaRepository fichaRepository) {
        return new ColumnaExistsFichasByColumnaIdAdapter(fichaRepository);
    }

    @Bean
    public ExistsColumnaAsignadaAdapter existsColumnaAsignadaAdapter(TableroRepository tableroRepository) {
        return new ExistsColumnaAsignadaAdapter(tableroRepository);
    }

    @Bean
    public KeycloakUserProvisioningAdapter keycloakUserProvisioningAdapter(KeycloakAdminProperties props) {
        return new KeycloakUserProvisioningAdapter(props);
    }

    // ── Empresa UseCase Beans ──

    @Bean
    public CreateEmpresaUseCase createEmpresaUseCase(SaveEmpresaPort savePort) {
        return new CreateEmpresaService(savePort);
    }

    @Bean
    public GetAllEmpresasUseCase getAllEmpresasUseCase(FindAllEmpresasPort findAllPort) {
        return new GetAllEmpresasService(findAllPort);
    }

    @Bean
    public EditEmpresaUseCase editEmpresaUseCase(FindEmpresaByIdPort findPort, SaveEmpresaPort savePort) {
        return new EditEmpresaService(findPort, savePort);
    }

    @Bean
    public DeleteEmpresaUseCase deleteEmpresaUseCase(
            FindEmpresaByIdPort findPort,
            ExistsTratosByEmpresaIdPort existsTratosPort,
            DeleteEmpresaByIdPort deletePort
    ) {
        return new DeleteEmpresaService(findPort, existsTratosPort, deletePort);
    }

    // ── Empresa Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveEmpresaPort saveEmpresaPort(EmpresaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllEmpresasPort findAllEmpresasPort(EmpresaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindEmpresaByIdPort findEmpresaByIdPort(EmpresaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteEmpresaByIdPort deleteEmpresaByIdPort(EmpresaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsTratosByEmpresaIdPort existsTratosByEmpresaIdPort(EmpresaRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Contacto UseCase Beans ──

    @Bean
    public CreateContactoUseCase createContactoUseCase(SaveContactoPort savePort) {
        return new CreateContactoService(savePort);
    }

    @Bean
    public GetAllContactosUseCase getAllContactosUseCase(FindAllContactosPort findAllPort) {
        return new GetAllContactosService(findAllPort);
    }

    @Bean
    public GetContactoByIdUseCase getContactoByIdUseCase(FindContactoByIdPort findPort) {
        return new GetContactoByIdService(findPort);
    }

    @Bean
    public EditContactoUseCase editContactoUseCase(FindContactoByIdPort findPort, SaveContactoPort savePort) {
        return new EditContactoService(findPort, savePort);
    }

    @Bean
    public DeleteContactoUseCase deleteContactoUseCase(
            FindContactoByIdPort findPort,
            ExistsTratosByContactoIdPort existsTratosPort,
            DeleteContactoByIdPort deletePort
    ) {
        return new DeleteContactoService(findPort, existsTratosPort, deletePort);
    }

    // ── Contacto Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveContactoPort saveContactoPort(ContactoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllContactosPort findAllContactosPort(ContactoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindContactoByIdPort findContactoByIdPort(ContactoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteContactoByIdPort deleteContactoByIdPort(ContactoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsTratosByContactoIdPort existsTratosByContactoIdPort(ContactoRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Tablero Mapper Bean ──

    /**
     * Stateful mapper for Tablero aggregate ↔ entities.
     *
     * <p>Requires {@link ColumnaRepository} to hydrate catalog column data when
     * reading {@link com.ar.crm2.model.entity.ColumnaTablero} from the relation entity.
     */
    @Bean
    public TableroMapper tableroMapper(ColumnaRepository columnaRepository) {
        return new TableroMapper(columnaRepository);
    }

    // ── Tablero Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveTableroPort saveTableroPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllTablerosPort findAllTablerosPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindTableroByIdPort findTableroByIdPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteTableroByIdPort deleteTableroByIdPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsFichasByColumnaIdPort existsFichasByColumnaIdPort(ExistsFichasByColumnaIdAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsColumnaEnTableroPort existsColumnaEnTableroPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public com.ar.crm2.application.tablero.port.out.FindColumnaByIdPort tableroFindColumnaByIdPort(TableroRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Tablero UseCase Beans ──

    @Bean
    public CreateTableroUseCase createTableroUseCase(SaveTableroPort savePort) {
        return new CreateTableroService(savePort);
    }

    @Bean
    public GetAllTablerosUseCase getAllTablerosUseCase(FindAllTablerosPort findAllPort) {
        return new GetAllTablerosService(findAllPort);
    }

    @Bean
    public GetTableroByIdUseCase getTableroByIdUseCase(FindTableroByIdPort findPort) {
        return new GetTableroByIdService(findPort);
    }

    @Bean
    public EditTableroUseCase editTableroUseCase(FindTableroByIdPort findPort, SaveTableroPort savePort) {
        return new EditTableroService(findPort, savePort);
    }

    @Bean
    public DeleteTableroUseCase deleteTableroUseCase(
            FindTableroByIdPort findPort,
            DeleteTableroByIdPort deletePort
    ) {
        return new DeleteTableroService(findPort, deletePort);
    }

    @Bean
    public AgregarColumnaTableroUseCase agregarColumnaTableroUseCase(
            FindTableroByIdPort findPort,
            SaveTableroPort savePort
    ) {
        return new AgregarColumnaTableroService(findPort, savePort);
    }

    @Bean
    public AsignarColumnaTableroUseCase asignarColumnaTableroUseCase(
            FindTableroByIdPort findTableroPort,
            com.ar.crm2.application.tablero.port.out.FindColumnaByIdPort findColumnaPort,
            ExistsColumnaEnTableroPort existsColumnaEnTableroPort,
            SaveTableroPort savePort
    ) {
        return new AsignarColumnaTableroService(findTableroPort, findColumnaPort, existsColumnaEnTableroPort, savePort);
    }

    @Bean
    public EliminarColumnaDelTableroUseCase eliminarColumnaDelTableroUseCase(
            FindTableroByIdPort findPort,
            ExistsFichasByColumnaIdPort existsFichasPort,
            SaveTableroPort savePort
    ) {
        return new EliminarColumnaDelTableroService(findPort, existsFichasPort, savePort);
    }

    @Bean
    public ReordenarColumnasUseCase reordenarColumnasUseCase(
            FindTableroByIdPort findPort,
            SaveTableroPort savePort
    ) {
        return new ReordenarColumnasService(findPort, savePort);
    }

    // ── Trato Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveTratoPort saveTratoPort(TratoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllTratosPort findAllTratosPort(TratoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindTratoByIdPort findTratoByIdPort(TratoRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteTratoByIdPort deleteTratoByIdPort(TratoRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Trato UseCase Beans ──

    @Bean
    public CreateTratoUseCase createTratoUseCase(SaveTratoPort savePort) {
        return new CreateTratoService(savePort);
    }

    @Bean
    public GetAllTratosUseCase getAllTratosUseCase(FindAllTratosPort findAllPort) {
        return new GetAllTratosService(findAllPort);
    }

    @Bean
    public GetTratoByIdUseCase getTratoByIdUseCase(FindTratoByIdPort findPort) {
        return new GetTratoByIdService(findPort);
    }

    @Bean
    public EditTratoUseCase editTratoUseCase(FindTratoByIdPort findPort, SaveTratoPort savePort) {
        return new EditTratoService(findPort, savePort);
    }

    @Bean
    public DeleteTratoUseCase deleteTratoUseCase(
            FindTratoByIdPort findPort,
            DeleteTratoByIdPort deletePort
    ) {
        return new DeleteTratoService(findPort, deletePort);
    }

    // ── Tarea Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveTareaPort saveTareaPort(TareaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllTareasPort findAllTareasPort(TareaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindTareaByIdPort findTareaByIdPort(TareaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteTareaByIdPort deleteTareaByIdPort(TareaRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Tarea UseCase Beans ──

    @Bean
    public CreateTareaUseCase createTareaUseCase(SaveTareaPort savePort) {
        return new CreateTareaService(savePort);
    }

    @Bean
    public GetAllTareasUseCase getAllTareasUseCase(FindAllTareasPort findAllPort) {
        return new GetAllTareasService(findAllPort);
    }

    @Bean
    public GetTareaByIdUseCase getTareaByIdUseCase(FindTareaByIdPort findPort) {
        return new GetTareaByIdService(findPort);
    }

    @Bean
    public EditTareaUseCase editTareaUseCase(FindTareaByIdPort findPort, SaveTareaPort savePort) {
        return new EditTareaService(findPort, savePort);
    }

    @Bean
    public DeleteTareaUseCase deleteTareaUseCase(
            FindTareaByIdPort findPort,
            DeleteTareaByIdPort deletePort
    ) {
        return new DeleteTareaService(findPort, deletePort);
    }

    // ── Ficha Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveFichaPort saveFichaPort(FichaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllFichasPort findAllFichasPort(FichaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindFichaByIdPort findFichaByIdPort(FichaRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteFichaByIdPort deleteFichaByIdPort(FichaRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Ficha UseCase Beans ──

    @Bean
    public CreateFichaUseCase createFichaUseCase(SaveFichaPort savePort) {
        return new CreateFichaService(savePort);
    }

    @Bean
    public GetAllFichasUseCase getAllFichasUseCase(FindAllFichasPort findAllPort) {
        return new GetAllFichasService(findAllPort);
    }

    @Bean
    public GetFichaByIdUseCase getFichaByIdUseCase(FindFichaByIdPort findPort) {
        return new GetFichaByIdService(findPort);
    }

    @Bean
    public EditFichaUseCase editFichaUseCase(FindFichaByIdPort findPort, SaveFichaPort savePort) {
        return new EditFichaService(findPort, savePort);
    }

    @Bean
    public DeleteFichaUseCase deleteFichaUseCase(
            FindFichaByIdPort findPort,
            DeleteFichaByIdPort deletePort
    ) {
        return new DeleteFichaService(findPort, deletePort);
    }

    // ── Identity Provider Adapter Bean ──

    @Bean
    public IdentityProviderUserPort identityProviderUserPort(KeycloakUserProvisioningAdapter adapter) {
        return adapter;
    }

    // ── Usuario Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveUsuarioPort saveUsuarioPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllUsuariosPort findAllUsuariosPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindUsuarioByIdPort findUsuarioByIdPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteUsuarioByIdPort deleteUsuarioByIdPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindUsuarioByKeycloakIdPort findUsuarioByKeycloakIdPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Usuario UseCase Beans ──

    @Bean
    public CreateUsuarioUseCase createUsuarioUseCase(SaveUsuarioPort savePort, IdentityProviderUserPort identityPort) {
        return new CreateUsuarioService(savePort, identityPort);
    }

    @Bean
    public GetAllUsuariosUseCase getAllUsuariosUseCase(FindAllUsuariosPort findAllPort) {
        return new GetAllUsuariosService(findAllPort);
    }

    @Bean
    public GetUsuarioByIdUseCase getUsuarioByIdUseCase(FindUsuarioByIdPort findPort) {
        return new GetUsuarioByIdService(findPort);
    }

    @Bean
    public EditUsuarioUseCase editUsuarioUseCase(FindUsuarioByIdPort findPort, SaveUsuarioPort savePort, IdentityProviderUserPort identityPort) {
        return new EditUsuarioService(findPort, savePort, identityPort);
    }

    @Bean
    public DeleteUsuarioUseCase deleteUsuarioUseCase(
            FindUsuarioByIdPort findPort,
            DeleteUsuarioByIdPort deletePort,
            IdentityProviderUserPort identityPort
    ) {
        return new DeleteUsuarioService(findPort, deletePort, identityPort);
    }

    // ── Rol Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveRolPort saveRolPort(RolRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllRolesPort findAllRolesPort(RolRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindRolByIdPort findRolByIdPort(RolRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteRolByIdPort deleteRolByIdPort(RolRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsUsuariosByRolIdPort existsUsuariosByRolIdPort(UsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    // ── Rol UseCase Beans ──

    @Bean
    public CreateRolUseCase createRolUseCase(SaveRolPort savePort) {
        return new CreateRolService(savePort);
    }

    @Bean
    public GetAllRolesUseCase getAllRolesUseCase(FindAllRolesPort findAllPort) {
        return new GetAllRolesService(findAllPort);
    }

    @Bean
    public GetRolByIdUseCase getRolByIdUseCase(FindRolByIdPort findPort) {
        return new GetRolByIdService(findPort);
    }

    @Bean
    public EditRolUseCase editRolUseCase(FindRolByIdPort findPort, SaveRolPort savePort) {
        return new EditRolService(findPort, savePort);
    }

    @Bean
    public DeleteRolUseCase deleteRolUseCase(
            FindRolByIdPort findPort,
            ExistsUsuariosByRolIdPort existsUsuariosPort,
            DeleteRolByIdPort deletePort
    ) {
        return new DeleteRolService(findPort, existsUsuariosPort, deletePort);
    }

    // ── SuperUsuario Adapter Beans (type-level narrowing) ──

    @Bean
    public SaveSuperUsuarioPort saveSuperUsuarioPort(SuperUsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindAllSuperUsuariosPort findAllSuperUsuariosPort(SuperUsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindSuperUsuarioByIdPort findSuperUsuarioByIdPort(SuperUsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public DeleteSuperUsuarioByIdPort deleteSuperUsuarioByIdPort(SuperUsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public FindSuperUsuarioByKeycloakIdPort findSuperUsuarioByKeycloakIdPort(SuperUsuarioRepositoryAdapter adapter) {
        return adapter;
    }

    // ── SuperUsuario UseCase Beans ──

    @Bean
    public CreateSuperUsuarioUseCase createSuperUsuarioUseCase(SaveSuperUsuarioPort savePort, IdentityProviderUserPort identityPort) {
        return new CreateSuperUsuarioService(savePort, identityPort);
    }

    @Bean
    public GetAllSuperUsuariosUseCase getAllSuperUsuariosUseCase(FindAllSuperUsuariosPort findAllPort) {
        return new GetAllSuperUsuariosService(findAllPort);
    }

    @Bean
    public GetSuperUsuarioByIdUseCase getSuperUsuarioByIdUseCase(FindSuperUsuarioByIdPort findPort) {
        return new GetSuperUsuarioByIdService(findPort);
    }

    @Bean
    public EditSuperUsuarioUseCase editSuperUsuarioUseCase(FindSuperUsuarioByIdPort findPort, SaveSuperUsuarioPort savePort, IdentityProviderUserPort identityPort) {
        return new EditSuperUsuarioService(findPort, savePort, identityPort);
    }

    @Bean
    public DeleteSuperUsuarioUseCase deleteSuperUsuarioUseCase(
            FindSuperUsuarioByIdPort findPort,
            DeleteSuperUsuarioByIdPort deletePort,
            IdentityProviderUserPort identityPort
    ) {
        return new DeleteSuperUsuarioService(findPort, deletePort, identityPort);
    }

    // ── Columna Adapter Beans (type-level narrowing) ──

    @Bean
    public com.ar.crm2.application.columna.port.out.ExistsFichasByColumnaIdPort columnaExistsFichasByColumnaIdPort(ColumnaExistsFichasByColumnaIdAdapter adapter) {
        return adapter;
    }

    @Bean
    public ExistsColumnaAsignadaPort existsColumnaAsignadaPort(ExistsColumnaAsignadaAdapter adapter) {
        return adapter;
    }

    // ── Columna UseCase Beans ──

    @Bean
    public CreateColumnaUseCase createColumnaUseCase(
            ColumnaRepositoryAdapter savePort,
            ColumnaRepositoryAdapter findAllPort
    ) {
        return new CreateColumnaService(savePort, findAllPort);
    }

    @Bean
    public GetAllColumnasUseCase getAllColumnasUseCase(ColumnaRepositoryAdapter findAllPort) {
        return new GetAllColumnasService(findAllPort);
    }

    @Bean
    public GetColumnaByIdUseCase getColumnaByIdUseCase(ColumnaRepositoryAdapter findPort) {
        return new GetColumnaByIdService(findPort);
    }

    @Bean
    public EditColumnaUseCase editColumnaUseCase(
            ColumnaRepositoryAdapter findPort,
            ColumnaRepositoryAdapter findAllPort,
            ColumnaRepositoryAdapter savePort
    ) {
        return new EditColumnaService(findPort, findAllPort, savePort);
    }

    @Bean
    public DeleteColumnaUseCase deleteColumnaUseCase(
            ColumnaRepositoryAdapter findPort,
            ExistsColumnaAsignadaPort existsColumnaAsignadaPort,
            com.ar.crm2.application.columna.port.out.ExistsFichasByColumnaIdPort columnaExistsFichasByColumnaIdPort,
            ColumnaRepositoryAdapter deletePort
    ) {
        return new DeleteColumnaService(findPort, existsColumnaAsignadaPort, columnaExistsFichasByColumnaIdPort, deletePort);
    }

}
