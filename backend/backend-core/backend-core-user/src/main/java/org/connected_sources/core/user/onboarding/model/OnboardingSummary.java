package org.connected_sources.core.user.onboarding.model;

import org.connected_sources.shared.onboarding.OnboardingState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public record OnboardingSummary(
        Long id,
        OnboardingState state,
        String requester,
        String producerName,
        String email,
        String website,
        String vatOrFiscalCode,
        String correlationId,
        Timestamp created_at        
) {
    public static OnboardingSummary fromRecord(ResultSet rs) throws SQLException {
        String status = rs.getString("state");
        OnboardingState state = OnboardingState.fromString(rs.getString("state"));
        return new OnboardingSummary(
            rs.getLong("id"),
                state,
            rs.getString("requester_user_id"),
            rs.getString("producer_name"),
            rs.getString("email"),
            rs.getString("website"),
            rs.getString("vat_or_fiscal_code"),
            rs.getString("correlation_id"),
            rs.getTimestamp("created_at")
        );
    }
}