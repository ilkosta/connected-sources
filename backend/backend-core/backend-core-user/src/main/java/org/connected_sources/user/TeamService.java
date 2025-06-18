package org.connected_sources.user;

import org.connected_sources.shared.*;

import java.util.*;

public class TeamService {

    private final Map<String, Team> teams = new HashMap<>();

    public void createTeam(String teamId, String producerId) {
        teams.put(teamId, new Team(teamId, producerId));
    }

    public void addMember(String teamId, User user, UserRole role) {
        Team team = teams.get(teamId);
        if (team != null) {
            team.addMember(new TeamMember(user, role));
        }
    }

    public void removeMember(String teamId, User user) {
        Team team = teams.get(teamId);
        if (team != null) {
            team.removeMember(user);
        }
    }

    public Set<TeamMember> getMembers(String teamId) {
        Team team = teams.get(teamId);
        return team != null ? team.getMembers() : Set.of();
    }

    public boolean isMember(String teamId, String userId) {
        return getMembers(teamId).stream()
            .anyMatch(member -> member.getUser().getId().equals(userId));
    }
}
