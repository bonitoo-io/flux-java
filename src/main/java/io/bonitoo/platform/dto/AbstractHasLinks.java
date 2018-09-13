package io.bonitoo.platform.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 08:50)
 */
public abstract class AbstractHasLinks {
    
    /**
     * The URIs of resources.
     */
    private Map<String, String> links = new HashMap<>();

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(final Map<String, String> links) {
        this.links = links;
    }
}