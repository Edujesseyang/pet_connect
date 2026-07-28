package com.pet_connect.backend_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet_connect.backend_service.dto.request.AddPostRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Post;
import com.pet_connect.backend_service.service.PostService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create_post")
    public ResponseEntity<?> createPost(@RequestBody AddPostRequest form) {
        InnerRespond<Post> result = postService.createPost(form);
        if (result.getState()) {
            log.info("user created a post", form.getUserId());
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/get_all_post")
    public ResponseEntity<?> getAllPost() {
        InnerRespond<List<Post>> result = postService.getAllPost();
        if (result.getState()) {
            log.info("user getting all post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<?> searchKeyword(@RequestParam String keyword) {
        InnerRespond<List<Post>> result = postService.searchKeyword(keyword);
        if (result.getState()) {
            log.info("user searching: " + keyword);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/get_my_post/{userId}")
    public ResponseEntity<?> getPostByUserId(@RequestParam int userId) {
        InnerRespond<List<Post>> result = postService.getUserPosts(userId);
        if (result.getState()) {
            log.info("user getting user's post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/get_saved_post/{userId}")
    public ResponseEntity<?> getSavedPostByUserId(@RequestParam int userId) {
        InnerRespond<List<Post>> result = postService.getSavedPostByUserId(userId);
        if (result.getState()) {
            log.info("user getting saved user's post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/get_applied_post/{userId}")
    public ResponseEntity<?> getAppliedPostByUserId(@RequestParam int userId) {
        InnerRespond<List<Post>> result = postService.getAppliedPostByUserId(userId);
        if (result.getState()) {
            log.info("user getting applied user's post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/unsave_post/{user_id}/{post_id}")
    public ResponseEntity<?> unsavePost(@RequestParam int user_id, @RequestParam int post_id) {
        InnerRespond<?> result = postService.unsavePost(user_id, post_id);
        if (result.getState()) {
            log.info("user atempt to unsave post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/unapply_post/{user_id}/{post_id}")
    public ResponseEntity<?> unApplyPost(@RequestParam int user_id, @RequestParam int post_id) {
        InnerRespond<?> result = postService.unApplyPost(user_id, post_id);
        if (result.getState()) {
            log.info("user atempt to un apply post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/delete_post/{post_id}")
    public ResponseEntity<?> deltePost(@RequestParam int post_id) {
        InnerRespond<?> result = postService.deletePost(post_id);
        if (result.getState()) {
            log.info("user atempt to un apply post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/save_post/{post_id}/{user_id}")
    public ResponseEntity<?> savePost(@RequestParam int post_id, @RequestParam int user_id) {
        InnerRespond<?> result = postService.savePost(post_id, user_id);

        if (result.getState()) {
            log.info("user atempt to save post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/apply_post/{post_id}/{user_id}")
    public ResponseEntity<?> applyPost(@RequestParam int post_id, @RequestParam int user_id) {
        InnerRespond<?> result = postService.applyPost(post_id, user_id);

        if (result.getState()) {
            log.info("user atempt to save post");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

}
