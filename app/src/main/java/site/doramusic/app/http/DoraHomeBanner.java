package site.doramusic.app.http;

import androidx.annotation.NonNull;

import dora.db.constraint.Id;
import dora.db.migration.OrmMigration;
import dora.db.table.OrmTable;
import dora.db.table.PrimaryKeyEntry;
import dora.db.table.PrimaryKeyId;

public class DoraHomeBanner implements OrmTable {

    private String imgUrl;
    private String detailUrl;
    @Id
    private long id;

    public String getImgUrl() {
        return imgUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    @Override
    public PrimaryKeyEntry getPrimaryKey() {
        return new PrimaryKeyId(id);
    }

    @Override
    public boolean isUpgradeRecreated() {
        return false;
    }

    @NonNull
    @Override
    public OrmMigration[] getMigrations() {
        return new OrmMigration[0];
    }
}
