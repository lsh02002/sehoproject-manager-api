package com.sehoprojectmanagerapi.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    List<User> findAllByEmailIn(List<String> emails);

    @Query("""
    SELECT u
    FROM User u
    WHERE u.id NOT IN (
        SELECT wm.user.id
        FROM WorkspaceMember wm
        WHERE wm.workspace.id = :workspaceId
    )
""")
    List<User> findUsersNotInWorkspace(Long workspaceId);

    @Query("""
    select u
    from User u
    where u.id in (
        select wm.user.id
        from WorkspaceMember wm
        where wm.workspace.id = :workspaceId
    )
    and u.id not in (
        select sm.user.id
        from SpaceMember sm
        where sm.space.id = :spaceId
    )
""")
    List<User> findWorkspaceMembersNotInSpace(Long workspaceId, Long spaceId);
}
