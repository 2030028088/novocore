package com.aurora.controller;

import com.aurora.entity.Project;
import com.aurora.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        Page<Project> pageResult = projectService.getProjects(page, size, published, category, search);
        
        return ResponseEntity.ok(Map.of(
            "items", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "page", pageResult.getCurrent(),
            "perPage", pageResult.getSize(),
            "totalPages", pageResult.getPages()
        ));
    }
    
    @GetMapping("/published")
    public ResponseEntity<List<Project>> getPublishedProjects() {
        List<Project> projects = projectService.getPublishedProjects();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }
    
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project created = projectService.createProject(project);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        Project updated = projectService.updateProject(id, project);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/{id}/toggle-publish")
    public ResponseEntity<Map<String, Boolean>> togglePublish(@PathVariable Long id) {
        boolean newStatus = projectService.togglePublish(id);
        return ResponseEntity.ok(Map.of("published", newStatus));
    }
}
