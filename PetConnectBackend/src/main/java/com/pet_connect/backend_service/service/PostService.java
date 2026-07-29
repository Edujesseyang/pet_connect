package com.pet_connect.backend_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pet_connect.backend_service.dto.request.AddPostRequest;
import com.pet_connect.backend_service.dto.respond.InnerRespond;
import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.Post;
import com.pet_connect.backend_service.repository.PostDAO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostService {

    private final PostDAO dao;

    public PostService(PostDAO postDAO) {
        this.dao = postDAO;
    }

    public InnerRespond<Post> createPost(AddPostRequest form) {
        Post post = new Post();
        post.setPetId(form.getPetId());
        post.setUserId(form.getUserId());
        post.setCreatedAt(form.getCreated_at());
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        post.setType(form.getType());
        post.setAdoptionFee(form.getAdoptionFee());
        Address address = new Address();
        address.setCountry(form.getCountry());
        address.setState(form.getState());
        address.setCity(form.getCity());
        address.setStreet(form.getStreet());
        address.setZipCode(form.getZipcode());
        post.setPickupLocation(address);

        try {
            post.setPostId(-1);
            post = dao.addPost(post);
            if (post.getPostId() != -1) {
                return new InnerRespond<>(true, "successful adding post", post);
            }
        } catch (RuntimeException e) {
            log.error("ERROR: while adding post", e);
        }
        return new InnerRespond<>(false, "faild to add post");
    }

    public InnerRespond<List<Post>> getAllPost() {
        List<Post> returnVal = dao.getAllPost();
        return new InnerRespond<>(true, "all posts", returnVal);
    }

    public InnerRespond<List<Post>> searchKeyword(String keyword) {
        List<Post> returnVal = dao.searchKeyword(keyword);
        return new InnerRespond<>(true, "all posts", returnVal);
    }

    public InnerRespond<List<Post>> getUserPosts(int userId) {
        List<Post> returnVal = dao.getUserPosts(userId);
        return new InnerRespond<>(true, "all posts", returnVal);
    }

    public InnerRespond<List<Post>> getSavedPostByUserId(int userId) {
        List<Post> returnVal = dao.getSavedPostByUserId(userId);
        return new InnerRespond<>(true, "all posts", returnVal);
    }

    public InnerRespond<List<Post>> getAppliedPostByUserId(int userId) {
        List<Post> returnVal = dao.getAppliedPostByUserId(userId);
        return new InnerRespond<>(true, "all posts", returnVal);
    }

    public InnerRespond<?> unsavePost(int userId, int postId) {
        if (dao.unsavePost(userId, postId)) {
            return new InnerRespond<>(true, "success");
        }
        return new InnerRespond<>(false, "faild");
    }

    public InnerRespond<?> unApplyPost(int userId, int postId) {
        if (dao.unApplyPost(userId, postId)) {
            return new InnerRespond<>(true, "success");
        }
        return new InnerRespond<>(false, "faild");
    }

    public InnerRespond<?> deletePost(int postId) {
        if (dao.deletePost(postId)) {
            return new InnerRespond<>(true, "delete post successful");
        }
        return new InnerRespond<>(false, "fail to delete post");
    }

    public InnerRespond<?> savePost(int postId, int userId) {
        if (dao.savePost(postId, userId)) {
            return new InnerRespond<>(true, "save post successful");
        }
        return new InnerRespond<>(false, "fail to save post");
    }

    public InnerRespond<?> applyPost(int postId, int userId) {
        if (dao.applyPost(postId, userId)) {
            return new InnerRespond<>(true, "save post successful");
        }
        return new InnerRespond<>(false, "fail to save post");
    }
}
