package org.connected_sources.shared;

import java.util.Objects;

public class TeamMember {
    private final User user;
    private final UserRole role;

    public TeamMember(User user, UserRole role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamMember)) return false;
        TeamMember that = (TeamMember) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
