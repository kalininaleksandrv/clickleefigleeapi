package com.github.kalininaleksandrv.clickleefigleeapi.interfaces;

import com.github.kalininaleksandrv.clickleefigleeapi.model.News;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsRepository extends MongoRepository<News,String> {
}
