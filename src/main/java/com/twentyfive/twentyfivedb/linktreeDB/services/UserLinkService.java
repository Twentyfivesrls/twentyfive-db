package com.twentyfive.twentyfivedb.linktreeDB.services;

import com.twentyfive.twentyfivedb.linktreeDB.repositories.UserLinkRepository;
import com.twentyfive.twentyfivemodel.exceptions.LinkDoesntExistException;
import com.twentyfive.twentyfivemodel.exceptions.UserLinkDoesntExistException;
import com.twentyfive.twentyfivemodel.models.linktreeModels.LinkTree;
import com.twentyfive.twentyfivemodel.models.linktreeModels.UserLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.UserLinkDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;


import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLinkService {
    private final UserLinkRepository userLinkRepository;

    public UserLink findByUsername(String username){

        UserLinkDocumentDB userLink = userLinkRepository.findByUserId(username).orElse(null);

        return TwentyFiveMapper.INSTANCE.userLinkDocumentDBToUserLink(userLink);
    }

    public UserLink add(LinkTree linkTree, String username) throws RuntimeException{
        linkTree.setId(UUID.randomUUID().toString());
        if(!userLinkRepository.existsByUserId(username)) {
            UserLink newUser = new UserLink();
            newUser.setUserId(username);
            List<LinkTree> userLinks = new LinkedList<>();
            userLinks.add(linkTree);
            newUser.setLinkTrees(userLinks);
            userLinkRepository.save(TwentyFiveMapper.INSTANCE.userLinkToUserLinkDocumentDB(newUser));
            return newUser;
        }
        else {
            UserLinkDocumentDB oldUserDB = userLinkRepository.findByUserId(username).get();
            oldUserDB.getLinkTrees().add(linkTree);
            userLinkRepository.save(oldUserDB);
            return TwentyFiveMapper.INSTANCE.userLinkDocumentDBToUserLink(oldUserDB);
        }
    }

    public UserLink update(String id, LinkTree linkTree, String username) throws RuntimeException{

        UserLinkDocumentDB userLink = userLinkRepository.findByUserId(username)
                .orElseThrow(() -> new UserLinkDoesntExistException());
        for ( LinkTree l: userLink.getLinkTrees()){
            if (l.getId().equals(id)){
                l.setNameLink(linkTree.getNameLink());
                l.setLink(linkTree.getLink());
            }
        }
        userLinkRepository.save(userLink);
        return TwentyFiveMapper.INSTANCE.userLinkDocumentDBToUserLink(userLink);
    }

    public UserLink delete(String id,String username) throws RuntimeException{
        UserLinkDocumentDB userLink = userLinkRepository.findByUserId(username)
                .orElseThrow(() -> new UserLinkDoesntExistException());
        for (LinkTree l : userLink.getLinkTrees()){
            if (id.equals(l.getId())){
                userLink.getLinkTrees().remove(l);
                userLinkRepository.save(userLink);
                return TwentyFiveMapper.INSTANCE.userLinkDocumentDBToUserLink(userLink);
            }
        }
        throw new LinkDoesntExistException();
    }
}
