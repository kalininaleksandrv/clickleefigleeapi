package com.github.kalininaleksandrv.clickleefigleeapi.model;

import java.io.Serializable;
import java.util.LinkedList;

public class NewsDAOWraper implements Serializable {

    private String id;

    private final News news;

    //0 - green 1 - yellow 2 - red
    private int clickBaitFactor;

    private LinkedList<Long> usersWhoAddNewsToClickBaitStatus;

    public NewsDAOWraper(News news) {
        this.news = news;
        this.id = news.getId();
        this.clickBaitFactor = 0;
    }

    public String getId() {
        return id;
    }

    public News getNews() {
        return news;
    }

    public int getClickBaitFactor() {
        return clickBaitFactor;
    }

    public LinkedList<Long> getUsersWhoAddNewsToClickBaitStatus() {
        return usersWhoAddNewsToClickBaitStatus;
    }

    public void setClickBaitFactor(int clickBaitFactor) {
        this.clickBaitFactor = clickBaitFactor;
    }

    public void addUsersWhoAddNewsToClickBaitStatus(Long userId) {
        this.usersWhoAddNewsToClickBaitStatus.addLast(userId);
    }

    //wrappers equal if containing news are equal
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News that = ((NewsDAOWraper) o).getNews();
        return that.equals(this.getNews());
    }

    @Override
    public int hashCode() {
        return this.news.hashCode();
    }
}
