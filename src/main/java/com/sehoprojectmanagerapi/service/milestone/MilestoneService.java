package com.sehoprojectmanagerapi.service.milestone;

import com.sehoprojectmanagerapi.config.mapper.MilestoneMapper;
import com.sehoprojectmanagerapi.repository.milestone.Milestone;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneRepository;
import com.sehoprojectmanagerapi.repository.milestone.MilestoneStatus;
import com.sehoprojectmanagerapi.repository.project.Project;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMember;
import com.sehoprojectmanagerapi.repository.project.projectmember.ProjectMemberRepository;
import com.sehoprojectmanagerapi.repository.team.teammember.RoleTeam;
import com.sehoprojectmanagerapi.repository.user.UserRepository;
import com.sehoprojectmanagerapi.service.exceptions.BadRequestException;
import com.sehoprojectmanagerapi.service.exceptions.CustomBadCredentialsException;
import com.sehoprojectmanagerapi.service.exceptions.NotFoundException;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneRequest;
import com.sehoprojectmanagerapi.web.dto.milestone.MilestoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final MilestoneMapper milestoneMapper;
    private final UserRepository userRepository;

    @Transactional
    public List<MilestoneResponse> getAllMilestonesByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        return milestoneRepository.findAllVisibleForUser(userId)
                .stream().map(milestoneMapper::toResponse).toList();
    }

    @Transactional
    public MilestoneResponse createMilestone(Long userId, MilestoneRequest request) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(()->new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        if(request.title().isEmpty()){
            throw new BadRequestException("해당 제목란이 비어있습니다.",  request.title());
        }

        if(request.status().isEmpty()){
            throw new BadRequestException("해당 상태란이 비어있습니다.", request.status());
        }

        Milestone milestone = new Milestone();
        milestone.setProject(projectMember.getProject());
        milestone.setTitle(request.title());
        milestone.setDescription(request.description());
        milestone.setStartDate(request.startDate());
        milestone.setDueDate(request.dueDate());
        milestone.setStatus(MilestoneStatus.valueOf(request.status()));

        milestoneRepository.save(milestone);

        return milestoneMapper.toResponse(milestone);
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long userId, Long milestoneId, MilestoneRequest request) {
        ProjectMember projectMember = projectMemberRepository.findByUserIdAndProjectId(userId, request.projectId())
                .orElseThrow(()->new NotFoundException("해당 프로젝트를 찾을 수 없습니다.", request.projectId()));

        if(request.title().isEmpty()){
            throw new BadRequestException("해당 제목란이 비어있습니다.", request.title());
        }

        if(request.description().isEmpty()){
            throw new BadRequestException("해당 내용란이 비어있습니다.", request.description());
        }

        if(request.status().isEmpty()){
            throw new BadRequestException("해당 상태란이 비어있습니다.", request.status());
        }

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(()->new NotFoundException("해당 마일스톤을 찾을 수 없습니다.", milestoneId));

        milestone.setProject(projectMember.getProject());
        milestone.setTitle(request.title());
        milestone.setDescription(request.description());
        milestone.setStartDate(request.startDate());
        milestone.setDueDate(request.dueDate());
        milestone.setStatus(MilestoneStatus.valueOf(request.status()));

        milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    @Transactional
    public void deleteMilestone(Long userId, Long milestoneId) {
        // 1. 마일스톤 조회
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("해당 마일스톤을 찾을 수 없습니다.", milestoneId));

        // 2. 권한 확인
        // (a) 프로젝트 생성자이거나
        // (b) 팀 Owner 이어야 함
        Project project = milestone.getProject();

        // 프로젝트 생성자 체크
        boolean isProjectCreator = project.getCreatedBy().getId().equals(userId);

        // 팀 Owner 체크
        boolean isTeamOwner = project.getTeam().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId) && m.getRole() == RoleTeam.OWNER);

        if (!isProjectCreator && !isTeamOwner) {
            throw new CustomBadCredentialsException("해당 마일스톤을 삭제할 권한이 없습니다.", userId);
        }

        // 3. 삭제 수행
        milestoneRepository.delete(milestone);
    }
}
