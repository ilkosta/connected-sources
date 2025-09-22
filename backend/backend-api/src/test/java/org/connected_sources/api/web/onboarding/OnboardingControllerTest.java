package org.connected_sources.api.web.onboarding;

import org.connected_sources.api.async.onboarding.TestConfig;
import org.connected_sources.core.user.UserRepository;
import org.connected_sources.core.user.async.onboarding.OnboardingProvisioner;
import org.connected_sources.core.user.onboarding.model.OnboardingRequestCmd;
import org.connected_sources.core.user.onboarding.model.ProvisioningSpec;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.service.ContactInformationRepo;
import org.connected_sources.notification.service.NotificationDispatcher;
import org.connected_sources.notification.template.NotificationTemplate;
import org.connected_sources.notification.template.TemplateService;
import org.connected_sources.shared.context.TenantContext;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.logging.TenantLogger;
import org.connected_sources.shared.naming.TenantIdNormalizer;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.connected_sources.shared.tenantdb.DbProvider;
import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.connected_sources.tenant.spi.db.TenantResourcePlanner;
import org.connected_sources.tenantdb.SqliteTenantDbMigrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(controllers = OnboardingController.class)
@SpringBootTest
        (properties = {
        "backend.base-url=http://localhost:8080",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration-h2",
        "spring.flyway.baseline-on-migrate=true",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.validate-on-migrate=false"
        })
@AutoConfigureMockMvc           // create context with MockMvc
@AutoConfigureTestDatabase      // to instantiate H2 in test instead PG
@ActiveProfiles("test")         // to load application-test.yaml --> enable migrations,...
@Import({OnboardingControllerTest.TestLoggingConfig.class,
        SqliteTenantDbMigrator.class, TestConfig.class })
