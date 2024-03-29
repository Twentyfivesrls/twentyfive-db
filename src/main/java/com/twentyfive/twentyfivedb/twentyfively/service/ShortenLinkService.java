package com.twentyfive.twentyfivedb.twentyfively.service;


import com.twentyfive.twentyfivedb.twentyfively.repository.ShortenLinkRepository;
import com.twentyfive.twentyfivedb.twentyfively.utils.GeneratePasswordUtil;
import com.twentyfive.twentyfivemodel.dto.twentyfiveLyDto.RequestValue;
import com.twentyfive.twentyfivemodel.models.twentyfiveLyModels.ShortenLink;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.ShortenLinkDocumentDB.ShortenLinkDocumentDB;
import twentyfive.twentyfiveadapter.adapter.Mapper.TwentyFiveMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.twentyfive.twentyfivedb.twentyfively.constants.ShortenLinkConstants.DEFAULT_SHORTEN_LINK_LENGTH;


@Slf4j
@Service
public class ShortenLinkService {

    private final ShortenLinkRepository shortenLinkRepository;

    @Value("${deployment.base.url}")
    private String baseUrl;

    @Value("${max.link.threshold}")
    private int threshold;

    public ShortenLinkService(ShortenLinkRepository shortenLinkRepository) {
        this.shortenLinkRepository = shortenLinkRepository;
    }


    public ShortenLink generateForMailKeycloak(RequestValue requestValue) {
        ShortenLinkDocumentDB toSave = new ShortenLinkDocumentDB();
        toSave.setDestinationUrl(requestValue.getUrl());

        toSave.setShortUrl(generateUniqueLink());
        toSave.setCreatedAt(new Date());
        ShortenLinkDocumentDB response = shortenLinkRepository.save(toSave);

        String shortUrlString = response.getShortUrl();
        String composedUrl = baseUrl + shortUrlString;
        response.setShortUrl(composedUrl);
        return TwentyFiveMapper.INSTANCE.shortenLinkDocumentDBToShortenLink(response);
    }

    public ShortenLink generateShortUrl(RequestValue requestValue) {
        //TODO: modificare la chiamata con l'username una volta che sarà implementato il login
        String userId = "";

        ShortenLinkDocumentDB toSave = new ShortenLinkDocumentDB();
        toSave.setDestinationUrl(requestValue.getUrl());

        if (StringUtils.isBlank(userId) || userId.equals("Guest")) {
            userId = requestValue.getUserToken();
        }

        toSave.setUserId(userId);
        toSave.setShortUrl(generateUniqueLink());
        toSave.setCreatedAt(new Date());
        ShortenLinkDocumentDB response = shortenLinkRepository.save(toSave);
        this.removeOldestLink(userId);

        String shortUrlString = response.getShortUrl();
        String composedUrl = baseUrl + shortUrlString;
        response.setShortUrl(composedUrl);
        return TwentyFiveMapper.INSTANCE.shortenLinkDocumentDBToShortenLink(response);
    }

    private void removeOldestLink(String userId) {
        List<ShortenLinkDocumentDB> allByUserId = shortenLinkRepository.findAllByUserId(userId);
        if (allByUserId.size() > threshold) {
            ShortenLinkDocumentDB oldest = allByUserId.get(0);
            for (ShortenLinkDocumentDB s : allByUserId) {
                if (s.getCreatedAt().before(oldest.getCreatedAt())) {
                    oldest = s;
                }
            }
            shortenLinkRepository.delete(oldest);
        }
    }

    public String getCompleteShortenLink(String shortUrl) {
        Optional<ShortenLinkDocumentDB> onDb = shortenLinkRepository.findByShortUrl(shortUrl);
        return onDb.map(ShortenLinkDocumentDB::getDestinationUrl).orElse("");
    }

    private String generateUniqueLink() {
        while (true) {
            String current = GeneratePasswordUtil.generateCommonLangPassword(DEFAULT_SHORTEN_LINK_LENGTH);
            if (shortenLinkRepository.findByShortUrl(current).isEmpty()) {
                return current;
            }
        }
    }


    public List<ShortenLink> getAllLinksForUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            log.info("User id is blank");
            return new ArrayList<>();
        }
        List<ShortenLinkDocumentDB> res = shortenLinkRepository.findAllByUserId(userId);
        List<ShortenLink> mapList = new ArrayList<>();
        for (ShortenLinkDocumentDB s : res) {
            mapList.add(TwentyFiveMapper.INSTANCE.shortenLinkDocumentDBToShortenLink(s));
        }


        for (ShortenLink s : mapList) {
            String shortUrlString = s.getShortUrl();
            String composedUrl = baseUrl + shortUrlString;
            s.setShortUrl(composedUrl);
        }
        return mapList;
    }

    public void deleteLink(String id) {
        shortenLinkRepository.deleteById(id);
    }
}
