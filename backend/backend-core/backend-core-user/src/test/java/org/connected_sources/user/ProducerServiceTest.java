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
    @InjectMocks
    private ProducerService producerService;

    @Test
    public void testRegisterAndFetchProducer() {
        String email = "email";
        String name = "pippo";
        String pid = "p1";
        producerService.registerProducer(pid, name, email);

        assertTrue(producerService.exists(pid));
        Producer p = producerService.getProducer(pid);
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
        String id = "p1";
        String name = "pippo";
        String email = "email";

        producerService.registerProducer(id,name,email);
        assertTrue(producerService.isRegistered(id));
        Producer p = producerService.getProducer(id);
        assertEquals(name, p.getName());
        verify(tenantManager).createTenant(id);
    }

    @Test
    public void testCompleteRegistrationStoresManager() {
        String id = "p1";
        String name = "pippo";
        String email = "email";

        String u1 = "user1";
        String u1email = "email1";
//        UserRole role = UserRole.MANAGER;
        producerService.registerProducer(id,name,email);
        User u = new User(u1, u1email);
        producerService.completeRegistration(id, u);
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