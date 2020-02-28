package com.github.kalininaleksandrv.clickleefigleeapi.interfaces;
import com.github.kalininaleksandrv.clickleefigleeapi.model.News;

import java.util.List;

public interface MessageHolder {
    void holdAllMessages(List<News> news);
    void holdError(String error);
}
