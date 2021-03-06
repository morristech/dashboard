package io.resourcepool.dashboard.dao.impl;

import io.resourcepool.dashboard.dao.MediaDao;
import io.resourcepool.dashboard.dao.util.MediaReverseIndex;
import io.resourcepool.dashboard.dao.util.YamlUtils;
import io.resourcepool.dashboard.exception.ResourceIoException;
import io.resourcepool.dashboard.exception.ResourceNotFoundException;
import io.resourcepool.dashboard.model.Content;
import io.resourcepool.dashboard.model.Media;
import io.resourcepool.dashboard.model.metadata.MediaMetadata;
import io.resourcepool.dashboard.model.type.MediaType;
import io.resourcepool.dashboard.property.DashboardProperties;
import io.resourcepool.dashboard.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component
@DependsOn("daoInitializer")
public class MediaDaoImpl implements MediaDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaDaoImpl.class);

    @Autowired
    private DashboardProperties props;

    @Autowired
    private FileSystem fs;

    private Path mediaDatabasePath;
    private Path mediaResourcesPath;

    private MediaReverseIndex mri = new MediaReverseIndex();

    @PostConstruct
    public void init() {
        mediaDatabasePath = fs.getPath(props.getBasePath(), ENTITY_NAME);
        mediaResourcesPath = fs.getPath(props.getBaseResources());
        // Refresh reverse index
        mri.refreshDataset(getAll());
    }

    @Override
    public MediaMetadata get(String uuid) {
        String bundleUuid = mri.getBundleTag(uuid);
        Path dataPath = getMediaFile(bundleUuid, uuid);
        return readMediaFromFile(dataPath);
    }

    @Override
    public Path getContent(String filename) {
        int index = filename.indexOf(".");
        if (index == -1) {
            return null;
        } else {
            String uuid = filename.substring(0, index);
            String ext = filename.substring(uuid.length());
            Path path = getResourceFile(uuid, ext);
            return path;
        }
    }

    @Override
    public List<MediaMetadata> getAll() {
        List<MediaMetadata> mediaMetadatas = new LinkedList<>();
        // Get all media in bundle folder
        try {
            Files.list(mediaDatabasePath)
                    .filter(Files::isDirectory)
                    .flatMap(path -> {
                        try {
                            return Files.list(path);
                        } catch (IOException e) {
                            LOGGER.error("exception in MediaDaoImpl#getAll", e);
                            throw new ResourceIoException(e);
                        }
                    })
                    .filter(Files::isRegularFile)
                    .forEach(path -> mediaMetadatas.add(readMediaFromFile(path)));
        } catch (IOException e) {
            LOGGER.error("exception in MediaDaoImpl#getAll", e);
            throw new ResourceIoException(e);
        }
        return mediaMetadatas;
    }

    @Override
    public List<MediaMetadata> getByBundle(String bundleTag) {
        List<MediaMetadata> mediaMetadatas = new LinkedList<>();
        // Get all media in bundle folder
        Path bundle = mediaDatabasePath.resolve(bundleTag);
        if (!Files.exists(bundle) || !Files.isDirectory(bundle)) {
            return mediaMetadatas;
        }
        try {
            Files.list(bundle).filter(Files::isRegularFile).forEach(path -> mediaMetadatas.add(readMediaFromFile(path)));
        } catch (IOException e) {
            LOGGER.error("exception in MediaDaoImpl#getByBundle", e);
            throw new ResourceIoException(e);
        }
        return mediaMetadatas;
    }

    @Override
    public void save(Media media) {
        MediaMetadata mediaMetadata = media.getMetadata();
        MediaType mediaType = mediaMetadata.getMediaType();

        Path dest = getMediaFile(mediaMetadata.getBundleTag(), mediaMetadata.getUuid());
        YamlUtils.store(mediaMetadata, dest);

        Content content = media.getContent();

        // TODO implement some kind of rollback if second content write fails
        if (content != null) {
            dest = getResourceFile(mediaMetadata.getUrl());
            try {
                Files.createFile(dest);
                FileCopyUtils.copy(content.getInputStream(), Files.newOutputStream(dest));
            } catch (IOException e) {
                LOGGER.warn("Error while writing file.", e);
                throw new IllegalStateException(e);
            }
        }

        // Refresh Reverse index
        refreshReverseIndex();
    }

    @Override
    public void deleteByBundle(String bundleTag) {
        if (bundleTag == null) {
            throw new ResourceNotFoundException();
        }
        // FIXME for now, we chose not to delete files when media is destroyed.
        try {
            // Delete each media of bundle tag
            Set<String> medias = mri.getMedias(bundleTag);
            if (medias == null) {
                return;
            }
            medias.forEach(this::delete);
            // Remove parent directory
            PathUtils.deleteDirectory(mediaDatabasePath.resolve(bundleTag));
        } catch (IOException e) {
            throw new ResourceIoException(e);
        }
        // Refresh Reverse index
        refreshReverseIndex();
    }

    @Override
    public void delete(String uuid) {
        String bundleTag = mri.getBundleTag(uuid);
        if (bundleTag == null) {
            throw new ResourceNotFoundException();
        }

        MediaMetadata mediaMetadata = get(uuid);

        // TODO add a rollback
        boolean result = YamlUtils.delete(mediaDatabasePath.resolve(bundleTag + "/" + uuid + ".yaml"));
        if (!result) {
            throw new ResourceIoException();
        }

        if (mediaMetadata.hasFile()) {
            result = YamlUtils.delete(getResourceFile(mediaMetadata.getUrl()));
            if (!result) {
                throw new ResourceIoException();
            }
        }

        // TODO Refresh Reverse index
        refreshReverseIndex();
    }

    private void refreshReverseIndex() {
        mri.invalidate();
        mri.refreshDataset(getAll());
    }

    /**
     * Retrieve media file from bundle tag and media tag
     *
     * @param bundleTag
     * @param uuid
     * @return
     */
    private Path getMediaFile(String bundleTag, String uuid) {
        String dataFileName = uuid + ".yaml";
        return mediaDatabasePath.resolve(bundleTag + "/" + dataFileName);
    }

    /**
     * Retrieve Resource file from URL.
     *
     * @param url the resource url
     * @return the resource file
     */
    private Path getResourceFile(String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        String ext = filename.substring(filename.lastIndexOf("."));
        String uuid = filename.substring(0, filename.lastIndexOf("."));
        return getResourceFile(uuid, ext);
    }

    /**
     * Retrieve Resource file from content uuid and extension.
     *
     * @param uuid the content uuid
     * @param ext  the extension
     * @return the resource file
     */
    private Path getResourceFile(String uuid, String ext) {
        String dataFileName = uuid + (ext.startsWith(".") ? ext : "." + ext);
        return mediaResourcesPath.resolve(dataFileName);
    }


    private MediaMetadata readMediaFromFile(Path path) {
        return YamlUtils.read(path, MediaMetadata.class);
    }
}
