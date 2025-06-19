package org.connected_sources.user;

import org.connected_sources.shared.Team;
import org.connected_sources.shared.TeamMember;
import org.connected_sources.shared.User;
import org.connected_sources.shared.UserRole;

import java.util.*;

public class TeamService {

    private final Map<String, Map<String, Team>> producers = new HashMap<>();

    public String createTeam(String teamId, String producerId) {
      if (producers.containsKey(producerId)) {
          Map<String, Team> teams = producers.get(producerId);
          teams.computeIfAbsent(teamId, k -> new Team(k, producerId));
      }
      else {
          Map<String, Team> teams = new HashMap<>();
          teams.put(teamId, new Team(teamId, producerId));
          producers.put(producerId, teams);
      }

      return teamId;
    }

    public void addMember(String producerId, String teamId, User user, UserRole role) {
        if( producers.containsKey(producerId)) {
            Map<String, Team> teams = producers.get(producerId);
            Team team = teams.get(teamId);
            if (team != null) {
                team.addMember(new TeamMember(user, role));
            }
            else {
                throw new IllegalArgumentException("No team found for producer " + producerId);
            }
        }
        else {
            throw new IllegalArgumentException("No producer found for team " + teamId);
        }
    }


    // silent
    public void removeMember(String producerId, String teamId, User user) {
        if( producers.containsKey(producerId)) {
            Map<String, Team> teams = producers.get(producerId);
            Team team = teams.get(teamId);
            if (team != null) {
                team.removeMember(user);
            }
        }
    }

    // silent
    public void removeMember(String producerId, String teamId, String userId) {
        if( producers.containsKey(producerId)) {
            Map<String, Team> teams = producers.get(producerId);
            Team team = teams.get(teamId);
            if (team != null) {
                team.removeMember(userId);
            }
        }
    }

    public Set<TeamMember> getMembers(String producerId,String teamId) {
        if (producers.containsKey(producerId)) {
            Map<String, Team> teams = producers.get(producerId);
            if (teams == null) { return Set.of(); }
            else {
                Team team = teams.get(teamId);
                return team != null ? team.getMembers() : Set.of();
            }
        }
        else { return Set.of(); }
    }

    public boolean isMember(String producerId, String teamId, String userId) {
        return getMembers(producerId,teamId)
                .stream()
                .anyMatch(member -> member.getUser().getId().equals(userId));
    }
}
