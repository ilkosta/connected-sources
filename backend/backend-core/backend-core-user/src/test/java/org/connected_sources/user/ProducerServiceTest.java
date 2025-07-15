package org.connected_sources.user;

import org.connected_sources.shared.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProducerServiceTest {

    @Mock
    private TenantLifecycleManager tenantManager;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ProducerService producerService;

    @Test
    public void testRegisterAndFetchProducer() {
        final String email = "email";
        final String name = "pippo";
        final String pid = "p1";
        final String lHQ = "sede legale";
        ProducerRegistration r = producerService.register(name, email,lHQ);

        assertFalse(producerService.isRegistered(r.getProducerId()));
        Producer p = producerService.getProducer(r.getProducerId());
        assertNull(p);

        User u = new User(name,email);
        producerService.completeRegistration(r.getProducerId(),r.getRegistrationId(), u);
        assertTrue(producerService.isRegistered(r.getProducerId()));

        p = producerService.getProducer(r.getProducerId());
        assertNotNull(p);
        assertEquals(name, p.getName());
        assertEquals(email, p.getInstitutionalEmail());
    }

    @Test
    public void testTeamCreation() {
        String pid = "p1";
        String tid = "tid";
        String uid1 = "user1";
        String uid2 = "user2";
        String u1email = "email1";
        String u2email = "email2";

        TeamService teamService = new TeamService();
        teamService.createTeam(tid, pid);
        User u = new User(uid1, u1email);
        teamService.addMember(pid, tid, u, UserRole.MANAGER);
        u = new User(uid2, u2email);
        teamService.addMember(pid,tid, u, UserRole.MEMBER);
        Set<TeamMember> members = teamService.getMembers(pid,tid);
        assertEquals(2, members.size());
        Assertions.assertTrue(
                members.stream().anyMatch(m -> m.getUser().getId().equals(uid1))
                             );
        Assertions.assertTrue(
                members.stream().anyMatch(m -> m.getUser().getId().equals(uid2))
                             );
    }

    @Test
    public void testRegisterProducerCreatesTenantAndStoresProducer() {

        final String name = "pippo";
        final String email = "email";
        final String lhq = "sede legale";

        ProducerRegistration r = producerService.register(name,email, lhq);
        assertFalse(producerService.isRegistered(r.getProducerId()));
        Producer p = producerService.getProducer(r.getProducerId());
        assertNull(p);

        User u = new User(name,email);
        producerService.completeRegistration(r.getProducerId(),r.getRegistrationId(), u);
        assertTrue(producerService.isRegistered(r.getProducerId()));

        p = producerService.getProducer(r.getProducerId());
        assertNotNull(p);
        assertEquals(name, p.getName());
        verify(tenantManager).provisionTenant(r.getProducerId());
    }

    @Test
    public void testCompleteRegistrationStoresManager() {

        final String name = "pippo";
        final String email = "email";
        final String hq = "sede legale";

        final String u1 = "user1";
        final String u1email = "email1";

        ProducerRegistration r = producerService.register(name,email,hq);
        User u = new User(u1, u1email);
        producerService.completeRegistration(r.getProducerId(), r.getRegistrationId(), u);
        assertEquals(u, producerService.getManager(u1));
    }
}

//import org.junit.jupiter.api.Test;
//
//public class ProducerServiceTest {
//    @Test
//    public void sanityCheck() {
//        System.out.println("Test runs!");
//    }
//}