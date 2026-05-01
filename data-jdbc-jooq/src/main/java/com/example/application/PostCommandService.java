package com.example.application;

import java.util.UUID;

public interface PostCommandService {
    UUID createPost(CreatePostCommand command);
}
