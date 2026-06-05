package com.wasac.billing.dto;

import com.wasac.billing.enums.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private String message;
        private LocalDateTime sentAt;
        private NotificationStatus status;
    }
}
