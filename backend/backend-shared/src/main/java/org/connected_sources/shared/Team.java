package org.connected_sources.shared;

import java.util.*;

public class Team {
    private final String teamId;
    private final String producerId;
    private final Set<TeamMember> members = new HashSet<>();

    public Team(String teamId, String producerId) {
        this.teamId = teamId;
        this.producerId = producerId;
    }

    public void addMember(TeamMember member) {
        members.add(member);
    }

    public void removeMember(User user) {
        members.removeIf(member -> member.getUser().equals(user));
    }

    public Set<TeamMember> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public String getProducerId() {
        return producerId;
    }

    public String getTeamId() {
        return teamId;
    }
}
