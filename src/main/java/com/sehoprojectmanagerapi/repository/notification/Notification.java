package com.sehoprojectmanagerapi.repository.notification;

import com.sehoprojectmanagerapi.repository.baseentity.BaseEntity;
import com.sehoprojectmanagerapi.repository.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 받는 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /** 알림 메시지 */
    @Column(nullable = false, length = 500)
    private String message;

    /** 알림 유형 (TASK_ASSIGNED, COMMENT_ADDED, PROJECT_INVITE 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    /** 관련된 엔티티 ID (TaskId, ProjectId 등) */
    private Long relatedId;

    /** 읽음 여부 */
    private boolean readFlag = false;

    /** 읽음 처리 */
    public void markAsRead() {
        this.readFlag = true;
    }
}
