package com.github.kalininaleksandrv.clickleefigleeapi.model;

import java.util.ArrayList;
import java.util.Objects;

public class NewsJsonWraper {
    public NewsJsonWraper() {
    }

    private String status;
    private ArrayList<News> news;
    private String page;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<News> getNews() {
        return news;
    }

    public void setNews(ArrayList<News> news) {
        this.news = news;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsJsonWraper that = (NewsJsonWraper) o;

        if (!Objects.equals(status, that.status)) return false;
        if (!Objects.equals(news, that.news)) return false;
        return Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (news != null ? news.hashCode() : 0);
        result = 31 * result + (page != null ? page.hashCode() : 0);
        return result;
    }
}
