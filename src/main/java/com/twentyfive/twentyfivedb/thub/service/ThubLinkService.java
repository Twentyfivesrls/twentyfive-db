package com.twentyfive.twentyfivedb.thub.service;

import com.twentyfive.twentyfivedb.thub.repository.ThubProfileRepository;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.ThubDocumentDB.ThubLink;
import twentyfive.twentyfiveadapter.adapter.Document.ThubDocumentDB.ThubProfile;

import java.util.List;
import java.util.UUID;

@Service
public class ThubLinkService {

    private final ThubProfileRepository thubProfileRepository;
    public ThubLinkService(ThubProfileRepository thubProfileRepository) {
        this.thubProfileRepository = thubProfileRepository;
    }

    public List<ThubLink> getProfileLinks(String username) {
        ThubProfile tP = thubProfileRepository.findByUsername(username).orElse(null);

        if(tP == null) {
            return null;
        }

        return tP.getLinks();
    }

    public List<ThubLink> saveLinks(String username, List<ThubLink> links) {
        ThubProfile tP = thubProfileRepository.findByUsername(username).orElse(null);

        if(tP == null) {
            return null;
        }

        tP.setLinks(links);
        thubProfileRepository.save(tP);

        return tP.getLinks();
    }

    public List<ThubLink> addLink(String username, ThubLink link) {
        ThubProfile tP = thubProfileRepository.findByUsername(username).orElse(null);

        if(tP == null) {
            return null;
        }

        link.setId(UUID.randomUUID().toString());
        tP.getLinks().add(link);
        thubProfileRepository.save(tP);

        return tP.getLinks();
    }

    public List<ThubLink> updateLink(String username, ThubLink link) {
        ThubProfile tP = thubProfileRepository.findByUsername(username).orElse(null);

        if(tP == null) {
            return null;
        }

        for (ThubLink l : tP.getLinks()) {
            if (l.getId().equals(link.getId())) {
                l.setUrl(link.getUrl());
                l.setName(link.getName());
                l.setThumbnail(link.getThumbnail());
                l.setEnabled(link.isEnabled());
                break;
            }
        }

        thubProfileRepository.save(tP);
        return tP.getLinks();
    }

    public List<ThubLink> deleteLink(String username, String linkId) {
        ThubProfile tP = thubProfileRepository.findByUsername(username).orElse(null);

        if(tP == null) {
            return null;
        }

        for (ThubLink l : tP.getLinks()) {
            if (l.getId().equals(linkId)) {
                tP.getLinks().remove(l);
                break;
            }
        }

        thubProfileRepository.save(tP);
        return tP.getLinks();
    }

}
