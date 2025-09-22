//package org.connected_sources.core.user;
//
//import org.connected_sources.shared.*;
//import org.connected_sources.tenant.spi.TenantLifecycleManager;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class ProducerServiceTest {
//
//    @Mock
//    private TenantLifecycleManager tenantManager;
//    @Mock
//    private NotificationService notificationService;
//    @InjectMocks
//    private ProducerService producerService;
//
//    @Test
//    void testRegisterAndFetchProducer() {
//        final String email = "email";
//        final Long id = 100L;
//        final String lHQ = "sede legale";
//        final String username = "pippo";
//        final String userEmail = "pippo@gmail.com";
//        final String producerName = "producer name";
//        ProducerRegistration r = producerService.register(username, userEmail, producerName, email,lHQ);
//
//        assertFalse(producerService.isRegistered(r.getProducerId()));
//        Producer p = producerService.getProducer(r.getProducerId());
//        assertNull(p);
//
//        User u = new User();
//        u.setId(id);
//        u.setEmail(email);
//        producerService.completeRegistration(r.getProducerId(),r.getRegistrationId(), u);
//        assertTrue(producerService.isRegistered(r.getProducerId()));
//
//        p = producerService.getProducer(r.getProducerId());
//        assertNotNull(p);
//        assertEquals(email, p.getInstitutionalEmail());
//    }
//
//    @Test
//    void testTeamCreation() {
//        String pid = "p1";
//        String tid = "tid";
//        Long uid1 = 100L;
//        Long uid2 = 200L;
//        String u1email = "email1";
//        String u2email = "email2";
//
//        TeamService teamService = new TeamService();
//        teamService.createTeam(tid, pid);
//        User u = new User();
//        u.setId(uid1); u.setEmail(u1email);
//        teamService.addMember(pid, tid, u, UserRole.MANAGER);
//        u = new User();
//        u.setId(uid2);u.setEmail(u2email);
//        teamService.addMember(pid,tid, u, UserRole.MEMBER);
//        Set<TeamMember> members = teamService.getMembers(pid,tid);
//        assertEquals(2, members.size());
//        Assertions.assertTrue(
//                members.stream().anyMatch(m -> m.getUser().getId().equals(uid1))
//                             );
//        Assertions.assertTrue(
//                members.stream().anyMatch(m -> m.getUser().getId().equals(uid2))
//                             );
//    }
//
//    @Test
//    void testRegisterProducerCreatesTenantAndStoresProducer() {
//
//        final Long id = 101L;
//        final String username = "pippo", userEmail = "pippo@gmail.com";
//
//        final String name = "producere1", email = "producer1 email";
//        final String lhq = "sede legale";
//
//        ProducerRegistration r = producerService.register(username,userEmail,name, email, lhq);
//        assertFalse(producerService.isRegistered(r.getProducerId()));
//        Producer p = producerService.getProducer(r.getProducerId());
//        assertNull(p);
//
//        User u = new User();
//        u.setId(id);u.setEmail(email);
//        producerService.completeRegistration(r.getProducerId(),r.getRegistrationId(), u);
//        assertTrue(producerService.isRegistered(r.getProducerId()));
//
//        p = producerService.getProducer(r.getProducerId());
//        assertNotNull(p);
//        assertEquals(name, p.getName());
//        verify(tenantManager).provisionTenant(r.getProducerId());
//    }
//
//    @Test
//    void testCompleteRegistrationStoresManager() {
//
//        final String name = "pippo";
//        final String email = "email";
//        final String hq = "sede legale";
//
//        final Long u1 = 1L;
//        final String username = "u1";
//        final String u1email = "email1";
//
//        ProducerRegistration r = producerService.register(username, u1email, name,email,hq);
//        User u = new User(); u.setId(u1); u.setEmail(u1email);
//        producerService.completeRegistration(r.getProducerId(), r.getRegistrationId(), u);
//        assertEquals(u, producerService.getManager(u1));
//    }
//}
