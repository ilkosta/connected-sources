package org.connected_sources.user;

import org.connected_sources.shared.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

public class TeamServiceTest {
  @Test void addMemberMustThrowForProducerWithoutTeam() {
    String tid = "t1";
    String pid = "p1";
    String uid = "u1";
    String uemail = "u1@org";
    String t2 = "t2";

    TeamService ts = new TeamService();
    User u1 = new User(uid, uemail);

    // il produttore non ha nessun team
    assertThrows(IllegalArgumentException.class, () -> {
      ts.addMember(pid, tid, u1, UserRole.MANAGER);
    });

    // il produttore non ha quel team
    ts.createTeam(t2,pid);
    assertThrows(IllegalArgumentException.class, () -> {
      ts.addMember(pid, tid, u1, UserRole.MANAGER);
    });
  }

  @Test
  public void removingMemberMustBeSilentIfNotExisting() {
    String tid = "t1";
    String t2 = "t2";
    String pid = "p1";
    String uid = "u1";
    String uemail = "u1@org";
    TeamService ts = new TeamService();
    assertDoesNotThrow(() -> {
      ts.removeMember(pid,tid,uid);
    });

    User u1 = new User(uid, uemail);
    assertDoesNotThrow(() -> {
      ts.removeMember(pid,tid,u1);
    });

    ts.createTeam(t2,pid);
    assertDoesNotThrow(() -> {
      ts.removeMember(pid,tid,uid);
    });
  }


  @Test
  public void testTeamMemberLifecycle() {
    String tid = "t1";
    String pid = "p1";
    String uid = "u1";
    String uemail = "u1@org";


    TeamService ts = new TeamService();
    String retTID = ts.createTeam(tid, pid);
    assertEquals(tid, retTID);
    User u1 = new User(uid, uemail);
    ts.addMember(pid,tid, u1, UserRole.MANAGER);
    Set<TeamMember> members = ts.getMembers(pid,tid);
    assertEquals(1, members.size());
    assertTrue(members.stream().anyMatch(m -> m.getUser().getId().equals(uid)));
    assertTrue(ts.isMember(pid,tid,uid));
    ts.removeMember(pid, tid, u1);
    assertTrue(ts.getMembers(pid,tid).isEmpty());
    assertFalse(ts.isMember(pid,tid,uid));

  }
}
