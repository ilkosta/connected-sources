package org.connected_sources.user;

import org.connected_sources.shared.TeamMember;
import org.connected_sources.shared.User;
import org.connected_sources.shared.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProducerServiceTest {

    @Test
    public void testRegisterAndFetchProducer() {
        ProducerService service = new ProducerService();
        service.registerProducer("p1", "Produttore Uno");

        assertTrue(service.exists("p1"));
        assertEquals("Produttore Uno", service.getProducerName("p1"));
    }

    @Test
    public void testTeamCreation() {
        TeamService teamService = new TeamService();
        teamService.createTeam("tid", "p1");
        User u = new User("u1", "u1@gmail.com");
        teamService.addMember("tid", u, UserRole.MANAGER);
        u = new User("u2", "u2@gmail.com");
        teamService.addMember("tid", u, UserRole.MEMBER);
        Set<TeamMember> members = teamService.getMembers("tid");
        assertEquals(2, members.size());
        assertTrue(members.contains("u1"));
        assertTrue(members.contains("u2"));
    }
}
