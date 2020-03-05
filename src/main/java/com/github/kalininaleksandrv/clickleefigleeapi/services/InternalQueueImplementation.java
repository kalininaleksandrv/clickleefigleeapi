package com.github.kalininaleksandrv.clickleefigleeapi.services;

import com.github.kalininaleksandrv.clickleefigleeapi.interfaces.InternalQueue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InternalQueueImplementation implements InternalQueue {

    Set<String> listofnews;

    public InternalQueueImplementation() {
        listofnews = new LinkedHashSet<>();
    }

    @Override
    public boolean compareAndAdd(String newsId) {
        return listofnews.add(newsId);
    }

    @Override
    public void trimQueue(int numberOfRemovedElements) {
        int size = listofnews.size();
        if(size>300){
            String[] targetArray = listofnews.toArray(new String[size]);
            Set<String> strSet = Arrays.stream(targetArray)
                    .skip(numberOfRemovedElements)
                    .collect(Collectors.toSet());
            listofnews.clear();
            listofnews.addAll(strSet);
        }
    }
}
