package com.sehoprojectmanagerapi.service.team;

import com.sehoprojectmanagerapi.config.function.RoleFunc;
import com.sehoprojectmanagerapi.repository.team.Team;
import com.sehoprojectmanagerapi.repository.team.TeamRepository;
import com.sehoprojectmanagerapi.repository.team.teamInvite.TeamInvite;
import com.sehoprojectmanagerapi.repository.team.teamInvite.TeamInviteRepository;
import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMember;
import com.sehoprojectmanagerapi.repository.team.teammember.TeamMemberRepository;
import com.sehoprojectmanagerapi.repository.user.User;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.ConflictException;
import com.sehoprojectmanagerapi.service.exceptions.NotAcceptableException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteRequest;
import com.sehoprojectmanagerapi.web.dto.team.TeamInviteResponse;
import com.sehoprojectmanagerapi.web.dto.team.TeamRequest;
import com.sehoprojectmanagerapi.web.dto.team.TeamResponse;
import com.sehoprojectmanagerapi.web.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private static final int DEFAULT_INVITE_TTL_DAYS = 14;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final RoleFunc roleFunc;

    @Transactional
    public List<TeamResponse> getAllTeamsByUser(Long userId) {
        return teamMemberRepository.findByUserId(userId)
                .stream().map(teamMember -> teamMapper.toTeamResponse(teamMember.getTeam())).toList();

    }

    @Transactional
    public TeamResponse getTeamByUserIdAndTeamId(Long userId, Long teamId) {
        return teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .map(teamMember -> teamMapper.toTeamResponse(teamMember.getTeam()))
                .orElseThrow(() -> new NotAcceptableException("해당 정보에 접근할 수 없습니다.", null));
    }

    @Transactional
    public TeamResponse createTeam(Long userId, TeamRequest teamRequest) {
        if (teamRequest.name() == null || teamRequest.name().trim().isEmpty()) {
            throw new BadRequestException("팀명이 비어있습니다.", null);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        Team team = Team.builder()
                .name(teamRequest.name())
                .build();

        Team savedTeam = teamRepository.save(team);

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(savedTeam);
        teamMember.setUser(user);
        teamMember.setRole(RoleTeam.OWNER);
        teamMember.setJoinedAt(OffsetDateTime.now());

        teamMemberRepository.save(teamMember);

        return teamMapper.toTeamResponse(savedTeam);
    }

    @Transactional
    public TeamResponse updateTeam(Long userId, Long teamId, TeamRequest teamRequest) {
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new NotFoundException("해당 팀에 본 사용자는 권한이 없습니다.", userId));

        Team team = teamMember.getTeam();

        boolean teamOk = roleFunc.hasAtLeast(teamMember.getRole(), RoleTeam.ADMIN);

        if (!teamOk) {
            throw new NotAcceptableException("팀 수정 권한이 없습니다.", userId);
        }

        // 3) 이름 변경 처리
        if (teamRequest.name() != null && !teamRequest.name().equalsIgnoreCase(team.getName())) {
            // 같은 프로젝트 내 팀명 중복 방지
            boolean nameExists = teamRepository.existsByNameIgnoreCase(teamRequest.name());
            if (nameExists) {
                throw new ConflictException("같은 프로젝트에 동일한 팀명이 이미 존재합니다.", teamRequest.name());
            }
            team.setName(teamRequest.name());
        }

        teamRepository.save(team);
        return teamMapper.toTeamResponse(team);
    }

    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        // 1) 팀 로드
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("해당 팀을 찾을 수 없습니다.", teamId));

        // 2) 권한 체크
        // - 팀 OWNER/MANAGER 이거나
        // - 프로젝트 ROLE이 MANAGER/ADMIN/OWNER 여야 함
        TeamMember tm = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new NotAcceptableException("해당 팀의 멤버가 아닙니다.", userId));

        boolean teamOk = roleFunc.hasAtLeast(tm.getRole(), RoleTeam.ADMIN);

        if (!teamOk) {
            throw new NotAcceptableException("팀 삭제 권한이 없습니다.", userId);
        }

        // 4) 삭제 (Team.members는 orphanRemoval=true 이므로 함께 제거)
        try {
            teamRepository.delete(team);
            teamRepository.flush(); // 즉시 constraint 위반 감지
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new ConflictException("팀을 삭제할 수 없습니다. 참조 중인 리소스가 있습니다.", teamId);
        } catch (RuntimeException e) {
            throw new ConflictException("해당 팀 삭제에 실패했습니다.", teamId);
        }
    }

    @Transactional
    public TeamInviteResponse inviteToTeam(Long inviterId, Long teamId, TeamInviteRequest request) {
        // 1) 권한자 확인
        TeamMember inviterMember = teamMemberRepository.findByUserIdAndTeamId(inviterId, teamId)
                .orElseThrow(() -> new NotFoundException("해당 팀에 본 사용자는 권한이 없습니다.", inviterId));
        if (inviterMember.getRole() != RoleTeam.OWNER) {
            throw new ConflictException("초대 권한이 없습니다.", inviterId);
        }

        // 2) 팀/유저 조회
        Team team = inviterMember.getTeam();
        User inviter = inviterMember.getUser();
        User invited = userRepository.findById(request.invitedUserId())
                .orElseThrow(() -> new NotFoundException("초대된 해당 사용자를 찾을 수 없습니다.", request.invitedUserId()));

        if (inviter.getId().equals(invited.getId())) {
            throw new ConflictException("자기 자신을 초대할 수 없습니다.", inviterId);
        }

        // 3) 이미 멤버인지 검사
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invited.getId())) {
            throw new ConflictException("이미 팀 멤버입니다.", invited.getId());
        }

        // 4) 중복 PENDING 초대 검사
        boolean hasPending = teamInviteRepository.existsByTeamIdAndInvitedUserIdAndStatusInAndExpiresAtAfter(
                teamId, invited.getId(), List.of(TeamInvite.Status.PENDING), OffsetDateTime.now());
        if (hasPending) {
            throw new ConflictException("대기 중인 초대가 이미 존재합니다.", invited.getId());
        }

        // 5) 초대 생성
        TeamInvite invite = new TeamInvite();
        invite.setTeam(team);
        invite.setInviter(inviter);
        invite.setInvitedUser(invited);
        invite.setStatus(TeamInvite.Status.PENDING);
        invite.setMessage(request.message());
        invite.setRequestedRole(request.requestedRole());
        invite.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_INVITE_TTL_DAYS));

        TeamInvite saved = teamInviteRepository.save(invite);

        // 6) (옵션) 알림/이벤트 발행 가능

        return teamMapper.toInviteResponse(saved);
    }

    @Transactional
    public TeamResponse acceptTeamInvite(Long userId, Long teamId, Long inviteId) {
        TeamInvite invite = teamInviteRepository.findByIdAndTeamId(inviteId, teamId)
                .orElseThrow(() -> new NotFoundException("초대를 찾을 수 없습니다.", inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("초대 대상자가 아닙니다.", userId);
        }

        if (invite.getStatus() != TeamInvite.Status.PENDING) {
            throw new ConflictException("이미 처리된 초대입니다.", inviteId);
        }

        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(TeamInvite.Status.EXPIRED);
            teamInviteRepository.save(invite);
            throw new ConflictException("초대가 만료되었습니다.", inviteId);
        }

        // 이미 멤버가 되었는지 최종 확인(경쟁 상황 대비)
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            TeamMember newMember = new TeamMember();
            newMember.setTeam(invite.getTeam());
            newMember.setUser(invite.getInvitedUser());
            newMember.setRole(invite.getRequestedRole() != null ? invite.getRequestedRole() : RoleTeam.MEMBER);
            newMember.setJoinedAt(OffsetDateTime.now());
            teamMemberRepository.save(newMember);
        }

        // 상태 변경 및 다른 PENDING 초대 만료
        invite.setStatus(TeamInvite.Status.ACCEPTED);
        teamInviteRepository.save(invite);
        teamInviteRepository.expireOtherPendings(teamId, userId, invite.getId(), OffsetDateTime.now());

        return teamMapper.toTeamResponse(invite.getTeam()); // 기존 변환기 재사용
    }

    @Transactional
    public void declineTeamInvite(Long userId, Long teamId, Long inviteId) {
        TeamInvite invite = teamInviteRepository.findByIdAndTeamId(inviteId, teamId)
                .orElseThrow(() -> new NotFoundException("초대를 찾을 수 없습니다.", inviteId));

        if (!invite.getInvitedUser().getId().equals(userId)) {
            throw new NotAcceptableException("초대 대상자가 아닙니다.", userId);
        }

        if (invite.getStatus() == TeamInvite.Status.PENDING) {
            invite.setStatus(TeamInvite.Status.DECLINED);
            teamInviteRepository.save(invite);
        }
        // 이미 ACCEPTED/DECLINED/EXPIRED면 멱등 처리(아무 동작 안 함)
    }
}
