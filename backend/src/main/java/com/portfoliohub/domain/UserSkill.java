package com.portfoliohub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_skills")
@IdClass(UserSkill.Key.class)
@Getter
@Setter
public class UserSkill {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "skill_id")
    private UUID skillId;

    @Column(length = 255)
    private String note;

    public static class Key implements Serializable {
        private UUID userId;
        private UUID skillId;

        public Key() {
        }

        public Key(UUID userId, UUID skillId) {
            this.userId = userId;
            this.skillId = skillId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(userId, key.userId) && Objects.equals(skillId, key.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, skillId);
        }
    }
}
