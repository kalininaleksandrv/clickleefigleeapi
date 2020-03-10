package com.github.kalininaleksandrv.clickleefigleeapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Arrays;


@Document(collection = "news")
public class News implements Serializable {

    public News() {
    }

    @Id
    private String id;
    private String title;
    private String description;
    private String url;
    private String author;
    private String image;
    private String language;
    private String[] category;

    @Indexed(direction = IndexDirection.ASCENDING)
    private String published;
    private int resultofprocessing;

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                ", image='" + image + '\'' +
                ", language='" + language + '\'' +
                ", category=" + Arrays.toString(category) +
                ", published='" + published + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        News other = (News) obj;
        if (!this.id.equals(other.getId()))
            return false;
        return this.title.equals(other.getTitle());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode()) +
                ((title == null) ? 0 : title.hashCode()) +
                ((description == null) ? 0 : description.hashCode());
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }


    public int getResultofprocessing() {
        return resultofprocessing;
    }

    public void setResultofprocessing(int resultofprocessing) {
        this.resultofprocessing = resultofprocessing;
    }
}
