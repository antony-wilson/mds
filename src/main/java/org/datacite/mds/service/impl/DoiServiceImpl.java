package org.datacite.mds.service.impl;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;
import org.datacite.mds.domain.Datacentre;
import org.datacite.mds.domain.Dataset;
import org.datacite.mds.service.DoiService;
import org.datacite.mds.service.HandleException;
import org.datacite.mds.service.HandleService;
import org.datacite.mds.service.SecurityException;
import org.datacite.mds.web.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoiServiceImpl implements DoiService {

    static final Logger log4j = Logger.getLogger(DoiServiceImpl.class);

    @Autowired
    HandleService handleService;

    public void create(String doi, String url, boolean testMode) throws HandleException, SecurityException {
        Datacentre datacentre = preliminaryCheck(doi, url);

        if (!testMode) {
            log4j.debug("trying handle registration: " + doi);

            handleService.create(doi, url);
            datacentre.incQuotaUsed();

            Dataset dataset = new Dataset();
            dataset.setDatacentre(datacentre);
            dataset.setDoi(doi);
            dataset.setIsActive(true);
            dataset.setLastMetadataStatus("ABSENT"); // TODO refactor - use enum
            dataset.persist();

            log4j.debug("doi registartion: " + dataset.getDoi() + " successful");
        } else {
            log4j.debug("TEST MODE - registration skipped");
        }
    }

    public void update(String doi, String url, boolean testMode) throws HandleException, SecurityException {
        Datacentre datacentre = preliminaryCheck(doi, url);
        Dataset dataset = null;
        try {
            dataset = (Dataset) Dataset.findDatasetsByDoiEquals(doi).getSingleResult();
        } catch (NoResultException e) {
            throw new SecurityException("DOI doesn't exist");
        } catch (NonUniqueResultException e) {
            String message = "more then one row for: " + doi;
            log4j.error(message, e);
            throw new RuntimeException(message, e);
        }

        if (!datacentre.getSymbol().equals(dataset.getDatacentre().getSymbol())) {
            String message = "cannot update DOI which belongs to another party";
            log4j.warn(datacentre.getSymbol() + " " + message + ", DOI: " + dataset.getDoi());
            throw new SecurityException(message);
        }

        if (!testMode) {
            handleService.update(doi, url);

            log4j.debug("doi update: " + doi + " successful");
        } else {
            log4j.debug("TEST MODE - update skipped");
        }
    }

    private Datacentre preliminaryCheck(String doi, String url) throws SecurityException {
        Datacentre datacentre = null;

        datacentre = SecurityUtils.getDatacentre();
        SecurityUtils.checkQuota(datacentre);
        SecurityUtils.checkRestrictions(doi, url, datacentre);

        return datacentre;
    }
}