@ComponentScan(
        basePackages = {
                "org.connected_sources.api.web.onboarding",  // OnboardingController pkg
                "org.connected_sources.core.user.onboarding" // repos/services for onboarding
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//@TestPropertySource(properties = {
//        // force Flyway to pick only the H2 path
//        "spring.flyway.enabled=true",
//        "spring.flyway.locations=classpath:db/migration-h2",
//        "spring.flyway.baseline-on-migrate=true",
//        // make sure JPA doesn't try to generate/alter schema
//        "spring.jpa.hibernate.ddl-auto=none"
//})
class OnboardingControllerTest {

    @TestConfiguration
    static class TestLoggingConfig {
        @Bean
        @Primary
        TenantLogger testTenantLogger() {
            return (category, message, event, attrs) -> { /* no-op for tests */ };
        }
    }
    private MockMvc mvc;
//    @Autowired WebApplicationContext webApplicationContext;

    // mocks
    private OnboardingRepo repo;
    private NotificationDispatcher notifier;
    private OnboardingProvisioner provisioner;
    private TenantResourcePlanner  tenantResourcePlanner;
    private TenantDbMigrator tenantDbMigrator;
    private TenantContextHolder tenantContextHolder;
    TemplateService templateService;
    ContactInformationRepo contactRepo;
    UserRepository userRepository;

    private @Value("${tenant.base-directory}") Path baseDir;
    private @Value("${backend.base-url}") String baseUrl;

    @BeforeEach
    void setup() {
        // ctx
        TenantContextHolder.set(new TenantContext("t-sys", 42L, "corr-ctrl"));

        repo = mock(OnboardingRepo.class);
        notifier = mock(NotificationDispatcher.class);
        provisioner = mock(OnboardingProvisioner.class);
        tenantResourcePlanner = mock(TenantResourcePlanner.class);
        tenantDbMigrator = mock(TenantDbMigrator.class);
        tenantContextHolder = mock(TenantContextHolder.class);
        templateService = mock(TemplateService.class);
        contactRepo = mock(ContactInformationRepo.class);
        userRepository = mock(UserRepository.class);

        // instantiate the controller directly with mocks
        OnboardingController controller = new OnboardingController(
                repo, notifier, provisioner,tenantResourcePlanner,
                baseUrl, baseDir,"2",
                templateService,contactRepo, userRepository);

        // build standalone MockMvc (no application context)
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

    }

    @BeforeEach
    void stubPlanner() {
        when(tenantResourcePlanner.plan(anyString())).thenAnswer(inv -> {
            String norm = TenantIdNormalizer.normalize(inv.getArgument(0, String.class));
            Path base = Path.of("/home/costa/pkg/connected-sources/backend/data/tenants");
            Path root = base.resolve(norm);
            Path dbPath = root.resolve("datasource.sqlite");
            Map<String,Object> map = new HashMap<>();
            DataSourceDescriptor dsd = new DataSourceDescriptor(
                    DbProvider.SQLITE, dbPath.toString(), "", "", map
            );
            return new TenantResourcePlanner.TenantResourcesPlan(
                    norm, root.toString(),dbPath.toString() , dsd);
        });
    }

    ///  to print all mappings of the controller
//    @Autowired
//    private RequestMappingHandlerMapping handlerMapping;

//    @Test
//    void printAllEndpoints() {
//        handlerMapping.getHandlerMethods().forEach((key, value) ->
//                System.out.println(key + " -> " + value)
//        );
//    }

//    @Test
//    void printAllEndpoints() {
//        RequestMappingHandlerMapping mapping = webApplicationContext.getBean(RequestMappingHandlerMapping.class);
//        mapping.getHandlerMethods().forEach((key, value) ->
//                System.out.println(key + " -> " + value)
//        );
//    }

    @Autowired
    JdbcTemplate jdbc;

    @Test // if can load a table created by flyway from H2
    void flywayHasMigrated() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_NAME='ONBOARDING_REQUEST'", Integer.class);
        assertThat(n).isNotNull();
        assertThat(n).isGreaterThan(0);
    }

    @Test
    void request_returns202_andNotifiesCurator() throws Exception {
        when(repo.createOrReuseRequest(any(OnboardingRequestCmd.class), anyString()))
                .thenReturn(101L);

        String body = """
      {
        "producerName": "Acme SRL",
        "email": "owner@acme.test",
        "website": "https://acme.test",
        "vatOrFiscalCode": "IT1234567890"
      }
      """;

        mvc.perform(post("/onboarding/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.state").value("REQUESTED"));

        // curator notification
        verify(notifier).enqueue(eq(NotificationTemplate.ONBOARDING_REQUESTED), anyString(), eq(Channel.EMAIL),
                anyString(), anyString(), any(), eq(EventType.ONBOARDING_REQUESTED), eq(false));

        verify(repo).createOrReuseRequest(any(OnboardingRequestCmd.class), eq("corr-ctrl"));
    }

    @Test
    void approve_transitions_andEmailsProducer() throws Exception {
        mvc.perform(post("/onboarding/requests/{id}/approve", 101))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.state").value("APPROVED"));


        verify(repo).transitionState(eq(101L), eq(OnboardingState.APPROVED), eq(42L), anyMap());
        verify(notifier).enqueue(eq(NotificationTemplate.ONBOARDING_APPROVED), anyString(), eq(Channel.EMAIL),
                contains("Complete your registration"), contains("http"), any(), eq(EventType.ONBOARDING_ACCEPTED), eq(false));
    }

    @Test
    void register_persists_PREPARATION_andEnqueuesProvisioner() throws Exception {
        String body = """
      {
        "producerAdminEmail": "admin@acme.test",
        "tenantIdHint": "Acme SRL",
        "initialUsers": [42, 77]
      }
      """;

        mvc.perform(post("/onboarding/requests/{id}/register-producer", 101)
                        .param("token", "dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.state").value("PREPARATION"));

        // controller transitions state then creates tenant then enqueues provisioning
        verify(repo).transitionState(eq(101L), eq(OnboardingState.PREPARATION), isNull(), argThat(map -> map.containsKey("tenantId")));
        ArgumentCaptor<String> tenantIdCap = ArgumentCaptor.forClass(String.class);
        verify(provisioner).enqueueProvisioning(eq(101L), eq(tenantIdCap.getValue()), anyString(), any(ProvisioningSpec.class));
    }

    @Test
    void get_returnsState_or404() throws Exception {
        when(repo.currentState(101L)).thenReturn(java.util.Optional.of("REQUESTED"));
        mvc.perform(get("/onboarding/requests/{id}", 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REQUESTED"));

        when(repo.currentState(404L)).thenReturn(java.util.Optional.empty());
        mvc.perform(get("/onboarding/requests/{id}", 404))
                .andExpect(status().isNotFound());
    }
}
