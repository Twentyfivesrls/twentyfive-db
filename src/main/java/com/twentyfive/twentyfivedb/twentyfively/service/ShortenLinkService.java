package com.twentyfive.twentyfivedb.twentyfively.service;


import com.twentyfive.twentyfivedb.twentyfively.repository.ShortenLinkRepository;
import com.twentyfive.twentyfivedb.twentyfively.utils.GeneratePasswordUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.twlyDto.RequestValue;
import twentyfive.twentyfiveadapter.models.twlyModels.ShortenLink;

import java.util.*;
import java.util.stream.Collectors;

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
        return generateShortUrl(requestValue);
    }

    public ShortenLink generateShortUrl(RequestValue requestValue) {
        String userId = requestValue.getUserToken();

        ShortenLink toSave = new ShortenLink();
        toSave.setDestinationUrl(requestValue.getUrl());
        toSave.setUserId(userId);
        toSave.setShortUrl(generateUniqueLink());
        toSave.setCreatedAt(new Date());

        ShortenLink savedLink = shortenLinkRepository.save(toSave);

        //this should never happen, but it is a good practice to check and keep the db clean
        removeOldestLink(userId);

        return composeUrl(savedLink);
    }

    private ShortenLink composeUrl(ShortenLink shortenLink) {
        shortenLink.setShortUrl(baseUrl + shortenLink.getShortUrl());
        return shortenLink;
    }

    private void removeOldestLink(String userId) {
        List<ShortenLink> links = shortenLinkRepository.findAllByUserIdAndDeleted(userId, false);
        if (links.size() > threshold) {
            ShortenLink oldestLink = links.stream()
                    .min(Comparator.comparing(ShortenLink::getCreatedAt))
                    .orElseThrow(() -> new IllegalStateException("No links found"));

            shortenLinkRepository.delete(oldestLink);
        }
    }

    public String getCompleteShortenLink(String shortUrl) {
        return shortenLinkRepository.findByShortUrl(shortUrl)
                .map(ShortenLink::getDestinationUrl)
                .orElse("");
    }

    private String generateUniqueLink() {
        String uniqueLink;
        do {
            uniqueLink = GeneratePasswordUtil.generateCommonLangPassword(DEFAULT_SHORTEN_LINK_LENGTH);
        } while (shortenLinkRepository.findByShortUrl(uniqueLink).isPresent());
        return uniqueLink;
    }

    public List<ShortenLink> getAllLinksForUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            log.info("User id is blank");
            return Collections.emptyList();
        }

        return shortenLinkRepository.findAllByUserIdAndDeletedOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::composeUrl)
                .collect(Collectors.toList());
    }

    public void deleteLink(String id) {
        shortenLinkRepository.findById(id).ifPresent(link -> {
            link.setDeleted(true);
            shortenLinkRepository.save(link);
        });
    }
}
