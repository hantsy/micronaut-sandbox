package com.example.photos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

@Data
@BsonDiscriminator(value = "albums")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    @BsonId
    private String id;

    @BsonProperty
    private String name;

    @Builder.Default
    private List<String> photos = new ArrayList<>();

    public synchronized void addPhoto(String photoId) {
        if (!photos.contains(photoId)) {
            this.photos.add(photoId);
        }
    }

    public void removePhoto(String photoId) {
        this.photos.removeIf(e -> e.equals(photoId));
    }

    public static Album of(String name) {
        var album = new Album();
        album.setName(name);
        return album;
    }

}
