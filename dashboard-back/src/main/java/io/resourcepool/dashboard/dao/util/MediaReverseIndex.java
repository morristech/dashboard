package io.resourcepool.dashboard.dao.util;

import io.resourcepool.dashboard.model.metadata.MediaMetadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Loïc Ortola on 12/06/2016.
 */
public class MediaReverseIndex {
    private Map<String, Set<String>> reverseIndex;

    public MediaReverseIndex() {
        reverseIndex = new HashMap<>();
    }

    /**
     * Retrieve the media uuids associated with a bundle tag.
     *
     * @param bundleTag the bundle tag
     * @return null if no match, the set otherwise
     */
    public Set<String> getMedias(String bundleTag) {
        return reverseIndex.get(bundleTag);
    }

    /**
     * Retrieve the bundle tag of a media.
     *
     * @param uuid the media tag
     * @return tag
     */
    public String getBundleTag(String uuid) {
        Optional<Map.Entry<String, Set<String>>> optional = reverseIndex
                .entrySet()
                .stream()
                .filter(e -> e.getValue().contains(uuid))
                .findFirst();

        return  optional.isPresent() ? optional.get().getKey() : null;
    }

    public void addEntry(String bundleTag, String uuid) {
        Set<String> medias = reverseIndex.get(bundleTag);
        if (medias == null) {
            medias = new HashSet<>();
            reverseIndex.put(bundleTag, medias);
        }
        medias.add(uuid);
    }

    public void invalidate() {
        reverseIndex.clear();
    }

    public void refreshDataset(List<MediaMetadata> medias) {
        if (medias == null || medias.isEmpty()) {
            return;
        }
        for (MediaMetadata media : medias) {
            addEntry(media.getBundleTag(), media.getUuid());
        }
    }
}
