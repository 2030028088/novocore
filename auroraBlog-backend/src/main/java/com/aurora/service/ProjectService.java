package com.aurora.service;

import com.aurora.entity.Project;
import com.aurora.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    public Page<Project> getProjects(Integer page, Integer size, Boolean published, 
                                      String category, String search) {
        Page<Project> pageParam = new Page<>(page, size);
        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        
        if (published != null) {
            wrapper.eq("published", published);
        }
        
        if (StringUtils.hasText(category) && !category.equals("ALL")) {
            wrapper.eq("category", category.toUpperCase());
        }
        
        if (StringUtils.hasText(search)) {
            String likeSearch = "%" + search + "%";
            wrapper.and(w -> w.like("title", likeSearch)
                    .or().like("title_zh", likeSearch)
                    .or().like("description", likeSearch));
        }
        
        wrapper.orderByDesc("year");
        
        return projectMapper.selectPage(pageParam, wrapper);
    }
    
    public List<Project> getPublishedProjects() {
        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        wrapper.eq("published", true);
        wrapper.orderByDesc("year");
        return projectMapper.selectList(wrapper);
    }
    
    public Project getProjectById(Long id) {
        return projectMapper.selectById(id);
    }
    
    public Project createProject(Project project) {
        projectMapper.insert(project);
        return project;
    }
    
    public Project updateProject(Long id, Project project) {
        project.setId(id);
        projectMapper.updateById(project);
        return project;
    }
    
    public boolean deleteProject(Long id) {
        return projectMapper.deleteById(id) > 0;
    }
    
    public boolean togglePublish(Long id) {
        Project project = projectMapper.selectById(id);
        if (project != null) {
            project.setPublished(!project.getPublished());
            return projectMapper.updateById(project) > 0;
        }
        return false;
    }
}
