package com.physics.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.physics.entity.Course;
import com.physics.entity.Founder;
import com.physics.entity.ResourceFile;
import com.physics.repository.CourseRepo;
import com.physics.repository.FileRepo;
import com.physics.repository.FounderRepo;
import com.physics.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") @CrossOrigin("*")
class PhysicsController {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired private FileRepo fileRepo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private CloudinaryService cloudinaryService;

    // Config for Cloudinary (Add Cloudinary Bean in a config class)
    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "YOUR_NAME", "api_key", "YOUR_KEY", "api_secret", "YOUR_SECRET"));

    @GetMapping("/courses/{year}")
    public List<Course> getByYear(@PathVariable Integer year) {
        return courseRepo.findByYear(year);
    }

    @PostMapping("/admin/add-course")
    @Transactional
    public ResponseEntity<?> addCourse(@RequestBody Course course, @RequestHeader("Admin-Key") String key) {
        if (!"OxPOQmA1hm0jnWuWmvOVpBs41tA".equals(key)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(courseRepo.save(course));
    }

    @PostMapping("/admin/upload/{courseId}")
    public ResponseEntity<?> upload(@PathVariable Long courseId,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam("name") String name,
                                    @RequestParam("description") String description,
                                    @RequestParam("type") String type,
                                    @RequestParam(value = "url", required = false) String url,
                                    @RequestHeader("Admin-Key") String key) throws IOException {

        // 1. Security check
        if (!"OxPOQmA1hm0jnWuWmvOVpBs41tA".equals(key)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 2. Find Course
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setName(name);
        resourceFile.setDescription(description);
        resourceFile.setType(type);
        resourceFile.setCourse(course);

        // 3. Logic based on Type
        if ("file".equalsIgnoreCase(type) && file != null) {
            // Upload to Cloudinary
            String fileUrl = cloudinaryService.uploadFile(file);
            resourceFile.setUrl(fileUrl);
        } else if ("link".equalsIgnoreCase(type)) {
            // Use the provided URL string
            resourceFile.setUrl(url);
        } else {
            return ResponseEntity.badRequest().body("Invalid upload type or missing data");
        }

        return ResponseEntity.ok(fileRepo.save(resourceFile));
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("towsifurrishad012@gmail.com");
        msg.setSubject("Physics App Request");
        msg.setText("From: " + body.get("email") + "\n\n" + body.get("message"));
        mailSender.send(msg);

        return ResponseEntity.ok(Map.of("message", "Success"));
    }

    @DeleteMapping("/admin/delete-resource/{fileId}")
    @Transactional
    public ResponseEntity<?> deleteResource(@PathVariable Long fileId, @RequestHeader("Admin-Key") String key) throws IOException {
        if (!"OxPOQmA1hm0jnWuWmvOVpBs41tA".equals(key)) return ResponseEntity.status(401).build();

        ResourceFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 1. Delete from Cloudinary
        cloudinaryService.deleteFile(file.getUrl());

        // 2. Delete from PostgreSQL
        fileRepo.delete(file);

        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/admin/delete-course/{courseId}")
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId, @RequestHeader("Admin-Key") String key) throws IOException {
        if (!"OxPOQmA1hm0jnWuWmvOVpBs41tA".equals(key)) return ResponseEntity.status(401).build();

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 1. Get all file URLs associated with this course
        List<String> fileUrls = course.getResources().stream()
                .map(ResourceFile::getUrl)
                .toList();

        // 2. Delete files from Cloudinary
        cloudinaryService.deleteMultipleFiles(fileUrls);

        // 3. Delete course from DB (Cascade will handle ResourceFile records)
        courseRepo.delete(course);

        return ResponseEntity.ok().build();
    }

    @Autowired
    private FounderRepo founderRepo;

    // 1. Get Founder Info (Public)
    @GetMapping("/founder-info")
    public ResponseEntity<Founder> getFounderInfo() {
        return ResponseEntity.ok(founderRepo.findFirstByOrderByIdAsc()
                .orElse(new Founder())); // Return empty object if not set
    }

    // 2. Upload Founder Image (Admin Only)
    @PostMapping("/admin/upload-founder")
    public ResponseEntity<?> uploadFounderImage(@RequestParam("file") MultipartFile file,
                                                @RequestHeader("Admin-Key") String key) throws IOException {
        if (!"OxPOQmA1hm0jnWuWmvOVpBs41tA".equals(key)) return ResponseEntity.status(401).build();

        // Upload to Cloudinary
        String imageUrl = cloudinaryService.uploadFile(file);

        // Update or Create Founder record
        Founder founder = founderRepo.findFirstByOrderByIdAsc().orElse(new Founder());
        founder.setImageUrl(imageUrl);

        // Pre-fill your details if this is the first time
        if (founder.getName() == null) {
            founder.setName("Towsifur Rishad");
            founder.setDepartment("Physics");
            founder.setBatch("54");
        }

        return ResponseEntity.ok(founderRepo.save(founder));
    }
}
