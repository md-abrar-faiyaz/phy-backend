package com.physics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.*;

@Entity @Data
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<ResourceFile> getResources() {
        return resources;
    }

    public void setResources(List<ResourceFile> resources) {
        this.resources = resources;
    }

    private Integer year; // 1, 2, 3, or 4
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<ResourceFile> resources = new ArrayList<>();
}








