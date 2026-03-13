package com.sehoprojectmanagerapi.repository.user;

import com.sehoprojectmanagerapi.repository.workspace.workspacemember.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

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
