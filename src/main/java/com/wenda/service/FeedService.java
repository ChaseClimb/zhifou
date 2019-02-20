package com.wenda.service;

import com.wenda.dao.FeedDao;
import com.wenda.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {
    @Autowired
    FeedDao feedDao;

    public List<Feed> getUserFeeds(List<Integer> userIds){
        return feedDao.selectUserFeeds(userIds);
    }

    public void addFeed(Feed feed){
        feedDao.addFeed(feed);
    }

    public Feed getById(int id){
        return feedDao.getFeedById(id);
    }

    public List<Feed> getFeedsByUserId(int userId){
        return feedDao.getFeedsByUserId(userId);
    }
}
