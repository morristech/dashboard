package com.excilys.shooflers.dashboard.server.model.metadata;

import com.excilys.shooflers.dashboard.server.model.Validity;
import com.excilys.shooflers.dashboard.server.model.type.MediaType;

import java.util.UUID;

/**
 * A media represent a file in memory (a file can be an image, a pdf, a video or a website).
 */
public class MediaMetadata {

    private String uuid;

    private String name;

    private int duration;

    private MediaType mediaType;

    private Validity validity;

    private String url;

    private String bundleTag;

    public MediaMetadata() { }

    private MediaMetadata(String uuid, String name, int duration, MediaType mediaType, Validity validity, String url, String bundleTag) {
        this.uuid = uuid;
        this.name = name;
        this.duration = duration;
        this.mediaType = mediaType;
        this.validity = validity;
        this.url = url;
        this.bundleTag = bundleTag;
    }

    public static class Builder {

        private String name;

        private int duration;

        private MediaType mediaType;

        private Validity validity;

        private String uuid;

        private String url;

        private String bundleTag;

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder validity(Validity validity) {
            this.validity = validity;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder bundleTag(String bundleTag) {
            this.bundleTag = bundleTag;
            return this;
        }

        public MediaMetadata build() {
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            return new MediaMetadata(uuid, name, duration, mediaType, validity, url, bundleTag);
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBundleTag() {
        return bundleTag;
    }

    public void setBundleTag(String bundleTag) {
        this.bundleTag = bundleTag;
    }
}