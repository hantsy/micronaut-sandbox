package com.example.application.internal;

import com.example.application.CreatePostCommand;
import com.example.application.PostCommandService;
import com.example.domain.model.Post;
import com.example.domain.model.PostTagRelation;
import com.example.domain.repository.HashTagRepository;
import com.example.domain.repository.PostRepository;
import com.example.domain.repository.PostTagRelationRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Singleton
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository postRepository;
    private final HashTagRepository hashTagRepository;
    private final PostTagRelationRepository postTagRelationRepository;

    @Override
    public UUID createPost(CreatePostCommand command) {
        // verify tagIds existence
        command.tagId().forEach(id -> {
            if (!hashTagRepository.existsById(id)) {

                // in the real world, it could be a custom exception
                throw new RuntimeException("Hash tag not found by id:" + id);
            }
        });

        // create post
        var savedID = postRepository.save(Post.of(command.title(), command.content())).id();

        // creat post tag relations
        var postTags = command.tagId()
                .stream()
                .map(id -> PostTagRelation.of(savedID, id))
                .toList();
        postTagRelationRepository.saveAll(postTags);

        // return saved post id
        return savedID;
    }
}
