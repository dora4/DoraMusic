package site.doramusic.app.db;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import dora.db.constraint.AssignType;
import dora.db.constraint.NotNull;
import dora.db.constraint.PrimaryKey;
import dora.db.constraint.Unique;
import dora.db.migration.OrmMigration;
import dora.db.table.Column;
import dora.db.table.Ignore;
import dora.db.table.OrmTable;
import dora.db.table.Table;
import site.doramusic.app.sort.Sort;

/**
 * 文件夹表。
 */
@Table("folder")
public class Folder implements OrmTable, Parcelable, Sort {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOLDER_NAME = "folder_name";
    public static final String COLUMN_FOLDER_PATH = "folder_path";

    @Ignore
    private String sortLetter;

    @Column(COLUMN_ID)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    public int id;

    @Column(COLUMN_FOLDER_NAME)
    public String name;

    @Unique
    @NotNull
    @Column(COLUMN_FOLDER_PATH)
    public String path;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt(COLUMN_ID, id);
        bundle.putString(COLUMN_FOLDER_NAME, name);
        bundle.putString(COLUMN_FOLDER_PATH, path);
        dest.writeBundle(bundle);
    }

    public Folder() {
    }

    public static Creator<Folder> CREATOR = new Creator<Folder>() {

        @Override
        public Folder createFromParcel(Parcel source) {
            Folder folder = new Folder();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            folder.id = bundle.getInt(COLUMN_ID);
            folder.name = bundle.getString(COLUMN_FOLDER_NAME);
            folder.path = bundle.getString(COLUMN_FOLDER_PATH);
            return folder;
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    @Override
    public boolean isUpgradeRecreated() {
        return false;
    }

    @Override
    public String getSortLetter() {
        return sortLetter;
    }

    @Override
    public void setSortLetter(String sortLetter) {
        this.sortLetter = sortLetter;
    }

    @Override
    public int compareTo(Sort sort) {
        return getSortLetter().compareTo(sort.getSortLetter());
    }

    @NonNull
    @Override
    public OrmMigration[] getMigrations() {
        return new OrmMigration[0];
    }
}
