package io.resourcepool.dashboard.model.entities;

public class Bundle {
    private String mUuid;

    private String mName;

    private Validity mValidity;

    private long mRevision;

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Validity getValidity() {
        return mValidity;
    }

    public void setValidity(Validity validity) {
        mValidity = validity;
    }

    public long getRevision() {
        return mRevision;
    }

    public void setRevision(long revision) {
        mRevision = revision;
    }

    @Override
    public String toString() {
        return "Bundle{" +
                "mUuid='" + mUuid + '\'' +
                ", mName='" + mName + '\'' +
                ", mValidity=" + mValidity +
                ", mRevision=" + mRevision +
                '}';
    }
}
